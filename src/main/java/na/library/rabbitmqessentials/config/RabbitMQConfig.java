package na.library.rabbitmqessentials.config;

import na.library.rabbitmqessentials.constants.QueueConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    // Message converter for serializing/deserializing messages as JSON
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate configuration with JSON message converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        // Enable publisher confirms for reliable publishing
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.out.println("Failed to confirm message delivery: " + cause);
            }
        });
        return rabbitTemplate;
    }

    // Configure the RabbitListener container factory
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        // Set prefetch count to control how many messages consumer fetches at once
        factory.setPrefetchCount(1);
        // Default to manual acknowledgment mode
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }

    // Create container factory with AUTO acknowledgment mode
    @Bean
    public SimpleRabbitListenerContainerFactory autoAckRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setPrefetchCount(10); // Higher prefetch count for auto-ack scenarios
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return factory;
    }

    /*
     * DIRECT EXCHANGE AND QUEUES
     */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(QueueConstants.DIRECT_EXCHANGE, true, false);
    }

    @Bean
    public Queue durableQueue() {
        return QueueBuilder.durable(QueueConstants.DURABLE_QUEUE)
                .build();
    }

    @Bean
    public Binding directBinding() {
        return BindingBuilder.bind(durableQueue())
                .to(directExchange())
                .with(QueueConstants.DIRECT_ROUTING_KEY);
    }
// ----------------------------------------------------
    @Bean
    public Queue nonDurableQueue() {
        return QueueBuilder.nonDurable(QueueConstants.NON_DURABLE_QUEUE)
                .build();
    }

    @Bean
    public Binding nonDurableBinding() {
        return BindingBuilder.bind(nonDurableQueue())
                .to(directExchange())
                .with(QueueConstants.NON_DURABLE_ROUTING_KEY);
    }
// ---------------------------------------------------------
    @Bean
    public Queue exclusiveQueue() {
        return QueueBuilder.nonDurable(QueueConstants.EXCLUSIVE_QUEUE)
                .exclusive()
                .build();
    }

    @Bean
    public Binding exclusiveBinding() {
        return BindingBuilder.bind(exclusiveQueue())
                .to(directExchange())
                .with(QueueConstants.EXCLUSIVE_ROUTING_KEY);
    }
// ---------------------------------------------------------------
    @Bean
    public Queue autoDeleteQueue() {
        return QueueBuilder.nonDurable(QueueConstants.AUTO_DELETE_QUEUE)
                .autoDelete()
                .build();
    }

    @Bean
    public Binding autoDeleteBinding() {
        return BindingBuilder.bind(autoDeleteQueue())
                .to(directExchange())
                .with(QueueConstants.AUTO_DELETE_ROUTING_KEY);
    }
