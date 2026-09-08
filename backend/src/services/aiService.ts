import { config } from '../config/env.js';
import { logger } from '../utils/logger.js';
import { RiskEngineResult } from './riskEngine.js';
import { IncidentMatch, FailureChainStep } from '../types/index.js';

export interface AISynthesisInput {
  deployment: string;
  version: string;
  environment: string;
  filesChanged: number;
  linesChanged: number;
  sensitiveAreas: string[];
  affectedServices: string[];
  historicalIncidents: IncidentMatch[];
  failureChain: FailureChainStep[];
  deterministicResult: RiskEngineResult;
}

export interface AISynthesisOutput {
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  riskScore: number;
  summary: string;
  recommendation: 'APPROVE' | 'CANARY' | 'HOLD' | 'ROLLBACK';
  affectedServices: string[];
  factors: any[];
  failureChain: FailureChainStep[];
  evidence: string[];
  isAiGenerated: boolean;
  confidence: number;
}

export class AIService {
  async synthesizeRiskAssessment(input: AISynthesisInput): Promise<AISynthesisOutput> {
    // If no OpenRouter API key, use deterministic fallback immediately
    if (!config.openRouterApiKey || config.openRouterApiKey.trim() === '') {
      logger.info('OpenRouter API key not configured. Using grounded deterministic risk synthesis.');
      return this.generateDeterministicSynthesis(input);
    }

    try {
      const prompt = `You are ShipSafe AI, an evidence-grounded deployment safety evaluator for SRE and DevOps engineers.
Analyze the following deployment evidence and return ONLY a valid JSON object.

RULES:
1. Do NOT invent new incidents, services, or metrics. Ground everything strictly in the provided evidence.
2. Label recommendations clearly (APPROVE, CANARY, HOLD).
3. Do NOT claim the AI predicts the future; describe potential failure chains.

EVIDENCE:
${JSON.stringify(input, null, 2)}

Respond with ONLY this JSON schema:
{
  "riskLevel": "LOW" | "MEDIUM" | "HIGH" | "CRITICAL",
  "riskScore": number (0-100),
  "summary": string (concise 1-2 sentence executive assessment),
  "recommendation": "APPROVE" | "CANARY" | "HOLD" | "ROLLBACK",
  "affectedServices": string[],
  "evidence": string[]
}`;

      const response = await fetch('https://openrouter.ai/api/v1/chat/completions', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${config.openRouterApiKey}`,
          'Content-Type': 'application/json',
          'HTTP-Referer': 'https://shipsafe.dev',
          'X-Title': 'ShipSafe Deployment Safety'
        },
        body: JSON.stringify({
          model: config.openRouterModel,
          messages: [{ role: 'user', content: prompt }],
          temperature: 0.1,
          response_format: { type: 'json_object' }
        }),
        signal: AbortSignal.timeout(6000) // 6s timeout
      });

      if (!response.ok) {
        throw new Error(`OpenRouter HTTP ${response.status}: ${await response.text()}`);
      }

      const json = (await response.json()) as any;
      const content = json.choices?.[0]?.message?.content;
      const parsed = JSON.parse(content);

      return {
        riskLevel: parsed.riskLevel || input.deterministicResult.riskLevel,
        riskScore: typeof parsed.riskScore === 'number' ? parsed.riskScore : input.deterministicResult.riskScore,
        summary: parsed.summary || this.generateDefaultSummary(input),
        recommendation: parsed.recommendation || input.deterministicResult.recommendation,
        affectedServices: parsed.affectedServices || input.affectedServices,
        factors: input.deterministicResult.factors,
        failureChain: input.failureChain,
        evidence: parsed.evidence || input.deterministicResult.evidence,
        isAiGenerated: true,
        confidence: 0.94
      };
    } catch (err: any) {
      logger.warn(`AI synthesis fallback triggered: ${err.message}. Using deterministic risk engine.`);
      return this.generateDeterministicSynthesis(input);
    }
  }

  private generateDeterministicSynthesis(input: AISynthesisInput): AISynthesisOutput {
    return {
      riskLevel: input.deterministicResult.riskLevel,
      riskScore: input.deterministicResult.riskScore,
      summary: this.generateDefaultSummary(input),
      recommendation: input.deterministicResult.recommendation,
      affectedServices: input.affectedServices,
      factors: input.deterministicResult.factors,
      failureChain: input.failureChain,
      evidence: input.deterministicResult.evidence,
      isAiGenerated: false,
      confidence: 0.96
    };
  }

  private generateDefaultSummary(input: AISynthesisInput): string {
    if (input.historicalIncidents.length > 0) {
      const topInc = input.historicalIncidents[0];
      return `${input.sensitiveAreas.join(', ')} modified in ${input.environment}. Similar historical incident (${topInc.incidentId}) previously caused outages across ${input.affectedServices.length} downstream services.`;
    }
    if (input.deterministicResult.riskLevel === 'LOW') {
      return `Low-impact release with isolated changes in ${input.deployment}. Safe for direct standard rollout.`;
    }
    return `Moderate risk changes touching ${input.filesChanged} file(s) in ${input.deployment}. Recommended gradual canary traffic ramp.`;
  }
}

export const aiService = new AIService();
