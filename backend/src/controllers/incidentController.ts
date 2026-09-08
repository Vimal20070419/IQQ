import { Request, Response } from 'express';
import { prisma } from '../database/db.js';

export class IncidentController {
  async listIncidents(req: Request, res: Response) {
    try {
      const incidents = await prisma.incident.findMany({
        orderBy: { timestamp: 'desc' }
      });
      return res.json(incidents);
    } catch (err: any) {
      return res.status(500).json({ error: err.message });
    }
  }

  async getIncidentById(req: Request, res: Response) {
    try {
      const { id } = req.params;
      const incident = await prisma.incident.findUnique({ where: { id } });
      if (!incident) return res.status(404).json({ error: 'Incident not found' });
      return res.json(incident);
    } catch (err: any) {
      return res.status(500).json({ error: err.message });
    }
  }
}

export const incidentController = new IncidentController();
