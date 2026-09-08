import http from 'http';

const BASE_URL = 'http://127.0.0.1:3000';
let passCount = 0;
let failCount = 0;
const results = [];

function assert(condition, testName, details = '') {
  if (condition) {
    passCount++;
    results.push({ name: testName, status: 'PASS', details });
    console.log(`  ✅ [PASS] ${testName} ${details ? '(' + details + ')' : ''}`);
  } else {
    failCount++;
    results.push({ name: testName, status: 'FAIL', details });
    console.error(`  ❌ [FAIL] ${testName} - ${details}`);
  }
}

async function request(path, options = {}, data = null) {
  return new Promise((resolve) => {
    const parsedUrl = new URL(path, BASE_URL);
    const reqOptions = {
      hostname: parsedUrl.hostname,
      port: parsedUrl.port,
      path: parsedUrl.pathname + parsedUrl.search,
      method: options.method || 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...(options.headers || {})
      }
    };

    const req = http.request(reqOptions, (res) => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => {
        try {
          resolve({ status: res.statusCode, headers: res.headers, data: JSON.parse(body) });
        } catch {
          resolve({ status: res.statusCode, headers: res.headers, data: body });
        }
      });
    });

    req.on('error', (err) => {
      resolve({ status: 500, error: err.message });
    });

    if (data) {
      req.write(typeof data === 'string' ? data : JSON.stringify(data));
    }
    req.end();
  });
}

