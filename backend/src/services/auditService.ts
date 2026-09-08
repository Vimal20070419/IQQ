import { prisma } from '../database/db.js';
import { logger } from '../utils/logger.js';

export interface RecordAuditParams {
  deploymentId?: string;
  userId?: string;
  userName: string;
  userRole: string;
  action: string;
  details: string;
  result: 'SUCCESS' | 'FAILED' | 'BLOCKED';
  ipAddress?: string;
  deviceModel?: string;
}

export class AuditService {
  async log(params: RecordAuditParams) {
    try {
      const record = await prisma.auditLog.create({
        data: {
          deploymentId: params.deploymentId,
          userId: params.userId,
          userName: params.userName,
          userRole: params.userRole,
          action: params.action,
          details: params.details,
          result: params.result,
          ipAddress: params.ipAddress || '127.0.0.1',
          deviceModel: params.deviceModel || 'iQOO 12 Pro (Android 14)'
        }
      });
      logger.info(`Audit Log: [${params.action}] by ${params.userName} (${params.userRole}) -> ${params.result}`);
      return record;
    } catch (err: any) {
      logger.error('Failed to write audit log', { error: err.message, params });
    }
  }

  async getRecentLogs(limit = 50) {
    return prisma.auditLog.findMany({
      orderBy: { timestamp: 'desc' },
      take: limit,
      include: {
        deployment: {
          select: {
            serviceName: true,
            version: true,
            environment: true
          }
        }
      }
    });
  }
}

export const auditService = new AuditService();
