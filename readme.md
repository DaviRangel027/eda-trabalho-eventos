# Sistema de Gerenciamento de Eventos e Ingressos

Trabalho prático da disciplina **Sistemas Baseados em Eventos e Mensageria Distribuída**, implementando uma Arquitetura Orientada a Eventos (EDA) com Java, Spring Boot e RabbitMQ.

## Visão Geral

O sistema é composto por 3 microsserviços independentes que se comunicam de forma assíncrona via RabbitMQ:

| Serviço | Porta | Responsabilidade |
|---|---|---|
| **event-service** | 8081 | Cria eventos e publica `EventCreated` |
| **ticket-service** | 8082 | Gera ingressos ao receber `EventCreated`; processa compras e publica `TicketPurchased` |
| **payment-service** | 8083 | Controla o livro contábil (ledger) financeiro de cada evento |

## Pré-requisitos

- Docker Desktop instalado e em execução
- JDK 21
- Maven (ou usar o wrapper `mvnw` incluído em cada serviço)

## Como Executar

### 1. Subir o RabbitMQ (Docker)

Na raiz do projeto:

```bash
docker-compose up -d
```

Verifique se subiu corretamente:

```bash
docker ps
```

Acesse a interface de gerenciamento em [http://localhost:15672](http://localhost:15672) (usuário: `admin`, senha: `admin`).

### 2. Subir os serviços Spring Boot

Em terminais separados (ou pela IDE), na pasta de cada serviço:

```bash
cd event-service
mvn spring-boot:run
```

```bash
cd ticket-service
mvn spring-boot:run
```

```bash
cd payment-service
mvn spring-boot:run
```

> **Importante:** suba o RabbitMQ antes dos serviços Spring Boot, para evitar erros de conexão na publicação dos primeiros eventos.

## Testando o Fluxo

### 1. Criar um evento

```
POST http://localhost:8081/api/events
Content-Type: application/json

{
  "name": "Show do Coldplay",
  "totalTickets": 4,
  "ticketPrice": 250.0
}
```

Isso dispara o evento `EventCreated`, que é consumido por:
- **ticket-service**, que gera os ingressos (status `AVAILABLE`)
- **payment-service**, que abre o ledger financeiro do evento (status `OPEN`)

### 2. Consultar os ingressos gerados

```
GET http://localhost:8082/api/tickets/event/{eventId}
```

### 3. Comprar um ingresso

```
POST http://localhost:8082/api/tickets/{ticketId}/purchase
```

Isso muda o status do ingresso para `SOLD` e dispara o evento `TicketPurchased`, consumido pelo **payment-service**, que atualiza o valor arrecadado no ledger. Quando o valor arrecadado atinge a receita esperada, o ledger fecha automaticamente (status `CLOSED`).

### 4. Consultar os ledgers financeiros

```
GET http://localhost:8083/api/payments/ledgers
```

## Estrutura do Repositório

```
eda-trabalho/
├── docker-compose.yml
├── README.md
├── documentacao/
│   └── arquitetura.md
├── event-service/
│   ├── pom.xml
│   └── src/main/java/com/example/event/...
├── ticket-service/
│   ├── pom.xml
│   └── src/main/java/com/example/ticket/...
└── payment-service/
    ├── pom.xml
    └── src/main/java/com/example/payment/...
```

## Tecnologias Utilizadas

- Java 21
- Spring Boot
- Spring AMQP (RabbitMQ)
- Spring Web
- Spring Data JPA
- H2 Database (em memória)
- Docker / Docker Compose