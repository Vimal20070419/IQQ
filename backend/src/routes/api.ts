import { Router } from 'express';
import { authController } from '../controllers/authController.js';
import { deploymentController } from '../controllers/deploymentController.js';
import { incidentController } from '../controllers/incidentController.js';
import { auditController } from '../controllers/auditController.js';
import { webhookController } from '../controllers/webhookController.js';
import { demoController } from '../controllers/demoController.js';
import { authenticate, requireRoles } from '../middleware/authMiddleware.js';

export const apiRouter = Router();

// --- AUTHENTICATION ---
apiRouter.post('/auth/login', (req, res) => authController.login(req, res));
apiRouter.get('/auth/demo-users', (req, res) => authController.getDemoUsers(req, res));

// --- WEBHOOKS (GitHub Actions) ---
apiRouter.post('/webhooks/github', (req, res) => webhookController.handleGitHubWebhook(req, res));

// --- DEPLOYMENTS ---
apiRouter.get('/deployments', authenticate, (req, res) => deploymentController.listDeployments(req, res));
apiRouter.get('/deployments/:id', authenticate, (req, res) => deploymentController.getDeploymentById(req, res));
apiRouter.get('/deployments/:id/risk', authenticate, (req, res) => deploymentController.getRiskAssessment(req, res));
apiRouter.get('/deployments/:id/evidence', authenticate, (req, res) => deploymentController.getEvidence(req, res));
apiRouter.get('/deployments/:id/blast-radius', authenticate, (req, res) => deploymentController.getBlastRadius(req, res));
apiRouter.get('/deployments/:id/failure-chain', authenticate, (req, res) => deploymentController.getFailureChain(req, res));
apiRouter.get('/deployments/:id/health', authenticate, (req, res) => deploymentController.getHealth(req, res));
apiRouter.get('/deployments/:id/rollback-guard', authenticate, (req, res) => deploymentController.getRollbackGuard(req, res));

// --- DEPLOYMENT ACTIONS (Authorized roles: ADMIN, RELEASE_MANAGER, ON_CALL_ENGINEER) ---
apiRouter.post(
  '/deployments/:id/approve',
  authenticate,
  requireRoles('ADMIN', 'RELEASE_MANAGER'),
  (req, res) => deploymentController.approveDeployment(req, res)
);

apiRouter.post(
  '/deployments/:id/canary',
  authenticate,
  requireRoles('ADMIN', 'RELEASE_MANAGER', 'ON_CALL_ENGINEER'),
  (req, res) => deploymentController.canaryDeployment(req, res)
);

apiRouter.post(
  '/deployments/:id/hold',
  authenticate,
  requireRoles('ADMIN', 'RELEASE_MANAGER', 'ON_CALL_ENGINEER'),
  (req, res) => deploymentController.holdDeployment(req, res)
);

apiRouter.post(
  '/deployments/:id/rollback',
  authenticate,
  requireRoles('ADMIN', 'RELEASE_MANAGER', 'ON_CALL_ENGINEER'),
  (req, res) => deploymentController.rollbackDeployment(req, res)
);

// --- HISTORICAL INCIDENTS ---
apiRouter.get('/incidents', authenticate, (req, res) => incidentController.listIncidents(req, res));
apiRouter.get('/incidents/:id', authenticate, (req, res) => incidentController.getIncidentById(req, res));

// --- AUDIT TRAIL ---
apiRouter.get('/audit', authenticate, (req, res) => auditController.getAuditLogs(req, res));

// --- HACKATHON LIVE DEMO HELPERS ---
apiRouter.post('/demo/deployment', (req, res) => demoController.triggerScenario(req, res));
apiRouter.post('/demo/simulate-health', (req, res) => demoController.simulateHealth(req, res));
apiRouter.post('/demo/reset', (req, res) => demoController.resetDemo(req, res));
