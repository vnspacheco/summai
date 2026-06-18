# SummAI

SaaS para resumir vídeos do YouTube com Angular no frontend, Spring Boot no backend e Google Gemini para gerar os resumos em português.

## Estrutura

- `frontend`: SPA em Angular com validação de URL, estados de loading/erro/sucesso, renderização de Markdown, cópia e download.
- `backend`: API REST em Spring Boot com endpoint `POST /api/v1/summarize`, extração de transcrição do YouTube, integração com Gemini e tratamento de erros.

## Variáveis de ambiente

### Backend

- `GEMINI_API_KEY`: chave da API Gemini.
- `CORS_ALLOWED_ORIGIN`: origem liberada para o frontend. Aceita múltiplas URLs separadas por vírgula.
- `GEMINI_MODEL`: opcional. Padrão `gemini-1.5-flash`.
- `GEMINI_TIMEOUT_SECONDS`: opcional. Padrão `30`.
- `GEMINI_MAX_TRANSCRIPT_CHARACTERS`: opcional. Padrão `45000`.

### Frontend

- `API_BASE_URL`: URL base do backend, por exemplo `https://meu-backend.onrender.com/api/v1`.

## Rodando localmente

### Backend

```powershell
cd backend
$env:GEMINI_API_KEY='sua-chave'
$env:CORS_ALLOWED_ORIGIN='http://localhost:4200'
.\mvnw.cmd spring-boot:run
```

### Frontend

```powershell
cd frontend
$env:API_BASE_URL='http://localhost:8080/api/v1'
npm install
npm start
```

## Deploy

### Netlify

- Base directory: `frontend`
- Build command: `npm run build`
- Publish directory: `dist/frontend`
- Variável obrigatória: `API_BASE_URL`

### Render

- Root directory: `backend`
- Build command: `./mvnw clean package`
- Start command: `java -jar target/summai-api-0.0.1-SNAPSHOT.jar`
- Variáveis obrigatórias: `GEMINI_API_KEY` e `CORS_ALLOWED_ORIGIN`

## Observações

- O frontend foi construído em Angular 20 por compatibilidade com a versão de Node disponível neste ambiente.
- O backend trata casos de vídeo sem legenda, vídeo inacessível e timeout do Gemini.