// ----------------------------------------------------------------
    /*
     * FANOUT EXCHANGE
     */
    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(QueueConstants.FANOUT_EXCHANGE, true, false);
    }

    @Bean
    public Queue fanoutQueue1() {
        return QueueBuilder.durable(QueueConstants.FANOUT_QUEUE_1).build();
    }

    @Bean
    public Queue fanoutQueue2() {
        return QueueBuilder.durable(QueueConstants.FANOUT_QUEUE_2).build();
    }

    @Bean
    public Binding fanoutBinding1() {
        return BindingBuilder.bind(fanoutQueue1()).to(fanoutExchange());
    }

    @Bean
    public Binding fanoutBinding2() {
        return BindingBuilder.bind(fanoutQueue2()).to(fanoutExchange());
    }

    /*
     * TOPIC EXCHANGE
     */
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(QueueConstants.TOPIC_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(QueueConstants.ORDER_QUEUE).build();
    }

    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(QueueConstants.PAYMENT_QUEUE).build();
    }

    @Bean
    public Queue inventoryQueue() {
        return QueueBuilder.durable(QueueConstants.INVENTORY_QUEUE).build();
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue())
                .to(topicExchange())
                .with("order.#"); // only one related message
    }

    @Bean
    public Binding paymentBinding() {
        return BindingBuilder.bind(paymentQueue())
                .to(topicExchange())
                .with("payment.#");  // All payment related messages
    }

    @Bean
    public Binding inventoryBinding() {
        return BindingBuilder.bind(inventoryQueue())
                .to(topicExchange())
                .with("inventory.#");  // All inventory related messages
    }

    /*
     * HEADERS EXCHANGE
     */
    @Bean
    public HeadersExchange headersExchange() {
        return new HeadersExchange(QueueConstants.HEADERS_EXCHANGE, true, false);
    }

    @Bean
    public Queue headersQueue() {
        return QueueBuilder.durable(QueueConstants.HEADERS_QUEUE).build();
    }

    @Bean
    public Binding headersBinding() {
        return BindingBuilder.bind(headersQueue())
                .to(headersExchange())
                .whereAll(Map.of("format", "pdf", "type", "report"))
                .match();
    }

    /*
     * DEAD LETTER QUEUE CONFIGURATION
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(QueueConstants.DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(QueueConstants.DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(QueueConstants.DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public Queue processQueue() {
        Map<String, Object> args = new HashMap<>();
        // Setup dead letter exchange and routing key
        args.put("x-dead-letter-exchange", QueueConstants.DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", QueueConstants.DEAD_LETTER_ROUTING_KEY);

        // Message TTL - messages will expire after 30 seconds if not consumed
        args.put("x-message-ttl", 30000);

        return QueueBuilder.durable(QueueConstants.PROCESS_QUEUE)
                .withArguments(args)
                .build();
    }

    @Bean
    public DirectExchange processExchange() {
        return new DirectExchange(QueueConstants.PROCESS_EXCHANGE);
    }

    @Bean
    public Binding processBinding() {
        return BindingBuilder.bind(processQueue())
                .to(processExchange())
                .with(QueueConstants.PROCESS_ROUTING_KEY);
    }

    /*
     * DELAYED MESSAGE EXCHANGE & QUEUE
    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(QueueConstants.DELAYED_EXCHANGE, "x-delayed-message", true, false, args);
    }
    @Bean
    public Queue delayedQueue() {
        return QueueBuilder.durable(QueueConstants.DELAYED_QUEUE).build();
    }

    @Bean
    public Binding delayedBinding() {
        return BindingBuilder.bind(delayedQueue())
                .to(delayedExchange())
                .with(QueueConstants.DELAYED_ROUTING_KEY)
                .noargs();
    }*/
    @Bean
    public DirectExchange delayedExchange() {
        return new DirectExchange(QueueConstants.DELAYED_EXCHANGE);
    }

    @Bean
    public Queue delayedQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 30000); // 30 saniye delay
        args.put("x-dead-letter-exchange", QueueConstants.MAIN_EXCHANGE);
        args.put("x-dead-letter-routing-key", QueueConstants.MAIN_ROUTING_KEY);

        return QueueBuilder.durable(QueueConstants.DELAYED_QUEUE)
                .withArguments(args)
                .build();
    }

    @Bean
    public Binding delayedBinding() {
        return BindingBuilder.bind(delayedQueue())
                .to(delayedExchange())
                .with(QueueConstants.DELAYED_ROUTING_KEY);
    }

    /*
     * RETRY QUEUE CONFIGURATION
     */
    @Bean
    public DirectExchange retryExchange() {
        return new DirectExchange(QueueConstants.RETRY_EXCHANGE);
    }

    @Bean
    public Queue retryQueue() {
        Map<String, Object> args = new HashMap<>();
        // Set up dead letter exchange for retry messages
        args.put("x-dead-letter-exchange", QueueConstants.MAIN_EXCHANGE);
        args.put("x-dead-letter-routing-key", QueueConstants.MAIN_ROUTING_KEY);

        // Set message TTL for retry delay (5 seconds)
        args.put("x-message-ttl", 5000);

        return QueueBuilder.durable(QueueConstants.RETRY_QUEUE)
                .withArguments(args)
                .build();
    }

    @Bean
    public Binding retryBinding() {
        return BindingBuilder.bind(retryQueue())
                .to(retryExchange())
                .with(QueueConstants.RETRY_ROUTING_KEY);
    }

    @Bean
    public DirectExchange mainExchange() {
        return new DirectExchange(QueueConstants.MAIN_EXCHANGE);
    }

    @Bean
    public Queue mainQueue() {
        Map<String, Object> args = new HashMap<>();
        // Set up dead letter exchange for failed messages
        args.put("x-dead-letter-exchange", QueueConstants.RETRY_EXCHANGE);
        args.put("x-dead-letter-routing-key", QueueConstants.RETRY_ROUTING_KEY);

        return QueueBuilder.durable(QueueConstants.MAIN_QUEUE)
                .withArguments(args)
                .build();
    }

    @Bean
    public Binding mainBinding() {
        return BindingBuilder.bind(mainQueue())
                .to(mainExchange())
                .with(QueueConstants.MAIN_ROUTING_KEY);
    }
}