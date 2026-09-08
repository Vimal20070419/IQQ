import { createServer } from '../src/server.js';
import { seedDatabase } from '../src/database/seed.js';

let isSeeded = false;
const app = createServer();

// Ensure database is seeded for serverless executions
app.use(async (req, res, next) => {
  if (!isSeeded) {
    try {
      await seedDatabase();
      isSeeded = true;
    } catch (err) {
      // Handled internally
    }
  }
  next();
});

export default app;
