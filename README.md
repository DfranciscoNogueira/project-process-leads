# Leads Import

Projeto desenvolvido para processamento de arquivos CSV de leads, utilizando Java 21 + Spring Boot 3 no backend e React + Vite no frontend.

O sistema permite upload de arquivos CSV, processamento assíncrono em chunks, persistência de dados, mensageria com Kafka, acompanhamento de status do lote e visualização dos leads processados.

## Tecnologias Utilizadas

### Backend
- Java 21
- Spring Boot 3
- Spring Data JPA
- PostgreSQL
- Apache Kafka
- Flyway Migration
- Swagger / OpenAPI
- JUnit 5
- Mockito

### Frontend
- React
- Vite


O processamento dos arquivos CSV ocorre em chunks configuráveis, utilizando execução multi-thread para ganho de performance e escalabilidade.

---

Para realizar teste utilize o arquivo `arquivo-para-teste-leads.csv` disponível na raiz do projeto, contendo dados de exemplo para upload.

---

# Execução Local

## Pré-requisitos

### Backend
- Java 21+
- Maven 3.9+

### Frontend

É necessário utilizar Node.js compatível com o Vite.

Versão mínima recomendada:

```txt
Node.js 20.19+
```

ou

```txt
Node.js 22.12+
```

Caso utilize versão inferior, ocorrerá o erro:

```txt
You are using Node.js 16.20.2.
Vite requires Node.js version 20.19+ or 22.12+.
Please upgrade your Node.js version.
```

---

## Executando Backend

Acesse a pasta do backend:

```bash
cd backend
```

Execute:

```bash
mvn spring-boot:run
```

A aplicação será iniciada na porta:

```txt
http://localhost:8080
```

Swagger:

```txt
http://localhost:8080/swagger-ui/index.html
```

---

## Executando Frontend

Acesse a pasta do frontend:

```bash
cd frontend
```

Instale dependências:

```bash
npm install
```

Execute:

```bash
npm run dev
```

O frontend será iniciado na porta:

```txt
http://localhost:5173/
```

Exemplo esperado:

```txt
VITE v8.0.14 ready

➜ Local: http://localhost:5173/
```

---

## Testes

O projeto possui testes unitários utilizando:

- JUnit 5
- Mockito

A cobertura de testes é superior a:

```txt
80%
```

Para executar os testes:

```bash
mvn test
```

---

# Execução com Docker

O projeto possui suporte completo a Docker Compose, subindo:

- Backend Spring Boot
- Frontend React
- PostgreSQL
- Kafka

Para subir todo o ambiente execute:

```bash
docker compose up --build
```

---

## Reiniciando ambiente Docker limpo

Caso queira remover volumes, banco e mensagens persistidas do Kafka:

```bash
docker compose down -v
docker compose up --build
```

---

## Endpoints Locais

### Frontend

```txt
http://localhost:5173/
```

### Backend

```txt
http://localhost:8080
```

### Swagger

```txt
http://localhost:8080/swagger-ui/index.html
```

---

## Funcionalidades

- Upload de arquivo CSV
- Validação do arquivo
- Processamento assíncrono
- Particionamento em chunks
- Processamento multi-thread
- Persistência em PostgreSQL
- Mensageria com Kafka
- Consulta paginada de leads
- Dashboard de métricas
- Atualização de status do lote
- Swagger/OpenAPI
- Testes unitários

## Estrutura do Projeto

```txt
backend/
frontend/
docker-compose.yml
README.md
```