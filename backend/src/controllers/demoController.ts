import { Request, Response } from 'express';
import { prisma } from '../database/db.js';
import { healthMonitor } from '../services/healthMonitor.js';
import { seedDatabase } from '../database/seed.js';
import { auditService } from '../services/auditService.js';
import { simulateHealthSchema, demoScenarioSchema } from '../validators/index.js';

export class DemoController {
  async triggerScenario(req: Request, res: Response) {
    try {
      const parsed = demoScenarioSchema.safeParse(req.body);
      const scenarioId = parsed.success ? parsed.data.scenarioId : 'hero-demo';

      let deploymentId = 'dep-checkout-v284';
      if (scenarioId === 'low-risk-docs') deploymentId = 'dep-docs-v112';
      if (scenarioId === 'medium-risk-api') deploymentId = 'dep-gateway-v340';

      const deployment = await prisma.deployment.findUnique({
        where: { id: deploymentId },
        include: { riskAssessment: true }
      });

      if (!deployment) {
        return res.status(404).json({ error: 'Scenario deployment not found. Run /api/demo/reset first.' });
      }

      await auditService.log({
        deploymentId: deployment.id,
        userName: 'Judge / Demo Presenter',
        userRole: 'RELEASE_MANAGER',
        action: 'DEMO_SCENARIO_TRIGGERED',
        details: `Triggered live demo scenario: ${scenarioId}`,
        result: 'SUCCESS'
      });

      return res.json({
        scenario: scenarioId,
        deploymentId: deployment.id,
        serviceName: deployment.serviceName,
        version: deployment.version,
        riskScore: deployment.riskAssessment?.riskScore,
        riskLevel: deployment.riskAssessment?.riskLevel,
        recommendation: deployment.riskAssessment?.recommendation
      });
    } catch (err: any) {
      return res.status(500).json({ error: err.message });
    }
  }

  async simulateHealth(req: Request, res: Response) {
    try {
      const parsed = simulateHealthSchema.safeParse(req.body);
      const deploymentId = parsed.success ? parsed.data.deploymentId : 'dep-checkout-v284';
      const degrade = parsed.success ? parsed.data.degrade : true;

      let metric;
      if (degrade) {
        metric = await healthMonitor.triggerDemoDegradation(deploymentId);
        await auditService.log({
          deploymentId,
          userName: 'Health Telemetry Daemon',
          userRole: 'ADMIN',
          action: 'HEALTH_ALERT',
          details: '🚨 DEPLOYMENT DEGRADATION DETECTED: Error rate spiked to 7.4%, latency 780ms. Recommendation: ROLLBACK',
          result: 'SUCCESS'
        });
      } else {
        metric = await healthMonitor.resetDemoHealth(deploymentId);
      }

      const comparison = await healthMonitor.getDeploymentHealth(deploymentId);

      return res.json({
        success: true,
        degraded: degrade,
        health: comparison
      });
    } catch (err: any) {
      return res.status(500).json({ error: err.message });
    }
  }

  async resetDemo(req: Request, res: Response) {
    try {
      await seedDatabase();
      return res.json({
        success: true,
        message: 'ShipSafe environment successfully reset to fresh demo state.'
      });
    } catch (err: any) {
      return res.status(500).json({ error: err.message });
    }
  }
}

export const demoController = new DemoController();
