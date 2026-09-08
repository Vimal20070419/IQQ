import { prisma } from '../database/db.js';
import { BlastRadiusData, BlastRadiusNode, BlastRadiusEdge } from '../types/index.js';

export class BlastRadiusEngine {
  /**
   * Traverse service dependency graph starting from target service
   */
  async computeBlastRadius(serviceName: string): Promise<BlastRadiusData> {
    const normalizedTarget = serviceName.toLowerCase().replace(/[\s_]+/g, '-');

    const allDependencies = await prisma.serviceDependency.findMany();

    const nodesMap = new Map<string, BlastRadiusNode>();
    const edges: BlastRadiusEdge[] = [];

    // Root node (target service)
    nodesMap.set(normalizedTarget, {
      id: normalizedTarget,
      name: this.formatServiceName(normalizedTarget),
      tier: 0,
      type: 'DIRECT',
      impactRisk: 'HIGH',
      description: 'Primary deployment target undergoing release'
    });

    // Tier 1 - Immediate Downstream & Upstream
    const tier1Targets = new Set<string>();
    for (const dep of allDependencies) {
      if (dep.sourceService === normalizedTarget) {
        tier1Targets.add(dep.targetService);
        edges.push({
          from: normalizedTarget,
          to: dep.targetService,
          connectionType: dep.dependencyType as any
        });
      } else if (dep.targetService === normalizedTarget) {
        // Upstream caller
        if (!nodesMap.has(dep.sourceService)) {
          nodesMap.set(dep.sourceService, {
            id: dep.sourceService,
            name: this.formatServiceName(dep.sourceService),
            tier: 1,
            type: 'CRITICAL_DEPENDENCY',
            impactRisk: 'HIGH',
            description: 'Direct upstream entry point dependent on target'
          });
        }
        edges.push({
          from: dep.sourceService,
          to: normalizedTarget,
          connectionType: dep.dependencyType as any
        });
      }
    }

    for (const t1 of tier1Targets) {
      if (!nodesMap.has(t1)) {
        nodesMap.set(t1, {
          id: t1,
          name: this.formatServiceName(t1),
          tier: 1,
          type: 'DOWNSTREAM',
          impactRisk: 'HIGH',
          description: 'Direct downstream consumer'
        });
      }
    }

    // Tier 2 - Cascading Downstream
    for (const dep of allDependencies) {
      if (tier1Targets.has(dep.sourceService) && dep.targetService !== normalizedTarget) {
        if (!nodesMap.has(dep.targetService)) {
          nodesMap.set(dep.targetService, {
            id: dep.targetService,
            name: this.formatServiceName(dep.targetService),
            tier: 2,
            type: 'DOWNSTREAM',
            impactRisk: 'MEDIUM',
            description: 'Cascading downstream dependency'
          });
        }
        edges.push({
          from: dep.sourceService,
          to: dep.targetService,
          connectionType: dep.dependencyType as any
        });
      }
    }

    // Fallback if no db links found
    if (nodesMap.size === 1) {
      return {
        directServicesCount: 1,
        downstreamServicesCount: 0,
        totalBlastRadiusCount: 1,
        nodes: Array.from(nodesMap.values()),
        edges: []
      };
    }

    const nodes = Array.from(nodesMap.values());
    const downstreamCount = nodes.filter(n => n.type === 'DOWNSTREAM').length;

    return {
      directServicesCount: 1,
      downstreamServicesCount: downstreamCount,
      totalBlastRadiusCount: nodes.length,
      nodes,
      edges
    };
  }

  private formatServiceName(raw: string): string {
    return raw
      .split('-')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }
}

export const blastRadiusEngine = new BlastRadiusEngine();
