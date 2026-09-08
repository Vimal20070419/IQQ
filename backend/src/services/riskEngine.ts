import { RiskFactor, RiskLevel, DeploymentAction } from '../types/index.js';

export interface RawDeploymentData {
  serviceName: string;
  environment: string;
  filesChanged: number;
  linesAdded: number;
  linesDeleted: number;
  databaseMigration: boolean;
  files: { filePath: string; category: string; linesChanged: number; isSensitive: boolean }[];
  similarIncidentsCount: number;
  highestIncidentSimilarity: number;
  downstreamCount: number;
}

export interface RiskEngineResult {
  riskLevel: RiskLevel;
  riskScore: number;
  recommendation: DeploymentAction;
  factors: RiskFactor[];
  evidence: string[];
}

export class RiskEngine {
  /**
   * Transparent risk calculator that scores contributing factors:
   * 1. Sensitive Components (+20 for payment/auth/database)
   * 2. Historical Incident Match (+18 for >70% match)
   * 3. Blast Radius / Downstream Dependencies (+16 for >3 services)
   * 4. Diff Size / Code Volatility (+15 for >150 lines)
   * 5. Environment Factor (+15 for production)
   * 6. Database Migration (+12 if schema changed)
   */
  calculateRisk(data: RawDeploymentData): RiskEngineResult {
    let score = 0;
    const factors: RiskFactor[] = [];
    const evidence: string[] = [];

    const totalLines = data.linesAdded + data.linesDeleted;

    // 1. Sensitive Area Check
    const sensitiveFiles = data.files.filter(f => f.isSensitive || ['PAYMENT', 'AUTH', 'DATABASE', 'CONFIG'].includes(f.category));
    if (sensitiveFiles.length > 0) {
      const points = 20;
      score += points;
      const categories = Array.from(new Set(sensitiveFiles.map(f => f.category))).join(', ');
      factors.push({
        factor: `${categories} configuration / logic modified`,
        points,
        category: 'SENSITIVE_CODE',
        evidence: `${sensitiveFiles.length} sensitive file(s) touched (${sensitiveFiles.map(f => f.filePath.split('/').pop()).join(', ')})`
      });
      evidence.push(`Sensitive component (${categories}) touched in ${sensitiveFiles.length} files`);
    }

    // 2. Historical Incident Check
    if (data.similarIncidentsCount > 0 && data.highestIncidentSimilarity >= 0.5) {
      const points = Math.min(20, Math.round(data.highestIncidentSimilarity * 20));
      score += points;
      factors.push({
        factor: `Similar historical incident found (${Math.round(data.highestIncidentSimilarity * 100)}% match)`,
        points,
        category: 'INCIDENT_HISTORY',
        evidence: `Matches past failure pattern in ${data.serviceName}`
      });
      evidence.push(`Found ${data.similarIncidentsCount} past similar incident(s) with up to ${Math.round(data.highestIncidentSimilarity * 100)}% similarity`);
    }

    // 3. Blast Radius / Downstream
    if (data.downstreamCount > 0) {
      const points = Math.min(18, 10 + data.downstreamCount * 2);
      score += points;
      factors.push({
        factor: `Critical downstream dependencies (${data.downstreamCount} services)`,
        points,
        category: 'BLAST_RADIUS',
        evidence: `Direct failure propagation to ${data.downstreamCount} downstream microservice(s)`
      });
      evidence.push(`${data.downstreamCount} downstream services in direct blast radius`);
    }

    // 4. Diff Size
    if (totalLines > 150 || data.filesChanged > 5) {
      const points = 15;
      score += points;
      factors.push({
        factor: `Substantial code diff in core logic (${totalLines} lines)`,
        points,
        category: 'DIFF_SIZE',
        evidence: `${totalLines} lines modified across ${data.filesChanged} files`
      });
      evidence.push(`${totalLines} lines changed across ${data.filesChanged} files in release payload`);
    } else if (totalLines > 40) {
      const points = 8;
      score += points;
      factors.push({
        factor: `Moderate code diff (${totalLines} lines)`,
        points,
        category: 'DIFF_SIZE',
        evidence: `${totalLines} lines changed`
      });
    }

    // 5. Environment
    if (data.environment.toLowerCase() === 'production') {
      const points = 15;
      score += points;
      factors.push({
        factor: 'Target is live Production environment',
        points,
        category: 'ENVIRONMENT',
        evidence: 'Direct impact on live user transactions with 100% traffic exposure'
      });
      evidence.push('Production deployment with live traffic exposure');
    }

    // 6. Database Migration
    if (data.databaseMigration) {
      const points = 12;
      score += points;
      factors.push({
        factor: 'Database schema migration included',
        points,
        category: 'DATABASE_MIGRATION',
        evidence: 'Requires schema lock and backwards compatibility verification'
      });
      evidence.push('Contains database schema migration');
    } else {
      evidence.push('No database migration required (Safe Rollback supported)');
    }

    // Clamp score between 0 and 100
    const finalScore = Math.min(100, Math.max(5, score));

    // Determine Risk Level & Recommendation
    let riskLevel: RiskLevel = 'LOW';
    let recommendation: DeploymentAction = 'APPROVE';

    if (finalScore >= 75) {
      riskLevel = 'HIGH';
      recommendation = 'CANARY'; // Or HOLD if > 90
    } else if (finalScore >= 45) {
      riskLevel = 'MEDIUM';
      recommendation = 'CANARY';
    } else {
      riskLevel = 'LOW';
      recommendation = 'APPROVE';
    }

    return {
      riskLevel,
      riskScore: finalScore,
      recommendation,
      factors,
      evidence
    };
  }
}

export const riskEngine = new RiskEngine();
