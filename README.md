# RabbitMQ Spring Integration Project

## Table of Contents
- [Introduction to RabbitMQ](#introduction-to-rabbitmq)
- [Core Concepts](#core-concepts)
    - [Messaging Model](#messaging-model)
    - [Exchange Types](#exchange-types)
    - [Queue Features](#queue-features)
    - [Message Properties](#message-properties)
    - [Consumer Acknowledgments](#consumer-acknowledgments)
    - [Message Reliability](#message-reliability)
- [Spring AMQP Integration](#spring-amqp-integration)
    - [RabbitTemplate](#rabbittemplate)
    - [Annotations](#annotations)
    - [Configuration](#configuration)


## Introduction to RabbitMQ

RabbitMQ is a message broker that implements the Advanced Message Queuing Protocol (AMQP). It serves as a middleware that enables reliable, asynchronous communication between different parts of an application or between separate applications. RabbitMQ receives, stores, and forwards messages between producers and consumers, providing a decoupling that increases the scalability and fault tolerance of distributed systems.

Key benefits of using RabbitMQ include:
- **Decoupling**: Services don't need direct knowledge of each other
- **Scalability**: Easy to scale producers or consumers independently
- **Reliability**: Messages persist even if a consumer is temporarily unavailable
- **Flexibility**: Various exchange types and routing options
- **Resilience**: Enhanced fault tolerance with message acknowledgments and persistence

## Core Concepts

### Messaging Model

RabbitMQ's messaging model consists of several key components:

1. **Producer**: Application that sends messages to an exchange
2. **Exchange**: Receives messages from producers and routes them to queues
3. **Queue**: Buffer that stores messages until consumed
4. **Binding**: Rules that tell exchanges which queues to route messages to
5. **Consumer**: Application that receives messages from queues
6. **Connection**: A physical TCP connection to the RabbitMQ server
7. **Channel**: A virtual connection inside a connection

### Exchange Types

RabbitMQ supports four exchange types, each with different routing behavior:

1. **Direct Exchange**: Routes messages based on an exact match between the routing key and the binding key.
   ```
   # Example: Messages with routing key "logs.error" will only go to queues 
   # bound with binding key "logs.error"
   ```

2. **Topic Exchange**: Routes messages based on wildcard pattern matching between the routing key and binding key.
   ```
   # Example: Queues bound with "logs.#" receive messages with routing keys like 
   # "logs.error", "logs.warn.system", etc.
   # * (star) replaces exactly one word
   # # (hash) replaces zero or more words
   ```

3. **Fanout Exchange**: Broadcasts all messages to all queues bound to it, ignoring any routing keys.
   ```
   # Example: Every queue bound to this exchange gets a copy of each message
   ```

4. **Headers Exchange**: Routes based on message header attributes rather than routing key.
   ```
   # Example: Messages with headers matching the binding attributes will be routed
   ```

### Queue Features

Queues can be configured with various properties:

1. **Durable**: Queues survive broker restarts if declared as durable.
   ```java
   // Example: Declaring a durable queue
   Queue queue = QueueBuilder.durable("my-durable-queue").build();
   ```

2. **Exclusive**: Used only by one connection and deleted when the connection closes.
   ```java
   // Example: Declaring an exclusive queue
   Queue queue = QueueBuilder.nonDurable("my-exclusive-queue").exclusive().build();
   ```

3. **Auto-delete**: Queue is deleted when the last consumer unsubscribes.
   ```java
   // Example: Declaring an auto-delete queue
   Queue queue = QueueBuilder.nonDurable("my-auto-delete-queue").autoDelete().build();
   ```

4. **TTL (Time-to-Live)**: How long a message can remain in a queue before being discarded.
   ```java
   // Example: Setting message TTL to 60000ms (1 minute)
   Queue queue = QueueBuilder.durable("my-ttl-queue")
                   .withArgument("x-message-ttl", 60000)
                   .build();
   ```

5. **Max Length**: Maximum number of messages or bytes a queue can hold.
   ```java
   // Example: Setting max length to 1000 messages
   Queue queue = QueueBuilder.durable("my-limited-queue")
                   .withArgument("x-max-length", 1000)
                   .build();
   ```

### Message Properties

When publishing messages, you can set various properties:

1. **Persistent**: Messages marked as persistent will be saved to disk and survive broker restarts.
   ```java
   // Example: Setting message as persistent
   rabbitTemplate.convertAndSend("exchange", "routing.key", message, m -> {
       m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
       return m;
   });
   ```

2. **Message ID**: Unique identifier for the message.
   ```java
   // Example: Setting message ID
   rabbitTemplate.convertAndSend("exchange", "routing.key", message, m -> {
       m.getMessageProperties().setMessageId("unique-id-123");
       return m;
   });
   ```

3. **Content Type**: MIME type of the message content.
   ```java
   // Example: Setting content type
   rabbitTemplate.convertAndSend("exchange", "routing.key", message, m -> {
       m.getMessageProperties().setContentType("application/json");
       return m;
   });
   ```

### Consumer Acknowledgments

RabbitMQ supports different acknowledgment modes:

1. **Auto Acknowledgment**: Messages are automatically acknowledged once delivered to the consumer.
   ```java
   // Example: Auto-acknowledgment (acknowledge parameter set to false)
   channel.basicConsume("queue-name", true, consumer);
   ```

2. **Manual Acknowledgment**: Consumer must explicitly acknowledge messages after processing.
   ```java
   // Example: Manual acknowledgment
   @RabbitListener(queues = "queue-name", ackMode = "MANUAL")
   public void receiveMessage(Message message, Channel channel) throws IOException {
       try {
           // Process message
           channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
       } catch (Exception e) {
           // Reject and requeue message
           channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
       }
   }
   ```

3. **Prefetch Count**: Limits the number of unacknowledged messages a consumer can have.
   ```java
   // Example: Setting prefetch count to 10
   @Bean
   public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
           ConnectionFactory connectionFactory) {
       SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
       factory.setConnectionFactory(connectionFactory);
       factory.setPrefetchCount(10);
       return factory;
   }
   ```

4. **Consumer Tag**: Unique identifier assigned to a consumer when it subscribes to a queue.
   ```java
   // Example: Getting consumer tag in a listener
   @RabbitListener(queues = "queue-name")
   public void receiveMessage(Message message) {
       String consumerTag = message.getMessageProperties().getConsumerTag();
       // Process message
   }
   ```

### Message Reliability

RabbitMQ provides several mechanisms for reliable messaging:

1. **Publisher Confirms**: Broker confirms that message has been received.
   ```java
   // Example: Enabling publisher confirms
   @Bean
   public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
       RabbitTemplate template = new RabbitTemplate(connectionFactory);
       template.setConfirmCallback((correlation, ack, reason) -> {
           if (ack) {
               log.info("Message confirmed: {}", correlation);
           } else {
               log.error("Message not confirmed: {}", reason);
           }
       });
       return template;
   }
   ```

2. **Return Callbacks**: Publisher is notified if message cannot be routed to any queue.
   ```java
   // Example: Setting return callback
   @Bean
   public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
       RabbitTemplate template = new RabbitTemplate(connectionFactory);
       template.setReturnsCallback(returned -> {
           log.error("Message returned: {}", returned.getMessage());
       });
       return template;
   }
   ```

## Spring AMQP Integration

Spring AMQP provides a higher-level abstraction over RabbitMQ's Java client, making it easier to work with RabbitMQ in Spring applications.

### RabbitTemplate

The `RabbitTemplate` is the central class for sending messages:

```java
@Service
public class MessagePublisher {
    private final RabbitTemplate rabbitTemplate;
    
    public MessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    public void sendMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
```

### Annotations

Spring AMQP provides annotations to simplify consumer creation:

1. **@RabbitListener**: Marks a method as a message listener.
   ```java
   @Component
   public class MessageConsumer {
       @RabbitListener(queues = "my-queue")
       public void receiveMessage(String message) {
           System.out.println("Received: " + message);
       }
   }
   ```

2. **@Queue**: Declares a queue.
   ```java
   @RabbitListener(bindings = @QueueBinding(
       value = @Queue("my-queue"),
       exchange = @Exchange(value = "my-exchange", type = ExchangeTypes.DIRECT),
       key = "my-routing-key"
   ))
   public void listen(String message) {
       // Process message
   }
   ```

### Configuration

Setting up RabbitMQ with Spring Boot:

```java
@Configuration
public class RabbitMQConfig {
    
    @Bean
    public Queue myQueue() {
        return new Queue("my-queue", true);
    }
    
    @Bean
    public DirectExchange myExchange() {
        return new DirectExchange("my-exchange");
    }
    
    @Bean
    public Binding myBinding(Queue myQueue, DirectExchange myExchange) {
        return BindingBuilder.bind(myQueue).to(myExchange).with("my-routing-key");
    }
}
```