import dotenv from 'dotenv';
dotenv.config();

export const config = {
  port: parseInt(process.env.PORT || '3000', 10),
  nodeEnv: process.env.NODE_ENV || 'development',
  databaseUrl: process.env.DATABASE_URL || 'file:./dev.db',
  jwtSecret: process.env.JWT_SECRET || 'shipsafe_jwt_secret_super_safe_key_2026',
  openRouterApiKey: process.env.OPENROUTER_API_KEY || '',
  openRouterModel: process.env.OPENROUTER_MODEL || 'anthropic/claude-3.5-sonnet',
  githubWebhookSecret: process.env.GITHUB_WEBHOOK_SECRET || 'shipsafe_webhook_secret_123',
};
