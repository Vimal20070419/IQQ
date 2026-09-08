export type UserRole = 'ADMIN' | 'RELEASE_MANAGER' | 'ON_CALL_ENGINEER' | 'VIEWER';

export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type DeploymentStatus = 'PENDING' | 'CANARY' | 'APPROVED' | 'HELD' | 'ROLLED_BACK' | 'DEGRADED';

export type DeploymentAction = 'APPROVE' | 'CANARY' | 'HOLD' | 'ROLLBACK';

export interface RiskFactor {
  factor: string;
  points: number;
  category: string;
  evidence: string;
}

export interface FailureChainStep {
  step: number;
  component: string;
  state: string;
  impact: string;
  evidence: string;
}

export interface BlastRadiusNode {
  id: string;
  name: string;
  tier: number; // 0 = direct, 1 = immediate downstream, 2 = cascading
  type: 'DIRECT' | 'DOWNSTREAM' | 'CRITICAL_DEPENDENCY';
  impactRisk: 'HIGH' | 'MEDIUM' | 'LOW';
  description: string;
}

export interface BlastRadiusEdge {
  from: string;
  to: string;
  connectionType: 'SYNC_HTTP' | 'ASYNC_QUEUE' | 'DATABASE' | 'CACHE';
}

export interface BlastRadiusData {
  directServicesCount: number;
  downstreamServicesCount: number;
  totalBlastRadiusCount: number;
  nodes: BlastRadiusNode[];
  edges: BlastRadiusEdge[];
}

export interface IncidentMatch {
  incidentId: string;
  title: string;
  service: string;
  similarityScore: number;
  rootCause: string;
  severity: string;
  resolution: string;
  matchingFactors: string[];
}

export interface HealthMetricSnapshot {
  errorRate: number; // e.g. 0.8 -> 7.4
  latencyMs: number; // e.g. 220 -> 780
  http5xxCount: number;
  cpuPercent: number;
  requestsPerSec: number;
  status: 'HEALTHY' | 'DEGRADED' | 'CRITICAL';
  recordedAt: string;
}

export interface HealthComparison {
  isDegraded: boolean;
  baseline: HealthMetricSnapshot;
  current: HealthMetricSnapshot;
  recommendation: DeploymentAction;
  triggerReason?: string;
}

export interface RollbackCheckResult {
  canRollback: boolean;
  currentVersion: string;
  targetVersion: string;
  targetVerified: boolean;
  hasDatabaseMigration: boolean;
  migrationWarning?: string;
  rollbackRiskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  checks: {
    name: string;
    passed: boolean;
    detail: string;
  }[];
}
