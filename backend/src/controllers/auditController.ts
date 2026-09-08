import { Request, Response } from 'express';
import { auditService } from '../services/auditService.js';

export class AuditController {
  async getAuditLogs(req: Request, res: Response) {
    try {
      const logs = await auditService.getRecentLogs(50);
      return res.json(logs);
    } catch (err: any) {
      return res.status(500).json({ error: err.message });
    }
  }
}

export const auditController = new AuditController();
