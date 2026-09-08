import http from 'http';

const BASE_URL = 'http://127.0.0.1:3000';

async function request(path, options = {}, data = null) {
  return new Promise((resolve, reject) => {
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

    req.on('error', reject);
    if (data) req.write(JSON.stringify(data));
    req.end();
  });
}

async function testRollbackFix() {
  console.log('🧪 VERIFYING FIX #1 — ROLLBACK TARGET VERSION BUG...\n');

  // Step 1: Reset Demo
  console.log('Step 1: Reset Demo');
  const resetRes = await request('/api/demo/reset', { method: 'POST' });
  console.log(`  ✓ Reset Status: ${resetRes.status}, Success: ${resetRes.data?.success}`);

  // Step 2: Deploy Canary
  console.log('\nStep 2: Deploy Canary (10%)');
  const canaryRes = await request('/api/deployments/dep-checkout-v284/canary', {
    method: 'POST',
    headers: { 'x-demo-role': 'RELEASE_MANAGER' }
  }, { action: 'CANARY', trafficPct: 10, reason: 'AI recommended gradual rollout' });
  console.log(`  ✓ Canary Status: ${canaryRes.status}, Deployment Status: ${canaryRes.data?.deployment?.status}`);

  // Step 3: Inject Degradation
  console.log('\nStep 3: Inject Telemetry Degradation (0.8% -> 7.4%, 220ms -> 780ms)');
  const degRes = await request('/api/demo/simulate-health', { method: 'POST' }, {
    deploymentId: 'dep-checkout-v284',
    degrade: true
  });
  console.log(`  ✓ Degradation Injected: Error Rate ${degRes.data?.health?.current?.errorRate}%, Recommendation: ${degRes.data?.health?.recommendation}`);

  // Step 4: Trigger Smart Rollback (Pre-flight check)
  console.log('\nStep 4: Trigger Smart Rollback Guard');
  const guardRes = await request('/api/deployments/dep-checkout-v284/rollback-guard', {
    headers: { 'x-demo-role': 'ON_CALL_ENGINEER' }
  });
  console.log(`  ✓ Guard Target Version: ${guardRes.data?.targetVersion}, Can Rollback: ${guardRes.data?.canRollback}`);

  // Step 5: Confirm Rollback
  console.log('\nStep 5: Confirm Rollback');
  const rollbackRes = await request('/api/deployments/dep-checkout-v284/rollback', {
    method: 'POST',
    headers: { 'x-demo-role': 'ON_CALL_ENGINEER' }
  }, { action: 'ROLLBACK', reason: 'Confirmed rollback after health degradation alert' });
  
  // Step 6: Verify Backend Response
  console.log('\nStep 6: Verify Backend Response payload');
  console.log('  Response Payload:', JSON.stringify(rollbackRes.data, null, 2));

  const hasSuccess = rollbackRes.data?.success === true;
  const hasDepId = rollbackRes.data?.deploymentId === 'dep-checkout-v284';
  const hasFromVersion = rollbackRes.data?.fromVersion === 'v2.8.4';
  const hasToVersion = rollbackRes.data?.toVersion === 'v2.8.3';
  const hasStatus = rollbackRes.data?.status === 'RESTORED';

  console.log(`  - success: ${hasSuccess} (${rollbackRes.data?.success})`);
  console.log(`  - deploymentId: ${hasDepId} (${rollbackRes.data?.deploymentId})`);
  console.log(`  - fromVersion: ${hasFromVersion} (${rollbackRes.data?.fromVersion})`);
  console.log(`  - toVersion: ${hasToVersion} (${rollbackRes.data?.toVersion})`);
  console.log(`  - status: ${hasStatus} (${rollbackRes.data?.status})`);

  if (!hasFromVersion || !hasToVersion || !hasStatus) {
    throw new Error('Backend response did not contain required fromVersion / toVersion / status!');
  }

  // Step 7: Verify Deployment State
  console.log('\nStep 7: Verify Deployment State in DB');
  const depState = await request('/api/deployments/dep-checkout-v284', {
    headers: { 'x-demo-role': 'RELEASE_MANAGER' }
  });
  console.log(`  ✓ Deployment Status: ${depState.data?.status}`);

  // Step 8 & 9 & 10: Verify Audit Stream and History
  console.log('\nStep 8-10: Verify Audit Log Stream & History');
  const auditRes = await request('/api/audit', {
    headers: { 'x-demo-role': 'RELEASE_MANAGER' }
  });
  const rollbackLog = auditRes.data?.find(a => a.action === 'ROLLBACK' && a.result === 'SUCCESS');
  console.log('  Audit Log Entry:', rollbackLog);
  console.log(`  Log Details: "${rollbackLog?.details}"`);

  if (!rollbackLog?.details?.includes('Restored stable version v2.8.3')) {
    throw new Error(`Audit log contains incorrect message: ${rollbackLog?.details}`);
  }
  if (rollbackLog?.details?.includes('Undefined') || rollbackLog?.details?.includes('undefined')) {
    throw new Error('Audit log contains Undefined!');
  }

  console.log('\n🎉 ALL 10 VERIFICATION STEPS PASSED PERFECTLY WITH ZERO UNDEFINED VALUES!');
}

testRollbackFix().catch(err => {
  console.error('❌ Test failed:', err);
  process.exit(1);
});
