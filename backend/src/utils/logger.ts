export const logger = {
  info: (msg: string, meta?: any) => {
    console.log(`[INFO] [${new Date().toISOString()}] ${msg}`, meta ? JSON.stringify(meta) : '');
  },
  warn: (msg: string, meta?: any) => {
    console.warn(`[WARN] [${new Date().toISOString()}] ${msg}`, meta ? JSON.stringify(meta) : '');
  },
  error: (msg: string, meta?: any) => {
    console.error(`[ERROR] [${new Date().toISOString()}] ${msg}`, meta ? JSON.stringify(meta) : '');
  },
  risk: (msg: string, score: number, meta?: any) => {
    console.log(`[RISK-ENGINE] [Score: ${score}/100] ${msg}`, meta ? JSON.stringify(meta) : '');
  }
};
