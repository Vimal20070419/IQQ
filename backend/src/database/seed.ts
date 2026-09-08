import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcryptjs';

const prisma = new PrismaClient();

export async function seedDatabase() {
  console.log('🌱 Starting ShipSafe Database Seeding...');

  // 1. Clean existing tables in reverse dependency order
  await prisma.auditLog.deleteMany();
  await prisma.deploymentHealth.deleteMany();
  await prisma.deploymentDecision.deleteMany();
  await prisma.riskAssessment.deleteMany();
  await prisma.deploymentFile.deleteMany();
  await prisma.deployment.deleteMany();
  await prisma.serviceDependency.deleteMany();
  await prisma.incident.deleteMany();
  await prisma.user.deleteMany();

  // 2. Seed Users
  const passwordHash = await bcrypt.hash('shipsafe2026', 10);
  
  const users = [
    {
      id: 'usr-admin',
      email: 'admin@shipsafe.dev',
      name: 'Alex Mercer (Platform Admin)',
      passwordHash,
      role: 'ADMIN',
      avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150'
    },
    {
      id: 'usr-relmgr',
      email: 'release@shipsafe.dev',
      name: 'Sarah Chen (Release Manager)',
      passwordHash,
      role: 'RELEASE_MANAGER',
      avatar: 'https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150'
    },
    {
      id: 'usr-oncall',
      email: 'oncall@shipsafe.dev',
      name: 'David Miller (On-Call SRE)',
      passwordHash,
      role: 'ON_CALL_ENGINEER',
      avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150'
    },
    {
      id: 'usr-viewer',
      email: 'viewer@shipsafe.dev',
      name: 'Elena Rostova (DevOps Observer)',
      passwordHash,
      role: 'VIEWER',
      avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150'
    }
  ];

  for (const user of users) {
    await prisma.user.create({ data: user });
  }
  console.log(`✅ Seeded ${users.length} Users`);

  // 3. Seed 18 Realistic SRE/DevOps Incidents
  const incidents = [
    {
      id: 'INC-017',
      title: 'Checkout outage after payment configuration update',
      serviceName: 'checkout-service',
      rootCause: 'Incorrect timeout & retry configuration on Stripe connector caused cascade thread starvation',
      changedComponent: 'payment.config.ts and retryPolicy.ts',
      affectedServices: 'checkout-service, payment-service, order-service, inventory-service',
      severity: 'SEV-1',
      resolution: 'Rolled back to previous version v2.8.3, restored default 2000ms circuit breaker timeout',
      deploymentPattern: 'Payment config modifications with downstream sync HTTP calls',
      timestamp: new Date(Date.now() - 14 * 24 * 3600 * 1000),
      lessonsLearned: 'Always test circuit breaker backoff and use Canary for payment gateway config changes.'
    },
    {
      id: 'INC-016',
      title: 'Database connection pool exhaustion on order spike',
      serviceName: 'order-service',
      rootCause: 'Unindexed query added to order history retrieval under high concurrency',
      changedComponent: 'orderRepository.ts and schema.prisma',
      affectedServices: 'order-service, notification-service, analytics-service',
      severity: 'SEV-1',
      resolution: 'Added composite index on (user_id, created_at) and increased Prisma pool size',
      deploymentPattern: 'Database query schema changes without index benchmarking',
      timestamp: new Date(Date.now() - 28 * 24 * 3600 * 1000),
      lessonsLearned: 'Mandatory explain query review for any ORM query changes.'
    },
    {
      id: 'INC-015',
      title: 'Authentication token validation latency spike',
      serviceName: 'auth-service',
      rootCause: 'Synchronous JWKS key rotation cache miss caused remote key fetch for every request',
      changedComponent: 'jwtValidator.ts',
      affectedServices: 'api-gateway, auth-service, checkout-service, user-service',
      severity: 'SEV-2',
      resolution: 'Implemented local in-memory JWKS cache with 24-hour TTL and stale-while-revalidate',
      deploymentPattern: 'Crypto/Auth library upgrade without local caching',
      timestamp: new Date(Date.now() - 40 * 24 * 3600 * 1000),
      lessonsLearned: 'Ensure JWKS public keys are cached asynchronously in Redis.'
    },
    {
      id: 'INC-014',
      title: 'Inventory over-allocation during flash sale',
      serviceName: 'inventory-service',
      rootCause: 'Pessimistic locking replaced with optimistic locking without distributed redis mutex',
      changedComponent: 'stockReservation.ts',
      affectedServices: 'inventory-service, order-service, warehouse-service',
      severity: 'SEV-1',
      resolution: 'Restored Redis Redlock distributed lock for stock reservation operations',
      deploymentPattern: 'Concurrency locking algorithm modifications',
      timestamp: new Date(Date.now() - 55 * 24 * 3600 * 1000),
      lessonsLearned: 'High-contention SKU reservation requires distributed locks.'
    },
    {
      id: 'INC-013',
      title: 'Notification queue backup due to webhook backpressure',
      serviceName: 'notification-service',
      rootCause: 'Downstream SMS provider rate limiting blocked RabbitMQ consumers',
      changedComponent: 'smsSender.ts and rabbitmqConsumer.ts',
      affectedServices: 'notification-service, auth-service, order-service',
      severity: 'SEV-2',
      resolution: 'Added token bucket rate limiter and dead-letter queue for delayed retries',
      deploymentPattern: 'External third-party API integration without client-side rate limits',
      timestamp: new Date(Date.now() - 70 * 24 * 3600 * 1000),
      lessonsLearned: 'Decouple consumer processing from third party webhook latency.'
    },
    {
      id: 'INC-012',
      title: 'API Gateway 502 Bad Gateway under peak traffic',
      serviceName: 'api-gateway',
      rootCause: 'Nginx upstream keepalive connection pool maxed out after buffer size reduction',
      changedComponent: 'gateway-proxy.conf',
      affectedServices: 'api-gateway, checkout-service, product-catalog, search-service',
      severity: 'SEV-1',
      resolution: 'Increased keepalive connections from 32 to 512 per upstream pod',
      deploymentPattern: 'Reverse proxy networking buffer and socket tuning',
      timestamp: new Date(Date.now() - 85 * 24 * 3600 * 1000),
      lessonsLearned: 'Load test gateway configuration changes before staging rollout.'
    },
    {
      id: 'INC-011',
      title: 'Search indexing lag causing stale catalog results',
      serviceName: 'search-service',
      rootCause: 'Elasticsearch bulk update chunk size increased from 500 to 5000 causing GC pauses',
      changedComponent: 'esIndexer.ts',
      affectedServices: 'search-service, product-catalog',
      severity: 'SEV-3',
      resolution: 'Reverted bulk batch size to 500 documents and adjusted Java heap JVM params',
      deploymentPattern: 'Search cluster indexing batch size changes',
      timestamp: new Date(Date.now() - 95 * 24 * 3600 * 1000),
      lessonsLearned: 'Monitor JVM garbage collection time during heavy batch ingestion.'
    },
    {
      id: 'INC-010',
      title: 'Payment gateway double charge on network retry',
      serviceName: 'payment-service',
      rootCause: 'Missing idempotency key on merchant API retry logic',
      changedComponent: 'paymentGatewayClient.ts',
      affectedServices: 'payment-service, checkout-service, accounting-service',
      severity: 'SEV-1',
      resolution: 'Enforced unique UUIDv4 idempotency key header on all outgoing payment attempts',
      deploymentPattern: 'Payment gateway API client modifications',
      timestamp: new Date(Date.now() - 110 * 24 * 3600 * 1000),
      lessonsLearned: 'All payment mutation requests must supply an Idempotency-Key.'
    },
    {
      id: 'INC-009',
      title: 'User profile avatar upload service denial of service',
      serviceName: 'user-service',
      rootCause: 'Uncapped image compression memory buffer allocated in-process',
      changedComponent: 'avatarProcessor.ts',
      affectedServices: 'user-service',
      severity: 'SEV-2',
      resolution: 'Offloaded image resizing to serverless Lambda worker with S3 trigger',
      deploymentPattern: 'High CPU/Memory media processing inside core web app',
      timestamp: new Date(Date.now() - 120 * 24 * 3600 * 1000),
      lessonsLearned: 'Never process binary media uploads in main web threads.'
    },
    {
      id: 'INC-008',
      title: 'Pricing discrepancy due to Redis cache deserialization bug',
      serviceName: 'pricing-service',
      rootCause: 'Floating point rounding drift introduced during JSON to protobuf serialization',
      changedComponent: 'priceFormatter.ts',
      affectedServices: 'pricing-service, checkout-service, product-catalog',
      severity: 'SEV-1',
      resolution: 'Switched pricing values to integer cents/micro-units everywhere',
      deploymentPattern: 'Serialization format transition across microservice boundaries',
      timestamp: new Date(Date.now() - 135 * 24 * 3600 * 1000),
      lessonsLearned: 'Store and transmit money in smallest currency integer units.'
    },
    {
      id: 'INC-007',
      title: 'Recommendation engine cold start latency after cache eviction',
      serviceName: 'recommendation-service',
      rootCause: 'Redis LRU eviction policy altered during memory optimization rollout',
      changedComponent: 'redis.conf',
      affectedServices: 'recommendation-service, home-feed-service',
      severity: 'SEV-3',
      resolution: 'Reinstated volatile-lru and increased Redis cluster memory allocation',
      deploymentPattern: 'Cache server configuration modifications',
      timestamp: new Date(Date.now() - 150 * 24 * 3600 * 1000),
      lessonsLearned: 'Audit cache eviction metrics before altering maxmemory policies.'
    },
    {
      id: 'INC-006',
      title: 'Shipping rate calculation timeout during checkout',
      serviceName: 'shipping-service',
      rootCause: 'FedEx API rate limit reached during international parcel validation',
      changedComponent: 'carrierIntegration.ts',
      affectedServices: 'shipping-service, checkout-service, order-service',
      severity: 'SEV-2',
      resolution: 'Added cached fallback shipping tables and circuit breaker pattern',
      deploymentPattern: 'Synchronous external carrier API calls during checkout',
      timestamp: new Date(Date.now() - 165 * 24 * 3600 * 1000),
      lessonsLearned: 'Always provide offline estimated shipping rate fallback.'
    },
    {
      id: 'INC-005',
      title: 'Email receipt delivery stalled due to SMTP connection leak',
      serviceName: 'email-service',
      rootCause: 'Missing socket close in exception handling block of mailer worker',
      changedComponent: 'smtpPool.ts',
      affectedServices: 'email-service, notification-service',
      severity: 'SEV-3',
      resolution: 'Wrapped socket lifecycle in try-finally ensuring connection disposal',
      deploymentPattern: 'Raw TCP socket handling in async workers',
      timestamp: new Date(Date.now() - 180 * 24 * 3600 * 1000),
      lessonsLearned: 'Use established pooling libraries with automatic leak detection.'
    },
    {
      id: 'INC-004',
      title: 'Cart abandonment spike caused by stale session cookie migration',
      serviceName: 'cart-service',
      rootCause: 'SameSite cookie policy changed to Strict without subdomain cross-domain handling',
      changedComponent: 'cookieMiddleware.ts',
      affectedServices: 'cart-service, checkout-service, api-gateway',
      severity: 'SEV-2',
      resolution: 'Updated cookie SameSite to Lax with Secure flag on HTTPS',
      deploymentPattern: 'Security header and session cookie policy updates',
      timestamp: new Date(Date.now() - 195 * 24 * 3600 * 1000),
      lessonsLearned: 'Validate cookie propagation across multiple frontend subdomains.'
    },
    {
      id: 'INC-003',
      title: 'Promotional discount code race condition causing negative totals',
      serviceName: 'promo-service',
      rootCause: 'Coupon validation and order checkout deduction was non-atomic',
      changedComponent: 'discountCalculator.ts',
      affectedServices: 'promo-service, checkout-service, payment-service',
      severity: 'SEV-1',
      resolution: 'Combined promo coupon claim and checkout total calculation in atomic SQL transaction',
      deploymentPattern: 'Multi-step financial calculations without transactional isolation',
      timestamp: new Date(Date.now() - 210 * 24 * 3600 * 1000),
      lessonsLearned: 'Financial deductions must be strictly serializable.'
    },
    {
      id: 'INC-002',
      title: 'Analytics pipeline backpressure crashed Kafka consumer group',
      serviceName: 'analytics-service',
      rootCause: 'Clickhouse batch ingestion timeout forced consumer rebalances',
      changedComponent: 'kafkaConsumer.ts',
      affectedServices: 'analytics-service',
      severity: 'SEV-3',
      resolution: 'Increased max.poll.interval.ms and split batch payloads into smaller micro-batches',
      deploymentPattern: 'Kafka consumer poll timeout tuning',
      timestamp: new Date(Date.now() - 230 * 24 * 3600 * 1000),
      lessonsLearned: 'Keep batch processing time well within Kafka poll heartbeat timeouts.'
    },
    {
      id: 'INC-001',
      title: 'GraphQL gateway schema mismatch breaking iOS/Android apps',
      serviceName: 'graphql-gateway',
      rootCause: 'Removed deprecated field without required 6-month deprecation lifecycle',
      changedComponent: 'schema.graphql',
      affectedServices: 'graphql-gateway, mobile-clients',
      severity: 'SEV-1',
      resolution: 'Restored nullable deprecated field and configured GraphQL Hive schema checks in CI',
      deploymentPattern: 'Breaking schema alterations on mobile BFF APIs',
      timestamp: new Date(Date.now() - 260 * 24 * 3600 * 1000),
      lessonsLearned: 'Run schema breaking change linter in GitHub Actions PR checks.'
    },
    {
      id: 'INC-018',
      title: 'Memory leak in node-fetch stream parser under sustained load',
      serviceName: 'product-catalog',
      rootCause: 'Unclosed response body streams on upstream cache miss handlers',
      changedComponent: 'catalogFetcher.ts',
      affectedServices: 'product-catalog, search-service, api-gateway',
      severity: 'SEV-2',
      resolution: 'Ensured stream consumption with pipeline utility and upgraded HTTP client',
      deploymentPattern: 'HTTP client library replacement in high-throughput service',
      timestamp: new Date(Date.now() - 20 * 24 * 3600 * 1000),
      lessonsLearned: 'Always test stream disposal under prolonged soak testing.'
    }
  ];

  for (const inc of incidents) {
    await prisma.incident.create({ data: inc });
  }
  console.log(`✅ Seeded ${incidents.length} Historical Incidents`);

  // 4. Seed Service Dependency Graph
  const dependencies = [
    { sourceService: 'api-gateway', targetService: 'checkout-service', dependencyType: 'SYNC_HTTP', isDownstream: true, failureWeight: 1.2 },
    { sourceService: 'api-gateway', targetService: 'auth-service', dependencyType: 'SYNC_HTTP', isDownstream: true, failureWeight: 1.5 },
    { sourceService: 'api-gateway', targetService: 'product-catalog', dependencyType: 'SYNC_HTTP', isDownstream: true, failureWeight: 0.8 },
    { sourceService: 'checkout-service', targetService: 'payment-service', dependencyType: 'CRITICAL', isDownstream: true, failureWeight: 2.0 },
    { sourceService: 'checkout-service', targetService: 'order-service', dependencyType: 'SYNC_HTTP', isDownstream: true, failureWeight: 1.8 },
    { sourceService: 'checkout-service', targetService: 'inventory-service', dependencyType: 'SYNC_HTTP', isDownstream: true, failureWeight: 1.6 },
    { sourceService: 'checkout-service', targetService: 'promo-service', dependencyType: 'SYNC_HTTP', isDownstream: true, failureWeight: 1.1 },
    { sourceService: 'payment-service', targetService: 'notification-service', dependencyType: 'ASYNC_QUEUE', isDownstream: true, failureWeight: 0.6 },
    { sourceService: 'order-service', targetService: 'inventory-service', dependencyType: 'SYNC_HTTP', isDownstream: true, failureWeight: 1.4 },
    { sourceService: 'order-service', targetService: 'notification-service', dependencyType: 'ASYNC_QUEUE', isDownstream: true, failureWeight: 0.7 },
    { sourceService: 'product-catalog', targetService: 'search-service', dependencyType: 'DATABASE', isDownstream: true, failureWeight: 0.9 }
  ];

  for (const dep of dependencies) {
    await prisma.serviceDependency.create({ data: dep });
  }
  console.log(`✅ Seeded ${dependencies.length} Service Dependencies`);

  // 5. Seed Core Hero Deployment (Checkout Service v2.8.4 - High Risk)
  const heroDeployment = await prisma.deployment.create({
    data: {
      id: 'dep-checkout-v284',
      serviceName: 'Checkout Service',
      version: 'v2.8.4',
      previousVersion: 'v2.8.3',
      environment: 'production',
      status: 'PENDING',
      commitSha: '8f4c2e19d',
      commitMessage: 'feat(checkout): optimize payment gateway retry logic and timeout config',
      author: 'jordan.dev@shipsafe.io',
      repository: 'shipsafe-org/checkout-service',
      branch: 'main',
      filesChanged: 6,
      linesAdded: 142,
      linesDeleted: 42,
      databaseMigration: false,
      files: {
        create: [
          { filePath: 'src/config/payment.config.ts', changeType: 'MODIFIED', linesChanged: 48, isSensitive: true, category: 'PAYMENT' },
          { filePath: 'src/services/stripeGateway.ts', changeType: 'MODIFIED', linesChanged: 64, isSensitive: true, category: 'PAYMENT' },
          { filePath: 'src/resilience/retryPolicy.ts', changeType: 'MODIFIED', linesChanged: 38, isSensitive: true, category: 'CONFIG' },
          { filePath: 'src/controllers/checkoutController.ts', changeType: 'MODIFIED', linesChanged: 22, isSensitive: false, category: 'API' },
          { filePath: 'src/types/payment.ts', changeType: 'MODIFIED', linesChanged: 8, isSensitive: false, category: 'CORE' },
          { filePath: 'tests/unit/checkout.spec.ts', changeType: 'MODIFIED', linesChanged: 4, isSensitive: false, category: 'CORE' }
        ]
      },
      riskAssessment: {
        create: {
          riskLevel: 'HIGH',
          riskScore: 84,
          summary: 'Payment timeout and retry policy modified in production environment. A similar historical incident (INC-017) previously caused catastrophic thread starvation and checkout failure across 4 downstream services.',
          recommendation: 'CANARY',
          confidence: 0.96,
          isAiGenerated: true,
          evidenceGrounded: true,
          factorsJson: JSON.stringify([
            { factor: 'Payment configuration modified', points: 20, category: 'SENSITIVE_CODE', evidence: 'Modified payment.config.ts & stripeGateway.ts timeout parameters' },
            { factor: 'Similar historical incident (INC-017)', points: 18, category: 'INCIDENT_HISTORY', evidence: 'Matches INC-017 root cause: timeout configuration failure' },
            { factor: 'Critical downstream dependencies', points: 16, category: 'BLAST_RADIUS', evidence: '4 services affected: Checkout, Payment, Orders, Inventory' },
            { factor: 'Substantial code diff in core logic', points: 15, category: 'DIFF_SIZE', evidence: '184 total lines changed across 6 files' },
            { factor: 'Target is live Production environment', points: 15, category: 'ENVIRONMENT', evidence: 'Production deployment with 100% user traffic exposure' }
          ]),
          evidenceJson: JSON.stringify([
            'Payment configuration modified in 2 sensitive files',
            '184 lines changed across 6 files in critical transaction path',
            'Similar historical incident INC-017 caused checkout outage',
            '4 downstream services in direct blast radius',
            'No database migration required (Safe Rollback available)'
          ]),
          affectedServicesJson: JSON.stringify([
            'Checkout Service',
            'Payment Service',
            'Order Service',
            'Inventory Service'
          ]),
          failureChainJson: JSON.stringify([
            { step: 1, component: 'Payment Configuration', state: 'Modified Timeout & Retries', impact: 'Socket connection pool exhaustion', evidence: 'stripeGateway.ts line 42 changed retry limit to 5' },
            { step: 2, component: 'Payment Gateway', state: 'Upstream Latency Spike', impact: 'Gateway thread pool blocked waiting for socket response', evidence: 'Payment timeout increased from 2000ms to 8000ms' },
            { step: 3, component: 'Checkout Service', state: 'Retries Cascading', impact: 'Inbound HTTP request queue reaches max capacity', evidence: 'Checkout service sync calls blocked on payment response' },
            { step: 4, component: 'Order Queue', state: 'Queue Saturation', impact: 'Async order confirmation messages delayed', evidence: 'Downstream RabbitMQ order pipeline backed up' },
            { step: 5, component: 'Customer Experience', state: 'Checkout Outage', impact: 'HTTP 504 Gateway Timeout on user checkout click', evidence: 'Complete checkout funnel failure for end users' }
          ]),
          similarIncidentsJson: JSON.stringify([
            {
              incidentId: 'INC-017',
              title: 'Checkout outage after payment configuration update',
              service: 'checkout-service',
              similarityScore: 0.92,
              rootCause: 'Incorrect timeout configuration on payment connector caused cascade thread starvation',
              severity: 'SEV-1',
              resolution: 'Rolled back to previous version v2.8.3, restored default 2000ms circuit breaker timeout',
              matchingFactors: ['Same service: checkout-service', 'Similar file: payment.config.ts', 'Matching downstream impact: payment, orders']
            },
            {
              incidentId: 'INC-010',
              title: 'Payment gateway double charge on network retry',
              service: 'payment-service',
              similarityScore: 0.74,
              rootCause: 'Missing idempotency key on merchant API retry logic',
              severity: 'SEV-1',
              resolution: 'Enforced unique UUIDv4 idempotency key header',
              matchingFactors: ['Payment gateway interaction', 'Retry mechanism changes']
            }
          ])
        }
      },
      healthMetrics: {
        create: [
          {
            errorRate: 0.8,
            latencyMs: 220,
            http5xxCount: 12,
            cpuPercent: 61.2,
            requestsPerSec: 12420,
            status: 'HEALTHY'
          }
        ]
      }
    }
  });

  // 6. Seed Additional Deployments for Active Deployments list & Scenarios
  // Scenario 1: Documentation change (Low Risk)
  await prisma.deployment.create({
    data: {
      id: 'dep-docs-v112',
      serviceName: 'Documentation Portal',
      version: 'v1.1.2',
      previousVersion: 'v1.1.1',
      environment: 'production',
      status: 'PENDING',
      commitSha: '3a1b9c8d',
      commitMessage: 'docs: update API authentication guides and SDK examples',
      author: 'maya.techwriter@shipsafe.io',
      repository: 'shipsafe-org/docs-portal',
      branch: 'main',
      filesChanged: 3,
      linesAdded: 45,
      linesDeleted: 12,
      databaseMigration: false,
      riskAssessment: {
        create: {
          riskLevel: 'LOW',
          riskScore: 12,
          summary: 'Static documentation update with zero backend or database changes. Safe for direct production release.',
          recommendation: 'APPROVE',
          confidence: 0.99,
          factorsJson: JSON.stringify([
            { factor: 'Static markdown documentation only', points: 5, category: 'DIFF_SIZE', evidence: 'Only .md and .png files updated' },
            { factor: 'Zero backend microservice dependencies', points: 2, category: 'BLAST_RADIUS', evidence: 'Isolated static frontend service' },
            { factor: 'Production environment', points: 5, category: 'ENVIRONMENT', evidence: 'Standard production pipeline' }
          ]),
          evidenceJson: JSON.stringify([
            'Pure documentation files changed (docs/api-auth.md)',
            'No sensitive configurations or API endpoints touched',
            'No historical incidents recorded for this repository'
          ]),
          affectedServicesJson: JSON.stringify(['Documentation Portal']),
          failureChainJson: JSON.stringify([]),
          similarIncidentsJson: JSON.stringify([])
        }
      },
      healthMetrics: {
        create: [
          {
            errorRate: 0.01,
            latencyMs: 45,
            http5xxCount: 0,
            cpuPercent: 14.5,
            requestsPerSec: 3200,
            status: 'HEALTHY'
          }
        ]
      }
    }
  });

  // Scenario 2: API Gateway rate limiting (Medium Risk)
  await prisma.deployment.create({
    data: {
      id: 'dep-gateway-v340',
      serviceName: 'API Gateway',
      version: 'v3.4.0',
      previousVersion: 'v3.3.9',
      environment: 'production',
      status: 'PENDING',
      commitSha: '5f7e8a12',
      commitMessage: 'feat(gateway): add token bucket rate limiter for partner API endpoints',
      author: 'sam.sre@shipsafe.io',
      repository: 'shipsafe-org/api-gateway',
      branch: 'main',
      filesChanged: 4,
      linesAdded: 88,
      linesDeleted: 14,
      databaseMigration: false,
      riskAssessment: {
        create: {
          riskLevel: 'MEDIUM',
          riskScore: 54,
          summary: 'Rate limiter added to gateway tier. Moderate risk of false-positive 429 Too Many Requests on burst traffic.',
          recommendation: 'CANARY',
          confidence: 0.91,
          factorsJson: JSON.stringify([
            { factor: 'Gateway traffic routing logic changed', points: 20, category: 'SENSITIVE_CODE', evidence: 'rateLimiter.ts token bucket implementation' },
            { factor: 'Multiple downstream services route through gateway', points: 18, category: 'BLAST_RADIUS', evidence: 'Gateway routes to all microservices' },
            { factor: 'Moderate diff size', points: 16, category: 'DIFF_SIZE', evidence: '102 lines modified in networking layer' }
          ]),
          evidenceJson: JSON.stringify([
            'Gateway rate limiting rules updated',
            'Potential to throttle partner webhook integrations',
            'Similar historical incident INC-012 with buffer limits'
          ]),
          affectedServicesJson: JSON.stringify(['API Gateway', 'Partner Webhooks', 'User Service']),
          failureChainJson: JSON.stringify([
            { step: 1, component: 'Gateway Token Bucket', state: 'Burst Rate Exceeded', impact: 'HTTP 429 returns for high-throughput clients', evidence: 'Max burst parameter configured to 500 req/min' },
            { step: 2, component: 'Partner API Clients', state: 'Sync Retry Floods', impact: 'Gateway connection table saturation', evidence: 'Aggressive client retries' }
          ]),
          similarIncidentsJson: JSON.stringify([
            {
              incidentId: 'INC-012',
              title: 'API Gateway 502 Bad Gateway under peak traffic',
              service: 'api-gateway',
              similarityScore: 0.68,
              rootCause: 'Nginx upstream connection pool maxed out',
              severity: 'SEV-1',
              resolution: 'Increased keepalive pool size',
              matchingFactors: ['Same service: api-gateway', 'Networking configuration change']
            }
          ])
        }
      },
      healthMetrics: {
        create: [
          {
            errorRate: 0.3,
            latencyMs: 110,
            http5xxCount: 4,
            cpuPercent: 42.0,
            requestsPerSec: 28500,
            status: 'HEALTHY'
          }
        ]
      }
    }
  });

  // 7. Seed Initial Audit Logs
  await prisma.auditLog.create({
    data: {
      deploymentId: heroDeployment.id,
      userName: 'Sarah Chen',
      userRole: 'RELEASE_MANAGER',
      action: 'VIEW_RISK',
      details: 'Reviewed risk score (84/100) and blast radius on iQOO 12 Pro (Android 14)',
      result: 'SUCCESS',
      ipAddress: '192.168.1.104',
      deviceModel: 'iQOO 12 Pro'
    }
  });

  console.log('🎉 ShipSafe Database Seeding Completed Successfully!');
}

if (require.main === module) {
  seedDatabase()
    .catch((e) => {
      console.error('❌ Seeding failed:', e);
      process.exit(1);
    })
    .finally(async () => {
      await prisma.$disconnect();
    });
}
