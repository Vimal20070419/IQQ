import { PrismaClient } from '@prisma/client';
import fs from 'fs';
import path from 'path';

// Handle Vercel serverless /tmp database migration
if (process.env.VERCEL) {
  const tmpDbPath = path.join('/tmp', 'dev.db');
  if (!fs.existsSync(tmpDbPath)) {
    const searchPaths = [
      path.join(process.cwd(), 'prisma', 'dev.db'),
      path.join(process.cwd(), 'backend', 'prisma', 'dev.db'),
      path.join(__dirname, '../../prisma/dev.db'),
      path.join(__dirname, '../prisma/dev.db'),
      path.join(__dirname, 'dev.db')
    ];
    for (const sp of searchPaths) {
      if (fs.existsSync(sp)) {
        try {
          fs.copyFileSync(sp, tmpDbPath);
          break;
        } catch (e) {
          // ignore
        }
      }
    }
  }
  process.env.DATABASE_URL = `file:${tmpDbPath}`;
}

const globalForPrisma = global as unknown as { prisma: PrismaClient };

export const prisma = globalForPrisma.prisma || new PrismaClient({
  datasources: process.env.DATABASE_URL ? {
    db: {
      url: process.env.DATABASE_URL
    }
  } : undefined
});

if (process.env.NODE_ENV !== 'production') globalForPrisma.prisma = prisma;
