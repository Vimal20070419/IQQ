import { Request, Response, NextFunction } from 'express';
import { logger } from '../utils/logger.js';

export function errorHandler(err: any, req: Request, res: Response, next: NextFunction) {
  logger.error(`Unhandled error on ${req.method} ${req.url}: ${err.message}`, { stack: err.stack });

  const statusCode = err.statusCode || 500;
  res.status(statusCode).json({
    error: err.message || 'Internal Server Error',
    timestamp: new Date().toISOString()
  });
}
