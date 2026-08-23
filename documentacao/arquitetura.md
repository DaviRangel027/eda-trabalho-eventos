# Documentação Arquitetural — Sistema de Gerenciamento de Eventos e Ingressos

## 1. Introdução

O sistema resolve o problema de gerenciamento de eventos e venda de ingressos, coordenando três responsabilidades que naturalmente pertencem a domínios diferentes: a criação e definição de um evento, a emissão e controle de ingressos, e o controle financeiro (arrecadação) desse evento.

A motivação para usar Arquitetura Orientada a Eventos (EDA) é o desacoplamento entre esses domínios: o serviço que cria o evento não precisa saber como os ingressos são gerados nem como o dinheiro é controlado — ele apenas anuncia que "um evento foi criado" e "um ingresso foi comprado", e cada serviço interessado reage de forma independente e assíncrona. Isso permite que os serviços evoluam, escalem e até falhem de forma isolada, sem que uma falha no `payment-service`, por exemplo, impeça a criação de eventos.

## 2. Arquitetura

### 2.1 Componentes

| Serviço | Porta | Papel |
|---|---|---|
| event-service | 8081 | Produtor — cria eventos e publica `EventCreated` |
| ticket-service | 8082 | Consumidor de `EventCreated` (gera ingressos) e produtor de `TicketPurchased` (ao vender um ingresso) |
| payment-service | 8083 | Consumidor de `EventCreated` (abre o ledger) e de `TicketPurchased` (atualiza a arrecadação) |

### 2.2 Fluxo de Eventos

```
┌────────────────┐
│  event-service  │
│  POST /events   │
└────────┬────────┘
         │ publica EventCreated
         │ (TopicExchange: event.exchange | routing key: event.created)
         ▼
   ┌─────────────┐
   │  RabbitMQ   │
   └──────┬──────┘
          │
    ┌─────┴─────┐
    ▼           ▼
┌─────────┐ ┌──────────────┐
│ ticket- │ │  payment-     │
│ service │ │  service      │
│         │ │               │
│ gera N  │ │ abre ledger   │
│ tickets │ │ (status OPEN) │
└────┬────┘ └───────────────┘
     │
     │ POST /tickets/{id}/purchase
     │ publica TicketPurchased
     │ (DirectExchange: payment.exchange | routing key: ticket.purchased)
     ▼
┌─────────────┐
│  RabbitMQ   │
└──────┬──────┘
       ▼
┌───────────────────┐
│  payment-service   │
│  atualiza ledger   │
│  fecha se atingir  │
│  a receita esperada│
└────────────────────┘
```

### 2.3 Persistência

Cada serviço mantém seu próprio banco H2 em memória, isolado dos demais:

| Serviço | Banco |
|---|---|
| event-service | `eventdb` |
| ticket-service | `ticketdb` |
| payment-service | `paymentdb` |

## 3. Decisões Técnicas

### 3.1 Broker de Mensageria

Optamos pelo RabbitMQ rodando via Docker Compose local, conforme exigência do enunciado. O RabbitMQ oferece um modelo flexível de exchanges e bindings que se encaixa bem com o padrão de publish/subscribe necessário neste sistema, além de contar com uma interface de gerenciamento (Management UI) útil para depuração durante o desenvolvimento.

### 3.2 Tipos de Exchange

O sistema utiliza **dois tipos de exchange**, escolhidos de acordo com a natureza de cada fluxo:

- **TopicExchange (`event.exchange`)** — usada para o evento `EventCreated`, que precisa alcançar múltiplos consumidores interessados (ticket-service e payment-service) a partir de uma única publicação. O uso de Topic permite futura extensão para routing keys mais específicas (ex: `event.created.vip`) sem alterar a estrutura.
- **DirectExchange (`payment.exchange`)** — usada para o evento `TicketPurchased`, que tem um único consumidor de destino (payment-service) e roteamento direto por chave exata (`ticket.purchased`), sem necessidade da flexibilidade de padrões do Topic.

### 3.3 Padrão de Mensageria

Cada serviço consumidor declara sua própria fila e binding via `RabbitMQConfig.java`, seguindo o padrão "cada serviço é dono da sua fila" — isso evita acoplamento na declaração de infraestrutura entre serviços e permite que cada um seja implantado de forma independente.

## 4. Eventos do Sistema

| Evento | Publicado por | Consumido por | Exchange | Routing Key | Campos |
|---|---|---|---|---|---|
| `EventCreated` | event-service | ticket-service, payment-service | `event.exchange` (Topic) | `event.created` | `id`, `name`, `totalTickets`, `ticketPrice` |
| `TicketPurchased` | ticket-service | payment-service | `payment.exchange` (Direct) | `ticket.purchased` | `ticketId`, `eventId`, `eventName`, `ticketCode`, `price` |

## 5. Endpoints REST

### event-service (porta 8081)

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/events` | Cria um novo evento e dispara `EventCreated` |
| GET | `/api/events` | Lista todos os eventos |

### ticket-service (porta 8082)

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/tickets` | Lista todos os ingressos |
| GET | `/api/tickets/event/{eventId}` | Lista ingressos de um evento específico |
| POST | `/api/tickets/{id}/purchase` | Compra um ingresso disponível, dispara `TicketPurchased` |

### payment-service (porta 8083)

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/payments/ledgers` | Lista os livros contábeis (ledgers) de todos os eventos |

## 6. Configuração

### 6.1 Docker Compose

O `docker-compose.yml` na raiz do projeto sobe apenas o RabbitMQ, conforme exigência do trabalho (RabbitMQ deve rodar exclusivamente via Docker):

```yaml
version: '3.8'
services:
  rabbitmq:
    image: rabbitmq:3-management-alpine
    container_name: rabbitmq-eventos
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    healthcheck:
      test: ["CMD", "rabbitmqctl", "status"]
      interval: 10s
      timeout: 5s
      retries: 5
volumes:
  rabbitmq_data:
```

### 6.2 Configuração de cada serviço

Cada serviço define, em seu `application.properties`, a conexão com o RabbitMQ (`localhost:5672`, usuário/senha `admin`/`admin`), a conexão com seu banco H2 próprio, e os nomes de exchange/fila/routing key que utiliza — ver README.md para instruções completas de execução.