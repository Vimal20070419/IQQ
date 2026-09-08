import { createServer } from './server.js';
import { config } from './config/env.js';
import { logger } from './utils/logger.js';
import { seedDatabase } from './database/seed.js';

async function bootstrap() {
  const app = createServer();

  // Auto-seed in development if database is clean
  try {
    await seedDatabase();
  } catch (err: any) {
    logger.warn('Seed database notice:', err.message);
  }

  app.listen(config.port, '0.0.0.0', () => {
    logger.info(`🚀 ShipSafe Backend running at http://0.0.0.0:${config.port}`);
    logger.info(`🛡️ Ready for Android connection & Live Demo presentation`);
  });
}

bootstrap().catch((err) => {
  logger.error('Failed to start ShipSafe server', err);
  process.exit(1);
});
