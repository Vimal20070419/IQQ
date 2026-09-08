import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { config } from '../config/env.js';
import { UserRole } from '../types/index.js';

export interface AuthRequest extends Request {
  user?: {
    id: string;
    email: string;
    name: string;
    role: UserRole;
  };
}

export function authenticate(req: AuthRequest, res: Response, next: NextFunction) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    // If demo mode or test client, allow fallback to demo user headers or mock token
    const demoRole = req.headers['x-demo-role'] as UserRole;
    if (demoRole) {
      const roleIdMap: Record<string, { id: string; name: string }> = {
        ADMIN: { id: 'usr-admin', name: 'Alex Mercer (Platform Admin)' },
        RELEASE_MANAGER: { id: 'usr-relmgr', name: 'Sarah Chen (Release Manager)' },
        ON_CALL_ENGINEER: { id: 'usr-oncall', name: 'David Miller (On-Call SRE)' },
        VIEWER: { id: 'usr-viewer', name: 'Elena Rostova (DevOps Observer)' }
      };
      const mapped = roleIdMap[demoRole] || { id: 'usr-relmgr', name: 'Sarah Chen (Release Manager)' };
      req.user = {
        id: mapped.id,
        email: `${demoRole.toLowerCase()}@shipsafe.dev`,
        name: mapped.name,
        role: demoRole
      };
      return next();
    }
    return res.status(401).json({ error: 'Unauthorized: Missing or invalid token' });
  }

  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, config.jwtSecret) as any;
    req.user = decoded;
    next();
  } catch (err) {
    return res.status(401).json({ error: 'Unauthorized: Invalid token' });
  }
}

export function requireRoles(...allowedRoles: UserRole[]) {
  return (req: AuthRequest, res: Response, next: NextFunction) => {
    if (!req.user) {
      return res.status(401).json({ error: 'Unauthorized' });
    }

    if (!allowedRoles.includes(req.user.role)) {
      return res.status(403).json({
        error: `Forbidden: Role '${req.user.role}' is not authorized to perform this deployment action. Required: ${allowedRoles.join(', ')}`
      });
    }

    next();
  };
}
