import express from 'express';
import cors from 'cors';
import fs from 'fs';
import path from 'path';
import { apiRouter } from './routes/api.js';
import { errorHandler } from './middleware/errorHandler.js';
import { logger } from './utils/logger.js';

export function createServer() {
  const app = express();

  app.use(cors());
  app.use(express.json());

  // Request logger
  app.use((req, res, next) => {
    logger.info(`${req.method} ${req.url}`);
    next();
  });

  // Health check endpoint
  app.get('/health', (req, res) => {
    res.json({ status: 'ok', service: 'ShipSafe Backend', timestamp: new Date().toISOString() });
  });

  // Resolve public directory across local and serverless Vercel environments
  const possiblePublicDirs = [
    path.join(process.cwd(), 'public'),
    path.join(process.cwd(), 'backend', 'public'),
    path.join(__dirname, '../public'),
    path.join(__dirname, '../../public'),
    path.join(__dirname, '../../backend/public')
  ];

  const publicDir = possiblePublicDirs.find(d => fs.existsSync(path.join(d, 'index.html'))) || possiblePublicDirs[0];

  if (fs.existsSync(publicDir)) {
    app.use(express.static(publicDir));
  }

  // Root Web Portal
  app.get('/', (req, res) => {
    const indexPath = path.join(publicDir, 'index.html');
    if (fs.existsSync(indexPath)) {
      return res.sendFile(indexPath);
    }
    return res.json({
      service: 'ShipSafe - AI-Powered Deployment Safety Platform',
      status: 'online',
      version: '1.0.0',
      endpoints: {
        health: '/health',
        deployments: '/api/deployments',
        login: '/api/auth/login',
        audit: '/api/audit'
      }
    });
  });

  // REST API root
  app.use('/api', apiRouter);

  // Global error handler
  app.use(errorHandler);

  return app;
}
