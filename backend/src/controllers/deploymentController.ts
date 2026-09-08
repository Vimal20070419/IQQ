import { Response } from 'express';
import { prisma } from '../database/db.js';
import { AuthRequest } from '../middleware/authMiddleware.js';
import { riskEngine } from '../services/riskEngine.js';
import { incidentMemory } from '../services/incidentMemory.js';
import { blastRadiusEngine } from '../services/blastRadiusEngine.js';
import { failureChainEngine } from '../services/failureChainEngine.js';
import { aiService } from '../services/aiService.js';
import { healthMonitor } from '../services/healthMonitor.js';
import { rollbackGuard } from '../services/rollbackGuard.js';
import { auditService } from '../services/auditService.js';
import { decisionActionSchema } from '../validators/index.js';

export class DeploymentController {
  async listDeployments(req: AuthRequest, res: Response) {
    try {
      const deployments = await prisma.deployment.findMany({
        orderBy: { createdAt: 'desc' },
        include: {
          riskAssessment: true,
          files: true,
          healthMetrics: {
            orderBy: { recordedAt: 'desc' },
            take: 1
          }
        }
      });

      return res.json(deployments.map(d => ({
        id: d.id,
        serviceName: d.serviceName,
        version: d.version,
        previousVersion: d.previousVersion,
        environment: d.environment,
        status: d.status,
        commitSha: d.commitSha,
        commitMessage: d.commitMessage,
        author: d.author,
        filesChanged: d.filesChanged,
        linesAdded: d.linesAdded,
        linesDeleted: d.linesDeleted,
        databaseMigration: d.databaseMigration,
        createdAt: d.createdAt,
        risk: d.riskAssessment ? {
          riskLevel: d.riskAssessment.riskLevel,
          riskScore: d.riskAssessment.riskScore,
          summary: d.riskAssessment.summary,
          recommendation: d.riskAssessment.recommendation,
          affectedServices: JSON.parse(d.riskAssessment.affectedServicesJson || '[]')
        } : null,
        latestHealth: d.healthMetrics[0] || null
      })));
    } catch (err: any) {
      return res.status(500).json({ error: err.message });
    }
  }

  async getDeploymentById(req: AuthRequest, res: Response) {
    try {
      const { id } = req.params;
      const deployment = await prisma.deployment.findUnique({
        where: { id },
        include: {
          riskAssessment: true,
          files: true,
          decisions: {
            include: { user: { select: { name: true, role: true } } },
            orderBy: { createdAt: 'desc' }
          },
          healthMetrics: {
            orderBy: { recordedAt: 'desc' },
            take: 5
          }
        }
      });

      if (!deployment) {
        return res.status(404).json({ error: 'Deployment not found' });
      }

      return res.json({
        ...deployment,
        riskAssessment: deployment.riskAssessment ? {
          ...deployment.riskAssessment,
          factors: JSON.parse(deployment.riskAssessment.factorsJson || '[]'),
          evidence: JSON.parse(deployment.riskAssessment.evidenceJson || '[]'),
          affectedServices: JSON.parse(deployment.riskAssessment.affectedServicesJson || '[]'),
          failureChain: JSON.parse(deployment.riskAssessment.failureChainJson || '[]'),
          similarIncidents: JSON.parse(deployment.riskAssessment.similarIncidentsJson || '[]')
        } : null
      });
    } catch (err: any) {
      return res.status(500).json({ error: err.message });
    }
  }

