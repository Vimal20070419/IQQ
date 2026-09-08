import express from 'express';
import cors from 'cors';
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

  // Serve static interactive console & mobile demo
  app.use(express.static('public'));

  // REST API root
  app.use('/api', apiRouter);

  // Global error handler
  app.use(errorHandler);

  return app;
}
