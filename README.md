# ShipSafe 🛡️
### AI-Powered Mobile Deployment Safety Platform for DevOps/SRE Engineers

> **"See the risk. Understand the impact. Ship safely."**

---

## 🌟 Overview
Modern CI/CD systems notify engineers when releases need approval, but engineers away from their laptops often make blind decisions or cause deployment delays. **ShipSafe** turns the engineer's phone into a **deployment safety and decision surface**, answering:
* *What could break if I deploy this?*
* *Why could it happen?*
* *How large is the blast radius?*
* *What is the safest deployment strategy?*

---

## 🏗️ Architecture

```text
Developer pushes code
        ↓
GitHub Actions CI/CD
        ↓
ShipSafe Webhook
        ↓
Code Diff & Sensitive File Analysis
        ↓
Historical Incident Memory (18 SRE Incident Cases)
        ↓
Service Dependency & Blast Radius Engine
        ↓
AI Risk Synthesis (OpenRouter with Deterministic Fallback)
        ↓
Push Notification to Engineer's Phone (iQOO Optimized)
        ↓
HERO SCREEN: Risk Decision Surface (Score: 84/100)
        ↓
Decision: APPROVE / CANARY 10% / HOLD
        ↓
Live Health Telemetry Monitoring (0.8% error rate)
        ↓
Telemetry Degradation Spike (0.8% → 7.4%, 220ms → 780ms)
        ↓
Smart Rollback Guard (Pre-flight Revalidation)
        ↓
Confirmed Rollback (Restored Stable v2.8.3)
        ↓
Immutable Cryptographic Audit Trail
```

---

## 📱 Native Android Application
Built with:
- **Language**: Kotlin 2.0
- **UI Toolkit**: Jetpack Compose + Material 3 (Enterprise Dark DevOps Theme)
- **Architecture**: MVVM + StateFlow + Kotlin Coroutines
- **Networking**: Retrofit 2 + OkHttp + Gson
- **Navigation**: Navigation Compose (16 screens)
- **Device Optimization**: Large touch targets, one-handed navigation, haptics, Red Light emergency mode.

### 16 Core Screens
1. **Splash Screen**: Animated logo reveal.
2. **Login Screen**: 1-click role selection (ADMIN, RELEASE_MANAGER, ON_CALL_ENGINEER, VIEWER).
3. **Home Dashboard / Safety Center**: Active deployments, high-risk banners, stats.
4. **Active Deployments**: Filterable deployment list.
5. **Deployment Details**: Full Git commit diffs, authors, and sensitive components.
6. **Hero Risk Decision Screen**: One-handed decision interface with 84/100 score gauge, transparent point breakdown, and action triggers.
7. **Blast Radius Screen**: Dependency graph with direct vs cascading downstream impact.
8. **Failure Chain Screen**: Step-by-step evidence-grounded failure path.
9. **Historical Incidents**: 18 realistic SRE incident memory records with similarity matching (INC-017).
10. **Strategy Simulator**: What-if comparator (Normal 100% vs Canary 10% vs Hold 0%).
11. **Health Monitoring Screen**: Live telemetry with simulated degradation injector.
12. **Rollback Guard Screen**: Target verification, DB migration lock check, safety checklist.
13. **Deployment Timeline**: Chronological event log from push to release.
14. **Audit History Screen**: Cryptographic audit records.
15. **Settings Screen**: Configurable backend server URL and device telemetry.
16. **Red Light Emergency Mode**: Distraction-free critical emergency decision interface.

---

## ⚙️ Backend & AI Service
Built with:
- **Runtime**: Node.js & TypeScript
- **Framework**: Express.js REST API
- **ORM & Database**: Prisma ORM with SQLite (portable zero-config) + PostgreSQL compatibility
- **AI Synthesis**: OpenRouter LLM API with deterministic fallback for offline resilience
- **Security**: JWT authentication, Role-Based Access Control (RBAC), HMAC Webhook verification.

---

## 🚀 Quickstart Guide

### 1. Start Backend
```bash
cd backend
npm install
npm run dev
```
The server will start on `http://localhost:3000` and auto-seed 18 historical incidents, dependencies, users, and hero deployments.

### 2. Run Android Project
Open the `android/` directory in Android Studio and run on an Android Device or Emulator.
The app connects to `http://10.0.2.2:3000` on Android Emulator or configurable IP in Settings.