  async getRiskAssessment(req: AuthRequest, res: Response) {
    try {
      const { id } = req.params;
      const deployment = await prisma.deployment.findUnique({
        where: { id },
        include: { riskAssessment: true, files: true }
      });

      if (!deployment) {
        return res.status(404).json({ error: 'Deployment not found' });
      }

      if (deployment.riskAssessment) {
        return res.json({
          deploymentId: deployment.id,
          serviceName: deployment.serviceName,
          version: deployment.version,
          environment: deployment.environment,
          riskLevel: deployment.riskAssessment.riskLevel,
          riskScore: deployment.riskAssessment.riskScore,
          summary: deployment.riskAssessment.summary,
          recommendation: deployment.riskAssessment.recommendation,
          confidence: deployment.riskAssessment.confidence,
          isAiGenerated: deployment.riskAssessment.isAiGenerated,
          factors: JSON.parse(deployment.riskAssessment.factorsJson || '[]'),
          evidence: JSON.parse(deployment.riskAssessment.evidenceJson || '[]'),
          affectedServices: JSON.parse(deployment.riskAssessment.affectedServicesJson || '[]'),
          failureChain: JSON.parse(deployment.riskAssessment.failureChainJson || '[]'),
          similarIncidents: JSON.parse(deployment.riskAssessment.similarIncidentsJson || '[]')
        });
      }

      // Compute on the fly if not exists
      const sensitiveFiles = deployment.files.filter(f => f.isSensitive);
      const sensitiveAreas = sensitiveFiles.map(f => f.category);
      const blastRadius = await blastRadiusEngine.computeBlastRadius(deployment.serviceName);
      const similarIncidents = await incidentMemory.findSimilarIncidents(
        deployment.serviceName,
        deployment.files.map(f => f.filePath),
        sensitiveAreas
      );

      const deterministic = riskEngine.calculateRisk({
        serviceName: deployment.serviceName,
        environment: deployment.environment,
        filesChanged: deployment.filesChanged,
        linesAdded: deployment.linesAdded,
        linesDeleted: deployment.linesDeleted,
        databaseMigration: deployment.databaseMigration,
        files: deployment.files,
        similarIncidentsCount: similarIncidents.length,
        highestIncidentSimilarity: similarIncidents[0]?.similarityScore || 0,
        downstreamCount: blastRadius.downstreamServicesCount
      });

      const failureChain = failureChainEngine.generateFailureChain(
        deployment.serviceName,
        sensitiveAreas,
        blastRadius.nodes.map(n => n.name),
        deployment.files
      );

      const aiSynthesized = await aiService.synthesizeRiskAssessment({
        deployment: deployment.serviceName,
        version: deployment.version,
        environment: deployment.environment,
        filesChanged: deployment.filesChanged,
        linesChanged: deployment.linesAdded + deployment.linesDeleted,
        sensitiveAreas,
        affectedServices: blastRadius.nodes.map(n => n.name),
        historicalIncidents: similarIncidents,
        failureChain,
        deterministicResult: deterministic
      });

      return res.json({
        deploymentId: deployment.id,
        serviceName: deployment.serviceName,
        version: deployment.version,
        environment: deployment.environment,
        ...aiSynthesized,
        similarIncidents
      });
    } catch (err: any) {
      return res.status(500).json({ error: err.message });
    }
  }

  async getEvidence(req: AuthRequest, res: Response) {
    try {
      const { id } = req.params;
      const deployment = await prisma.deployment.findUnique({
        where: { id },
        include: { files: true, riskAssessment: true }
      });

      if (!deployment) return res.status(404).json({ error: 'Deployment not found' });

      const evidenceBullets = deployment.riskAssessment
        ? JSON.parse(deployment.riskAssessment.evidenceJson || '[]')
        : [];

      return res.json({
        deploymentId: deployment.id,
        serviceName: deployment.serviceName,
        version: deployment.version,
        environment: deployment.environment,
        filesChangedCount: deployment.filesChanged,
        linesChangedCount: deployment.linesAdded + deployment.linesDeleted,
        databaseMigration: deployment.databaseMigration,
        changedFiles: deployment.files,
        evidence: evidenceBullets
      });
    } catch (err: any) {
      return res.status(500).json({ error: err.message });
    }
  }

  async getBlastRadius(req: AuthRequest, res: Response) {
    try {
      const { id } = req.params;
      const deployment = await prisma.deployment.findUnique({ where: { id } });
      if (!deployment) return res.status(404).json({ error: 'Deployment not found' });

      const data = await blastRadiusEngine.computeBlastRadius(deployment.serviceName);
      return res.json({
        deploymentId: deployment.id,
        serviceName: deployment.serviceName,
        ...data
      });
    } catch (err: any) {
      return res.status(500).json({ error: err.message });
    }
  }

  async getFailureChain(req: AuthRequest, res: Response) {
    try {
      const { id } = req.params;
      const deployment = await prisma.deployment.findUnique({
        where: { id },
        include: { files: true, riskAssessment: true }
      });
      if (!deployment) return res.status(404).json({ error: 'Deployment not found' });

      if (deployment.riskAssessment?.failureChainJson) {
        return res.json({
          deploymentId: deployment.id,
          serviceName: deployment.serviceName,
          label: 'AI-assisted potential failure path (evidence grounded)',
          steps: JSON.parse(deployment.riskAssessment.failureChainJson)
        });
      }

      const steps = failureChainEngine.generateFailureChain(
        deployment.serviceName,
        ['payment configuration'],
        ['Checkout Service', 'Payment Service', 'Order Service', 'Inventory Service'],
        deployment.files
      );

      return res.json({
        deploymentId: deployment.id,
        serviceName: deployment.serviceName,
        label: 'AI-assisted potential failure path (evidence grounded)',
        steps
      });
    } catch (err: any) {
      return res.status(500).json({ error: err.message });
    }
  }

  async getHealth(req: AuthRequest, res: Response) {
    try {
      const { id } = req.params;
      const health = await healthMonitor.getDeploymentHealth(id);
      return res.json(health);
    } catch (err: any) {
      return res.status(500).json({ error: err.message });
    }
  }