async function runComprehensiveTest() {
  console.log('═══════════════════════════════════════════════════════════════════════');
  console.log('       SHIPSAFE FULL END-TO-END QA & VERIFICATION SUITE');
  console.log('═══════════════════════════════════════════════════════════════════════\n');

  // 1. HEALTH & CONNECTIVITY TEST
  console.log('🔹 1. HEALTH & CONNECTIVITY TEST');
  const healthRes = await request('/health');
  assert(healthRes.status === 200, 'Server health probe', `Status ${healthRes.status}, Service: ${healthRes.data?.service}`);

  // 2. AUTHENTICATION & RBAC TESTS
  console.log('\n🔹 2. AUTHENTICATION & RBAC TESTS');
  // 2.1 Valid login for Release Manager
  const loginRelMgr = await request('/api/auth/login', { method: 'POST' }, {
    email: 'release@shipsafe.dev',
    password: 'shipsafe2026'
  });
  assert(loginRelMgr.status === 200 && loginRelMgr.data?.token, 'Login Release Manager', `Role: ${loginRelMgr.data?.user?.role}`);
  const relMgrToken = loginRelMgr.data?.token;

  // 2.2 Valid login for SRE On-Call
  const loginOnCall = await request('/api/auth/login', { method: 'POST' }, {
    email: 'oncall@shipsafe.dev',
    password: 'shipsafe2026'
  });
  assert(loginOnCall.status === 200 && loginOnCall.data?.token, 'Login On-Call SRE', `Role: ${loginOnCall.data?.user?.role}`);
  const onCallToken = loginOnCall.data?.token;

  // 2.3 Valid login for Viewer
  const loginViewer = await request('/api/auth/login', { method: 'POST' }, {
    email: 'viewer@shipsafe.dev',
    password: 'shipsafe2026'
  });
  assert(loginViewer.status === 200 && loginViewer.data?.token, 'Login Viewer', `Role: ${loginViewer.data?.user?.role}`);
  const viewerToken = loginViewer.data?.token;

  // 2.4 Invalid Password (401)
  const invalidPass = await request('/api/auth/login', { method: 'POST' }, {
    email: 'release@shipsafe.dev',
    password: 'wrong_password_123'
  });
  assert(invalidPass.status === 401, 'Reject Invalid Password (401)', `Response: ${invalidPass.data?.error || invalidPass.status}`);

  // 2.5 Nonexistent User (401)
  const nonUser = await request('/api/auth/login', { method: 'POST' }, {
    email: 'nonexistent_user@shipsafe.dev',
    password: 'shipsafe2026'
  });
  assert(nonUser.status === 401, 'Reject Nonexistent User (401)', `Response: ${nonUser.data?.error || nonUser.status}`);

  // 2.6 Missing Token on Protected API (401)
  const noToken = await request('/api/deployments');
  assert(noToken.status === 401, 'Reject Missing Token on Protected Route (401)');

  // 2.7 Malformed Token (401)
  const malformedToken = await request('/api/deployments', {
    headers: { 'Authorization': 'Bearer invalid.malformed.jwt.token' }
  });
  assert(malformedToken.status === 401, 'Reject Malformed JWT Token (401)');

  // 2.8 RBAC: Viewer should be blocked from executing Canary (403)
  const viewerCanary = await request('/api/deployments/dep-checkout-v284/canary', {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${viewerToken}` }
  }, { action: 'CANARY', trafficPct: 10, reason: 'Viewer unauthorized attempt' });
  assert(viewerCanary.status === 403, 'RBAC: Block Viewer from Canary Action (403)', `Status ${viewerCanary.status}`);

  // 3. DEPLOYMENT & RISK ENGINE TESTS
  console.log('\n🔹 3. DEPLOYMENT & RISK ENGINE TESTS');
  const depsRes = await request('/api/deployments', {
    headers: { 'Authorization': `Bearer ${relMgrToken}` }
  });
  assert(depsRes.status === 200 && Array.isArray(depsRes.data) && depsRes.data.length >= 3, 'Fetch Deployment List', `Count: ${depsRes.data?.length}`);

  // 3.1 Hero Deployment: dep-checkout-v284
  const heroDep = await request('/api/deployments/dep-checkout-v284', {
    headers: { 'Authorization': `Bearer ${relMgrToken}` }
  });
  assert(heroDep.status === 200 && heroDep.data?.id === 'dep-checkout-v284', 'Fetch Hero Deployment Details', `Service: ${heroDep.data?.serviceName}`);

  // 3.2 Nonexistent Deployment (404)
  const notFoundDep = await request('/api/deployments/dep-nonexistent-999', {
    headers: { 'Authorization': `Bearer ${relMgrToken}` }
  });
  assert(notFoundDep.status === 404, 'Handle Nonexistent Deployment (404)');

  // 3.3 Risk Assessment Check
  const riskRes = await request('/api/deployments/dep-checkout-v284/risk', {
    headers: { 'Authorization': `Bearer ${relMgrToken}` }
  });
  assert(riskRes.status === 200 && riskRes.data?.riskScore === 84, 'Hero Deployment Risk Score (84/100)', `Score: ${riskRes.data?.riskScore}, Level: ${riskRes.data?.riskLevel}`);
  assert(riskRes.data?.recommendation === 'CANARY', 'AI Recommendation matches CANARY');
  assert(riskRes.data?.factors?.length >= 5, 'Contributing Factors Breakdown present', `Count: ${riskRes.data?.factors?.length}`);
  assert(riskRes.data?.evidence?.length >= 4, 'Evidence Bullets grounded', `Count: ${riskRes.data?.evidence?.length}`);

  // 4. BLAST RADIUS & FAILURE CHAIN TESTS
  console.log('\n🔹 4. BLAST RADIUS & FAILURE CHAIN TESTS');
  const blastRes = await request('/api/deployments/dep-checkout-v284/blast-radius', {
    headers: { 'Authorization': `Bearer ${relMgrToken}` }
  });
  assert(blastRes.status === 200 && blastRes.data?.totalBlastRadiusCount >= 4, 'Blast Radius Topology Calculation', `Total Affected: ${blastRes.data?.totalBlastRadiusCount} services`);

  const chainRes = await request('/api/deployments/dep-checkout-v284/failure-chain', {
    headers: { 'Authorization': `Bearer ${relMgrToken}` }
  });
  assert(chainRes.status === 200 && chainRes.data?.steps?.length === 5, 'Failure Chain 5 Step Propagation Path', `Steps: ${chainRes.data?.steps?.length}`);
  assert(chainRes.data?.steps[0]?.component?.includes('Payment'), 'Failure Chain Step 1 starts with Payment Component');

  // 5. HISTORICAL INCIDENT MEMORY TESTS
  console.log('\n🔹 5. HISTORICAL INCIDENT MEMORY TESTS');
  const incRes = await request('/api/incidents', {
    headers: { 'Authorization': `Bearer ${relMgrToken}` }
  });
  assert(incRes.status === 200 && incRes.data?.length === 18, 'Historical Incident Memory Seed Count (18 incidents)', `Count: ${incRes.data?.length}`);

  const inc17 = await request('/api/incidents/INC-017', {
    headers: { 'Authorization': `Bearer ${relMgrToken}` }
  });
  assert(inc17.status === 200 && inc17.data?.id === 'INC-017', 'Fetch Similar Incident INC-017', `Title: ${inc17.data?.title}`);

  // 6. DEPLOYMENT ACTIONS & HEALTH MONITORING TESTS
  console.log('\n🔹 6. DEPLOYMENT ACTIONS & HEALTH MONITORING');
  // 6.1 Execute Canary 10%
  const canaryRes = await request('/api/deployments/dep-checkout-v284/canary', {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${relMgrToken}` }
  }, { action: 'CANARY', trafficPct: 10, reason: 'AI recommended gradual 10% canary rollout' });
  assert(canaryRes.status === 200 && canaryRes.data?.action === 'CANARY', 'Execute Canary 10% Rollout', `Status: ${canaryRes.data?.deployment?.status}`);

  // 6.2 Health Baseline Check
  const healthBase = await request('/api/deployments/dep-checkout-v284/health', {
    headers: { 'Authorization': `Bearer ${relMgrToken}` }
  });
  assert(healthBase.status === 200 && healthBase.data?.baseline?.errorRate === 0.8, 'Baseline Health Error Rate (0.8%)', `Latency: ${healthBase.data?.baseline?.latencyMs}ms`);

  // 6.3 Inject Degradation (0.8% -> 7.4%, 220ms -> 780ms)
  const degRes = await request('/api/demo/simulate-health', { method: 'POST' }, {
    deploymentId: 'dep-checkout-v284',
    degrade: true
  });
  assert(degRes.status === 200 && degRes.data?.degraded === true, 'Inject Telemetry Degradation Spike', `Error Rate: ${degRes.data?.health?.current?.errorRate}%, Latency: ${degRes.data?.health?.current?.latencyMs}ms`);
  assert(degRes.data?.health?.recommendation === 'ROLLBACK', 'Telemetry Degradation triggers ROLLBACK recommendation');

  // 7. SMART ROLLBACK GUARD & RESTORATION TESTS
  console.log('\n🔹 7. SMART ROLLBACK GUARD & RESTORATION');
  // 7.1 Pre-flight Rollback Guard Check
  const guardRes = await request('/api/deployments/dep-checkout-v284/rollback-guard', {
    headers: { 'Authorization': `Bearer ${onCallToken}` }
  });
  assert(guardRes.status === 200 && guardRes.data?.canRollback === true, 'Rollback Guard Pre-flight Verification', `Target: ${guardRes.data?.targetVersion}, DB Migration Safe: ${!guardRes.data?.hasDatabaseMigration}`);

  // 7.2 Execute Confirmed Rollback
  const rollbackRes = await request('/api/deployments/dep-checkout-v284/rollback', {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${onCallToken}` }
  }, { action: 'ROLLBACK', reason: 'Confirmed rollback after health degradation alert' });
  assert(rollbackRes.status === 200 && rollbackRes.data?.action === 'ROLLBACK', 'Execute Verified Rollback to v2.8.3', `Restored Version: ${rollbackRes.data?.restoredVersion}`);

  // 7.3 Second Rollback Attempt (Safely Handled / Blocked)
  const rollbackTwice = await request('/api/deployments/dep-checkout-v284/rollback', {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${onCallToken}` }
  }, { action: 'ROLLBACK', reason: 'Duplicate rollback request' });
  assert(rollbackTwice.status === 400 || rollbackTwice.data?.guard?.canRollback === false, 'Duplicate Rollback Safe Blocking', `Status: ${rollbackTwice.status}`);

  // 8. AUDIT LOG INTEGRITY & SECURITY TESTS
  console.log('\n🔹 8. AUDIT LOG INTEGRITY & SECURITY');
  const auditRes = await request('/api/audit', {
    headers: { 'Authorization': `Bearer ${relMgrToken}` }
  });
  assert(auditRes.status === 200 && Array.isArray(auditRes.data), 'Audit Log Trail Retrieval', `Total Entries: ${auditRes.data?.length}`);
  const actions = auditRes.data.map(a => a.action);
  assert(actions.includes('CANARY'), 'Audit Log records CANARY decision');
  assert(actions.includes('ROLLBACK'), 'Audit Log records ROLLBACK action');
  assert(actions.includes('HEALTH_ALERT'), 'Audit Log records HEALTH_ALERT telemetry trigger');

  // 9. GITHUB WEBHOOK TEST
  console.log('\n🔹 9. GITHUB ACTIONS WEBHOOK TEST');
  const webhookRes = await request('/api/webhooks/github', {
    method: 'POST',
    headers: {
      'X-GitHub-Event': 'push',
      'X-Hub-Signature-256': 'mock-dev-signature'
    }
  }, {
    repository: { full_name: 'shipsafe-org/payment-service' },
    ref: 'refs/heads/main',
    head_commit: {
      id: '9a8b7c6d',
      message: 'fix: optimize payment webhook timeout',
      author: { email: 'ci@shipsafe.dev' },
      added: [],
      modified: ['src/config/payment.config.ts'],
      removed: []
    }
  });
  assert(webhookRes.status === 201 && webhookRes.data?.success === true, 'GitHub Webhook Ingestion & Automated Risk Scan', `DeploymentId: ${webhookRes.data?.deploymentId}`);

  // 10. DEMO RELIABILITY (5 CONSECUTIVE RUNS)
  console.log('\n🔹 10. DEMO RELIABILITY (5 CONSECUTIVE RUNS)');
  let allDemoPass = true;
  for (let i = 1; i <= 5; i++) {
    const reset = await request('/api/demo/reset', { method: 'POST' });
    const rRes = await request('/api/deployments/dep-checkout-v284/risk', {
      headers: { 'Authorization': `Bearer ${relMgrToken}` }
    });
    const canary = await request('/api/deployments/dep-checkout-v284/canary', {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${relMgrToken}` }
    }, { action: 'CANARY', trafficPct: 10, reason: 'Demo test' });
    const deg = await request('/api/demo/simulate-health', { method: 'POST' }, { deploymentId: 'dep-checkout-v284', degrade: true });
    const rb = await request('/api/deployments/dep-checkout-v284/rollback', {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${onCallToken}` }
    }, { action: 'ROLLBACK', reason: 'Demo test rollback' });

    const runOk = reset.status === 200 && rRes.data?.riskScore === 84 && canary.status === 200 && deg.data?.degraded === true && rb.status === 200;
    if (!runOk) allDemoPass = false;
    console.log(`  🔄 Hero Demo Run ${i}/5: ${runOk ? 'PASS (100% Deterministic)' : 'FAIL'}`);
  }
  assert(allDemoPass, '5 Consecutive Hero Demo Iterations 100% Deterministic & Reliable');

  // SUMMARY
  console.log('\n═══════════════════════════════════════════════════════════════════════');
  console.log(`TEST RESULTS: ${passCount} PASSED | ${failCount} FAILED | TOTAL: ${passCount + failCount}`);
  console.log('═══════════════════════════════════════════════════════════════════════\n');
}

runComprehensiveTest().catch(console.error);
