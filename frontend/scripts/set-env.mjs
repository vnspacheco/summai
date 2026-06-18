import { mkdirSync, writeFileSync } from 'node:fs';
import { resolve } from 'node:path';

const mode = process.argv[2] ?? 'development';
const environmentDir = resolve('src/environments');
const localApiBaseUrl = 'http://localhost:8080/api/v1';
const productionApiBaseUrl =
  process.env.API_BASE_URL?.trim() || 'https://your-render-service.onrender.com/api/v1';

mkdirSync(environmentDir, { recursive: true });

const files = {
  'environment.ts': {
    production: false,
    apiBaseUrl: mode === 'development' ? localApiBaseUrl : productionApiBaseUrl
  },
  'environment.prod.ts': {
    production: true,
    apiBaseUrl: productionApiBaseUrl
  }
};

for (const [fileName, content] of Object.entries(files)) {
  const output = `export const environment = ${JSON.stringify(content, null, 2)} as const;\n`;
  writeFileSync(resolve(environmentDir, fileName), output, 'utf8');
}
