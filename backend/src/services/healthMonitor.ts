import { prisma } from '../database/db.js';
import { HealthComparison, HealthMetricSnapshot } from '../types/index.js';

export class HealthMonitorService {
  /**
   * Get current health metrics and comparison against baseline
   */
  async getDeploymentHealth(deploymentId: string): Promise<HealthComparison> {
    const latestMetrics = await prisma.deploymentHealth.findMany({
      where: { deploymentId },
      orderBy: { recordedAt: 'desc' },
      take: 2
    });

    if (latestMetrics.length === 0) {
      // Return default baseline
      const defaultBaseline: HealthMetricSnapshot = {
        errorRate: 0.8,
        latencyMs: 220,
        http5xxCount: 12,
        cpuPercent: 61.2,
        requestsPerSec: 12420,
        status: 'HEALTHY',
        recordedAt: new Date().toISOString()
      };

      return {
        isDegraded: false,
        baseline: defaultBaseline,
        current: defaultBaseline,
        recommendation: 'CANARY'
      };
    }

    const currentRecord = latestMetrics[0];
    const baselineRecord = latestMetrics.length > 1 ? latestMetrics[latestMetrics.length - 1] : latestMetrics[0];

    const current: HealthMetricSnapshot = {
      errorRate: currentRecord.errorRate,
      latencyMs: currentRecord.latencyMs,
      http5xxCount: currentRecord.http5xxCount,
      cpuPercent: currentRecord.cpuPercent,
      requestsPerSec: currentRecord.requestsPerSec,
      status: currentRecord.status as any,
      recordedAt: currentRecord.recordedAt.toISOString()
    };

    const baseline: HealthMetricSnapshot = {
      errorRate: baselineRecord.errorRate,
      latencyMs: baselineRecord.latencyMs,
      http5xxCount: baselineRecord.http5xxCount,
      cpuPercent: baselineRecord.cpuPercent,
      requestsPerSec: baselineRecord.requestsPerSec,
      status: baselineRecord.status as any,
      recordedAt: baselineRecord.recordedAt.toISOString()
    };

    const isDegraded = current.errorRate > 3.0 || current.latencyMs > 500 || current.status === 'DEGRADED';

    return {
      isDegraded,
      baseline,
      current,
      recommendation: isDegraded ? 'ROLLBACK' : 'CANARY',
      triggerReason: isDegraded ? `Error rate elevated to ${current.errorRate}% (baseline: ${baseline.errorRate}%), Latency ${current.latencyMs}ms (baseline: ${baseline.latencyMs}ms)` : undefined
    };
  }

  /**
   * Simulate telemetry update or degradation trigger for hackathon demo
   */
  async recordHealthMetric(
    deploymentId: string,
    errorRate: number,
    latencyMs: number,
    http5xxCount: number,
    cpuPercent: number,
    requestsPerSec: number,
    status: 'HEALTHY' | 'DEGRADED' | 'CRITICAL'
  ) {
    const record = await prisma.deploymentHealth.create({
      data: {
        deploymentId,
        errorRate,
        latencyMs,
        http5xxCount,
        cpuPercent,
        requestsPerSec,
        status
      }
    });

    if (status === 'DEGRADED' || status === 'CRITICAL') {
      await prisma.deployment.update({
        where: { id: deploymentId },
        data: { status: 'DEGRADED' }
      });
    }

    return record;
  }

  /**
   * Helper to trigger the 60-90s Hero Demo degradation
   */
  async triggerDemoDegradation(deploymentId: string) {
    return this.recordHealthMetric(
      deploymentId,
      7.4,   // 0.8% -> 7.4%
      780,   // 220ms -> 780ms
      184,   // 12 -> 184
      88.4,  // 61.2% -> 88.4%
      11200,
      'DEGRADED'
    );
  }

  /**
   * Helper to reset to normal healthy metrics
   */
  async resetDemoHealth(deploymentId: string) {
    return this.recordHealthMetric(
      deploymentId,
      0.8,
      220,
      12,
      61.2,
      12420,
      'HEALTHY'
    );
  }
}

export const healthMonitor = new HealthMonitorService();
