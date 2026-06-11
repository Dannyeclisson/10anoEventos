# 10anoEventos

Aplicacao full stack para gerenciamento de eventos, organizada como um monorepo com backend Spring Boot, frontend Angular e infraestrutura local com PostgreSQL via Docker Compose.

## Visao Geral

O repositorio esta dividido assim:

```text
backend/              API REST em Java 21 com Spring Boot 4, JPA e PostgreSQL
frontend/app/         Aplicacao Angular 21
infra/docker/         Docker Compose do banco PostgreSQL
```

Fluxo esperado em desenvolvimento:

1. Subir o PostgreSQL local.
2. Rodar o backend em `http://localhost:8080`.
3. Rodar o frontend em `http://localhost:4200`.
4. O frontend consome a API de eventos em `http://localhost:8080/api/eventos`.

## Tecnologias

- Java 21
- Spring Boot 4.0.4
- Spring Web MVC
- Spring Data JPA
- PostgreSQL 15
- Maven Wrapper
- Angular 21
- npm 11
- Docker / Docker Compose

Ambiente local usado na validacao deste README:

- Java `21`
- Node `v22.17.0`
- npm `11.10.1`
- Docker `29.2.1`

## Pre-requisitos

Instale antes de rodar:

- Java 21
- Node.js compativel com Angular 21
- npm
- Docker Desktop ou Docker Engine com Docker Compose

Nao e necessario instalar Maven globalmente, porque o backend inclui Maven Wrapper (`mvnw` e `mvnw.cmd`).

## Como Rodar

Execute os comandos a partir da raiz do repositorio.

### 1. Subir o banco de dados

```powershell
docker compose -f infra/docker/docker-compose.yml up -d
```

O Compose cria um PostgreSQL com:

```text
host: localhost
porta: 5432
banco: eventos_db
usuario: eventos_user
senha: eventos_pass
```

Esses valores precisam bater com `backend/src/main/resources/application.yml`, que hoje ja esta configurado para esse banco.

Para parar o banco:

```powershell
docker compose -f infra/docker/docker-compose.yml down
```

Para parar e remover tambem o volume com os dados:

```powershell
docker compose -f infra/docker/docker-compose.yml down -v
```

### 2. Rodar o backend

No Windows:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
cd backend
./mvnw spring-boot:run
```

A API sobe em:

```text
http://localhost:8080
```

Endpoint simples para testar:

```text
GET http://localhost:8080/api/test
```

Resposta esperada:

```text
API funcionando
```

### 3. Rodar o frontend

Em outro terminal:

```powershell
cd frontend/app
npm ci
npm start
```

O Angular sobe em:

```text
http://localhost:4200
```

## Scripts Uteis

Backend:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
.\mvnw.cmd package
```

Frontend:

```powershell
cd frontend/app
npm ci
npm start
npm run build
npm test
```

## Endpoints da API

### Teste

```text
GET /api/test
```

### Usuarios

```text
GET  /api/usuarios
POST /api/usuarios
```

Payload para criar usuario:

```json
{
  "nome": "Maria Organizadora",
  "email": "maria@example.com",
  "tipo": "ORGANIZADOR"
}
```

Valores aceitos em `tipo`:

```text
ORGANIZADOR
PARTICIPANTE
COLABORADOR
```

### Eventos

```text
GET  /api/eventos
POST /api/eventos
```

Payload para criar evento:

```json
{
  "nome": "Workshop de Tecnologia",
  "descricao": "Encontro sobre desenvolvimento full stack",
  "local": "Auditorio Central",
  "dataHora": "2026-06-20T19:30:00",
  "organizadorId": 1
}
```

Importante: `organizadorId` precisa apontar para um usuario ja existente. O backend converte `dataHora` com `LocalDateTime.parse`, entao use formato ISO local, como `2026-06-20T19:30:00`.

## Exemplos Rapidos com PowerShell

Criar um organizador:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/api/usuarios `
  -ContentType "application/json" `
  -Body '{"nome":"Maria Organizadora","email":"maria@example.com","tipo":"ORGANIZADOR"}'
```

Criar um evento para o organizador `1`:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/api/eventos `
  -ContentType "application/json" `
  -Body '{"nome":"Workshop de Tecnologia","descricao":"Encontro sobre desenvolvimento full stack","local":"Auditorio Central","dataHora":"2026-06-20T19:30:00","organizadorId":1}'
```

Listar eventos:

```powershell
Invoke-RestMethod http://localhost:8080/api/eventos
```

## Estado Atual do Frontend

O projeto Angular ainda esta majoritariamente no template inicial gerado pelo Angular CLI.

Ja existe:

- `EventoService`, apontando para `http://localhost:8080/api/eventos`
- `EventosComponent`, que lista eventos retornados pela API

Mas a tela de eventos ainda nao esta ligada na aplicacao principal, porque:

- `app.routes.ts` esta vazio
- `app.html` ainda contem o placeholder padrao do Angular
- `app.config.ts` ainda nao registra `provideHttpClient()`, necessario para injetar `HttpClient` quando a tela de eventos for usada

## Validacoes Feitas

Comandos executados durante a analise:

```text
npm ci
npm run build
npm test -- --watch=false
```

Resultado:

- `npm ci` concluiu com sucesso.
- `npm run build` concluiu com sucesso e gerou `frontend/app/dist/app`.
- `npm ci` reportou 2 vulnerabilidades moderadas via `npm audit`.
- `npm test -- --watch=false` falhou na compilacao dos testes porque os specs importam nomes que nao existem mais:
  - `src/app/app.spec.ts` importa `App`, mas o componente exportado e `AppComponent`.
  - `src/app/pages/eventos/eventos.spec.ts` importa `Eventos`, mas o componente exportado e `EventosComponent`.

Tambem foi executado:

```text
.\mvnw.cmd test
```

Resultado:

- O backend compilou.
- O teste `BackendApplicationTests.contextLoads` falhou ao carregar o `ApplicationContext`.
- A causa reportada foi criacao do `entityManagerFactory` sem conseguir determinar o dialect do Hibernate via metadados JDBC.

Na pratica, o teste atual depende da configuracao real do datasource PostgreSQL e nao possui um perfil de teste isolado. Para testes automatizados mais confiaveis, uma melhoria futura seria criar `src/test/resources/application-test.yml` com banco de teste, Testcontainers ou H2, e ativar esse perfil nos testes.

## Observacoes de Implementacao

- O backend usa `spring.jpa.hibernate.ddl-auto: update`, entao as tabelas sao criadas/atualizadas automaticamente durante o desenvolvimento.
- CORS esta liberado para `http://localhost:4200` em `CorsConfig`.
- A API nao possui autenticacao neste momento.
- Os textos de validacao em alguns arquivos Java aparecem com caracteres corrompidos no codigo-fonte, por exemplo `obrigatÃ³rio`. Vale revisar a codificacao desses arquivos para UTF-8.
