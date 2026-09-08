# ShipSafe — Hackathon 60-90s Demo Script for Judges

### Tagline
> **"See the risk. Understand the impact. Ship safely."**

---

## 🎬 60–90 Second Hero Demo Walkthrough

### 1. The Hook (0:00 - 0:15)
- **Presenter Statement**: *"Modern CI/CD notifies engineers when a release needs approval, but approvals are often blind because engineers don't have deep context on their phones. ShipSafe turns the engineer's phone into an AI-powered deployment safety and decision surface."*
- **Screen**: Open **ShipSafe App** on phone $\rightarrow$ View **Deployment Safety Center**.
- **Visual**: Show **Active Deployments** card $\rightarrow$ `Checkout Service v2.8.4` flagged as **HIGH RISK (84/100)**.

---

### 2. The Hero Risk Decision Screen (0:15 - 0:35)
- **Presenter Action**: Tap **VIEW RISK** on `Checkout Service v2.8.4`.
- **Key Points to Highlight**:
  1. **🔴 Risk Score Gauge**: 84/100 calculated with transparent evidence breakdown:
     - `+20 Payment configuration changed`
     - `+18 Similar historical incident (INC-017)`
     - `+16 Critical downstream dependencies (4 services)`
     - `+15 Substantial diff in core logic (184 lines)`
     - `+15 Live production environment`
  2. **🔍 Grounded Evidence**: Expand *"Why This Risk?"* $\rightarrow$ Point out that the AI never hallucinates; every conclusion is anchored in diffs and historical memory.
  3. **🧠 Historical Incident Match**: Point out `INC-017` (*Checkout outage after payment configuration update*).
  4. **💥 Blast Radius & Failure Chain**: Show the 4 affected services (*Checkout $\rightarrow$ Payment $\rightarrow$ Orders $\rightarrow$ Inventory*).

---

### 3. The Decision: Safe Canary (0:35 - 0:50)
- **Presenter Statement**: *"Instead of blindly approving 100% traffic or halting release completely, the AI recommends Canary 10%."*
- **Action**: Tap **CANARY 10%**.
- **Screen**: Transitions to **Live Post-Deploy Health Monitoring**.
- **Visual**: Initial metrics show normal baseline (Error Rate: 0.8%, P99 Latency: 220ms).

---

### 4. Telemetry Degradation & Smart Rollback (0:50 - 1:15)
- **Action**: Tap **🚨 INJECT ERROR SPIKE** (Simulating live production canary degradation).
- **Visual**:
  - Error rate jumps **0.8% $\rightarrow$ 7.4%**
  - Latency spikes **220ms $\rightarrow$ 780ms**
  - Alert banner turns crimson: **"🚨 DEPLOYMENT DEGRADATION DETECTED — AI Recommendation: Immediate Rollback"**
- **Action**: Tap **RESTORE STABLE VERSION (v2.8.3)**.
- **Visual**: **Smart Rollback Guard** modal runs 4 pre-flight checks:
  - ✓ Deployment Active State
  - ✓ Previous Stable Tag v2.8.3 verified in registry
  - ✓ Database Schema lock check (No migrations, safe binary revert)
  - ✓ Cluster readiness check
- **Action**: Tap **CONFIRM ROLLBACK**.

---

### 5. Verified Resolution & Audit Trail (1:15 - 1:30)
- **Screen**: Opens **Immutable Audit Trail**.
- **Visual**: Demonstrates cryptographically timestamped records:
  - `Sarah Chen (RELEASE_MANAGER)` $\rightarrow$ `CANARY (10% traffic)`
  - `Health Telemetry Daemon` $\rightarrow$ `HEALTH_ALERT (Spike to 7.4%)`
  - `David Miller (ON_CALL_ENGINEER)` $\rightarrow$ `ROLLBACK (Restored v2.8.3)` on *iQOO 12 Pro (Android 14)*.

---

## ⚡ Fast Demo Reset for Next Judge
To reset the demo instantly:
- Run `npm run seed` in `backend/` or call `POST /api/demo/reset`.
