# 10anoEventos

Aplicação full stack para gerenciamento de eventos, usuários, inscrições, participações e insumos. O projeto está organizado como um monorepo com backend Spring Boot, frontend Angular e PostgreSQL via Docker Compose.

## Visão geral

```text
backend/              API REST em Java 21 com Spring Boot
frontend/app/         Aplicação Angular 21 com Angular Material
infra/docker/         Docker Compose do PostgreSQL
```

Fluxo local de desenvolvimento:

1. Subir o PostgreSQL.
2. Iniciar o backend em `http://localhost:8080`.
3. Iniciar o frontend em `http://localhost:4200`.
4. Consultar e testar a API pela documentação Swagger.

## Tecnologias

- Java 21
- Spring Boot 4.0.4
- Spring Web MVC
- Spring Security com JWT em cookie HttpOnly
- Spring Data JPA
- Springdoc OpenAPI e Swagger UI
- PostgreSQL 15
- Maven
- Angular 21 e Angular Material
- npm 11
- Docker e Docker Compose

## Pré-requisitos

- Java 21
- Node.js compatível com Angular 21
- npm
- Docker Desktop ou Docker Engine com Docker Compose

O backend inclui Maven Wrapper (`mvnw` e `mvnw.cmd`), portanto uma instalação global do Maven é opcional.

## Como rodar

Execute os comandos a partir da raiz do repositório.

### Banco de dados

```powershell
docker compose -f infra/docker/docker-compose.yml up -d
```

Configuração local padrão:

```text
host: localhost
porta: 5432
banco: eventos_db
usuário: eventos_user
senha: eventos_pass
```

Para encerrar o banco:

```powershell
docker compose -f infra/docker/docker-compose.yml down
```

Antes de aplicar migrations em uma base existente, revise possíveis duplicidades indicadas pelos scripts em `backend/src/main/resources/db/migration`.

### Backend

Windows:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
cd backend
./mvnw spring-boot:run
```

O backend fica disponível em `http://localhost:8080`.

### Frontend

Em outro terminal:

```powershell
cd frontend/app
npm ci
npm start
```

O frontend fica disponível em `http://localhost:4200`.

## Documentação da API

Após iniciar o backend, a documentação interativa está disponível em:

```text
http://localhost:8080/swagger-ui/index.html
```

Especificação OpenAPI em JSON:

```text
http://localhost:8080/v3/api-docs
```

Especificação OpenAPI em YAML:

```text
http://localhost:8080/v3/api-docs.yaml
```

As rotas protegidas exigem uma sessão ativa. Na Swagger UI, execute primeiro `POST /api/auth/login`; o backend define o JWT em cookie HttpOnly e o navegador o envia nas chamadas seguintes para o mesmo host.

A documentação está organizada nas tags:

- Autenticação
- Usuários
- Eventos
- Participações / Inscrições
- Insumos
- Testes

## Comandos úteis

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

## Observações

- O backend usa `spring.jpa.hibernate.ddl-auto: update` no ambiente local.
- As origens locais do Angular permitidas pelo CORS estão configuradas no backend.
- Senhas são recebidas apenas nos DTOs de entrada e nunca aparecem nos DTOs de resposta.
- A documentação detalhada dos endpoints é mantida pelo OpenAPI, evitando duplicação no README.
