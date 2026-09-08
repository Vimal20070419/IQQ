import { z } from 'zod';

export const loginSchema = z.object({
  email: z.string().email(),
  password: z.string().min(4),
});

export const decisionActionSchema = z.object({
  action: z.enum(['APPROVE', 'CANARY', 'HOLD', 'ROLLBACK']),
  trafficPct: z.number().min(1).max(100).optional(),
  reason: z.string().min(2),
  deviceModel: z.string().optional(),
});

export const simulateHealthSchema = z.object({
  deploymentId: z.string(),
  degrade: z.boolean().default(false),
  errorRate: z.number().optional(),
  latencyMs: z.number().optional(),
});

export const demoScenarioSchema = z.object({
  scenarioId: z.enum(['low-risk-docs', 'medium-risk-api', 'high-risk-payment', 'hero-demo']),
});