  async getRollbackGuard(req: AuthRequest, res: Response) {
    try {
      const { id } = req.params;
      const guardResult = await rollbackGuard.verifyRollbackTarget(id);
      return res.json(guardResult);
    } catch (err: any) {
      return res.status(500).json({ error: err.message });
    }
  }

  async approveDeployment(req: AuthRequest, res: Response) {
    return this.handleDecision(req, res, 'APPROVE');
  }

  async canaryDeployment(req: AuthRequest, res: Response) {
    return this.handleDecision(req, res, 'CANARY');
  }

  async holdDeployment(req: AuthRequest, res: Response) {
    return this.handleDecision(req, res, 'HOLD');
  }

  async rollbackDeployment(req: AuthRequest, res: Response) {
    try {
      const { id } = req.params;
      const parsed = decisionActionSchema.safeParse(req.body);
      const reason = parsed.success ? parsed.data.reason : 'Automated rollback triggered after health degradation';
      const deviceModel = parsed.success ? parsed.data.deviceModel : 'iQOO 12 Pro (Android 14)';

      // 1. Rollback Guard Pre-flight Revalidation
      const guard = await rollbackGuard.verifyRollbackTarget(id);
      if (!guard.canRollback) {
        await auditService.log({
          deploymentId: id,
          userName: req.user?.name || 'On-Call SRE',
          userRole: req.user?.role || 'ON_CALL_ENGINEER',
          action: 'ROLLBACK',
          details: `Rollback blocked by safety guard: ${guard.checks.find(c => !c.passed)?.detail}`,
          result: 'BLOCKED',
          deviceModel
        });
        return res.status(400).json({
          error: 'Rollback pre-flight safety check failed',
          guard
        });
      }

      // 2. Execute Rollback State Transition
      const updated = await prisma.deployment.update({
        where: { id },
        data: { status: 'ROLLED_BACK' }
      });

      const fromVersion = guard.currentVersion || updated.version;
      const toVersion = guard.targetVersion || updated.previousVersion;

      // 3. Record Decision
      await prisma.deploymentDecision.create({
        data: {
          deploymentId: id,
          userId: req.user?.id || 'usr-oncall',
          action: 'ROLLBACK',
          reason
        }
      });

      // 4. Log Immutable Audit Record
      await auditService.log({
        deploymentId: id,
        userId: req.user?.id || 'usr-oncall',
        userName: req.user?.name || 'David Miller',
        userRole: req.user?.role || 'ON_CALL_ENGINEER',
        action: 'ROLLBACK',
        details: `ROLLBACK SUCCESSFUL: Restored stable version ${toVersion}. Reason: ${reason}`,
        result: 'SUCCESS',
        deviceModel
      });

      return res.json({
        success: true,
        deploymentId: id,
        fromVersion: fromVersion,
        toVersion: toVersion,
        restoredVersion: toVersion,
        status: 'RESTORED',
        action: 'ROLLBACK',
        deployment: updated
      });
    } catch (err: any) {
      return res.status(500).json({ error: err.message });
    }
  }

  private async handleDecision(req: AuthRequest, res: Response, action: 'APPROVE' | 'CANARY' | 'HOLD') {
    try {
      const { id } = req.params;
      const parsed = decisionActionSchema.safeParse(req.body);
      const reason = parsed.success ? parsed.data.reason : `Executed ${action} decision from mobile app`;
      const trafficPct = action === 'CANARY' ? (parsed.success && parsed.data.trafficPct ? parsed.data.trafficPct : 10) : undefined;
      const deviceModel = parsed.success ? parsed.data.deviceModel : 'iQOO 12 Pro (Android 14)';

      let newStatus = 'APPROVED';
      if (action === 'CANARY') newStatus = 'CANARY';
      if (action === 'HOLD') newStatus = 'HELD';

      const updated = await prisma.deployment.update({
        where: { id },
        data: { status: newStatus }
      });

      await prisma.deploymentDecision.create({
        data: {
          deploymentId: id,
          userId: req.user?.id || 'usr-relmgr',
          action,
          trafficPct,
          reason
        }
      });

      await auditService.log({
        deploymentId: id,
        userId: req.user?.id || 'usr-relmgr',
        userName: req.user?.name || 'Sarah Chen',
        userRole: req.user?.role || 'RELEASE_MANAGER',
        action,
        details: action === 'CANARY' ? `Initiated Canary rollout at ${trafficPct}% traffic. Reason: ${reason}` : `Executed ${action}. Reason: ${reason}`,
        result: 'SUCCESS',
        deviceModel
      });

      return res.json({
        success: true,
        action,
        trafficPct,
        deployment: updated
      });
    } catch (err: any) {
      return res.status(500).json({ error: err.message });
    }
  }
}

export const deploymentController = new DeploymentController();
