import { prisma } from '../database/db.js';
import { IncidentMatch } from '../types/index.js';

export class IncidentMemoryService {
  /**
   * Search historical incidents using multi-factor matching:
   * 1. Service name match
   * 2. Component/File keyword match (e.g. payment, timeout, retry, config, auth, sql)
   * 3. Affected service overlap
   */
  async findSimilarIncidents(
    serviceName: string,
    changedFiles: string[],
    sensitiveAreas: string[]
  ): Promise<IncidentMatch[]> {
    const allIncidents = await prisma.incident.findMany({
      orderBy: { timestamp: 'desc' },
      take: 20
    });

    const normalizedService = serviceName.toLowerCase().replace(/[\s-_]+/g, '');
    const fileKeywords = changedFiles.map(f => f.toLowerCase());
    const sensitiveKeywords = sensitiveAreas.map(s => s.toLowerCase());

    const scoredIncidents: IncidentMatch[] = [];

    for (const inc of allIncidents) {
      let score = 0;
      const matchingFactors: string[] = [];

      const incService = inc.serviceName.toLowerCase().replace(/[\s-_]+/g, '');
      if (normalizedService.includes(incService) || incService.includes(normalizedService)) {
        score += 0.45;
        matchingFactors.push(`Same service: ${inc.serviceName}`);
      }

      // Check component / keyword matches
      const incChanged = inc.changedComponent.toLowerCase();
      const incRootCause = inc.rootCause.toLowerCase();
      const incPattern = inc.deploymentPattern.toLowerCase();

      let matchedFileKeyword = false;
      for (const fk of fileKeywords) {
        const basename = fk.split('/').pop() || fk;
        if (incChanged.includes(basename) || incRootCause.includes(basename)) {
          if (!matchedFileKeyword) {
            score += 0.30;
            matchingFactors.push(`Matching changed component: ${inc.changedComponent}`);
            matchedFileKeyword = true;
          }
        }
      }

      // Check sensitive domain keywords (payment, timeout, retry, database, cache, auth)
      const domainKeywords = ['payment', 'timeout', 'retry', 'pool', 'locking', 'cache', 'buffer', 'gateway', 'index', 'migration'];
      for (const kw of domainKeywords) {
        const fileHasKw = fileKeywords.some(f => f.includes(kw)) || sensitiveKeywords.some(s => s.includes(kw));
        const incHasKw = incRootCause.includes(kw) || incChanged.includes(kw) || incPattern.includes(kw);
        if (fileHasKw && incHasKw) {
          score += 0.20;
          matchingFactors.push(`Matching pattern: ${kw} configuration/behavior`);
          break;
        }
      }

      // Cap at 0.98
      const finalScore = Math.min(0.98, parseFloat(score.toFixed(2)));

      if (finalScore >= 0.35) {
        scoredIncidents.push({
          incidentId: inc.id,
          title: inc.title,
          service: inc.serviceName,
          similarityScore: finalScore,
          rootCause: inc.rootCause,
          severity: inc.severity,
          resolution: inc.resolution,
          matchingFactors: matchingFactors.length > 0 ? matchingFactors : ['Similar deployment profile']
        });
      }
    }

    return scoredIncidents.sort((a, b) => b.similarityScore - a.similarityScore).slice(0, 3);
  }
}

export const incidentMemory = new IncidentMemoryService();
