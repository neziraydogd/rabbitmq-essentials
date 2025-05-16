package na.library.rabbitmqessentials.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import na.library.rabbitmqessentials.constants.QueueConstants;
import na.library.rabbitmqessentials.model.Message;
import na.library.rabbitmqessentials.model.Order;
import na.library.rabbitmqessentials.model.Payment;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service class for publishing messages to RabbitMQ
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PublisherService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publish a message using the direct exchange with default delivery mode
     * @param message The message to publish
     * @param routingKey The routing key to use
     */
    public void publishToDirectExchange(Message message, String routingKey) {
        log.info("Publishing message to direct exchange with routing key: {}", routingKey);
        rabbitTemplate.convertAndSend(QueueConstants.DIRECT_EXCHANGE, routingKey, message);
    }

    /**
     * Publish a message using the direct exchange with persistent delivery mode
     * @param message The message to publish
     * @param routingKey The routing key to use
     */
    public void publishPersistentMessage(Message message, String routingKey) {
        log.info("Publishing persistent message with routing key: {}", routingKey);
        rabbitTemplate.convertAndSend(QueueConstants.DIRECT_EXCHANGE, routingKey, message, m -> {
            m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return m;
        });
    }

    /**
     * Publish a message to the fanout exchange (broadcasts to all bound queues)
     * @param message The message to broadcast
     */
    public void publishToFanoutExchange(Message message) {
        log.info("Broadcasting message to all queues bound to fanout exchange");
        rabbitTemplate.convertAndSend(QueueConstants.FANOUT_EXCHANGE, "", message);
    }

    /**
     * Publish an order message using topic exchange with appropriate routing
     * @param order The order to publish
     * @param action The action taking place (create, update, etc.)
     */
    public void publishOrderEvent(Order order, String action) {
        String routingKey = "order." + action + "." + order.getStatus().toString().toLowerCase();
        log.info("Publishing order event with routing key: {}", routingKey);
        rabbitTemplate.convertAndSend(QueueConstants.TOPIC_EXCHANGE, routingKey, order);
    }

    /**
     * Publish a payment message using topic exchange with appropriate routing
     * @param payment The payment to publish
     * @param action The action taking place (create, update, etc.)
     */
    public void publishPaymentEvent(Payment payment, String action) {
        String routingKey = "payment." + action + "." + payment.getStatus().toString().toLowerCase();
        log.info("Publishing payment event with routing key: {}", routingKey);
        rabbitTemplate.convertAndSend(QueueConstants.TOPIC_EXCHANGE, routingKey, payment);
    }

    /**
     * Publish a message using headers exchange
     * @param message The message to publish
     * @param headers The headers to use for routing
     */
    public void publishWithHeaders(Message message, Map<String, Object> headers) {
        log.info("Publishing message with headers: {}", headers);
        rabbitTemplate.convertAndSend(QueueConstants.HEADERS_EXCHANGE, "", message, m -> {
            headers.forEach((key, value) -> m.getMessageProperties().getHeaders().put(key, value));
            return m;
        });
    }
    
    /**
     * Publish a message that will be processed or sent to DLQ if processing fails
     * @param message The message to publish
     */
    public void publishToProcessQueue(Message message) {
        log.info("Publishing message to process queue");
        rabbitTemplate.convertAndSend(QueueConstants.PROCESS_EXCHANGE, QueueConstants.PROCESS_ROUTING_KEY, message);
    }
    
    /**
     * Publish a message to the delayed exchange with a specified delay
     * @param message The message to publish
     * @param delayMs The delay in milliseconds
     */
    public void publishWithDelay(Message message, long delayMs) {
        log.info("Publishing message with delay: {}ms", delayMs);
        try {
            rabbitTemplate.convertAndSend(
                QueueConstants.DELAYED_EXCHANGE, 
                QueueConstants.DELAYED_ROUTING_KEY, 
                message,
                m -> {
                    m.getMessageProperties().setDelayLong(delayMs);
                    return m;
                }
            );
        } catch (AmqpException e) {
            log.error("Error publishing delayed message: {}", e.getMessage());
        }
    }
    
    /**
     * Publish a message to the main queue with retry policy
     * @param message The message to publish
     */
    public void publishToMainQueue(Message message) {
        log.info("Publishing message to main queue with retry policy");
        rabbitTemplate.convertAndSend(QueueConstants.MAIN_EXCHANGE, QueueConstants.MAIN_ROUTING_KEY, message);
    }
    
    /**
     * Publish a message with custom properties
     * @param message The message content
     * @param priority Message priority
     * @param expiration Message expiration time in milliseconds
     * @param correlationId Message correlation ID for tracking
     */
    public void publishWithCustomProperties(Message message, Integer priority, String expiration, String correlationId) {
        log.info("Publishing message with custom properties");
        
        MessageProperties properties = new MessageProperties();
        if (priority != null) {
            properties.setPriority(priority);
        }
        if (expiration != null) {
            properties.setExpiration(expiration);
        }
        if (correlationId != null) {
            properties.setCorrelationId(correlationId);
        }
        
        // Convert message to JSON bytes
        org.springframework.amqp.core.Message amqpMessage = 
            rabbitTemplate.getMessageConverter().toMessage(message, properties);
        
        // Set delivery mode
        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        
        rabbitTemplate.send(QueueConstants.DIRECT_EXCHANGE, QueueConstants.DIRECT_ROUTING_KEY, amqpMessage);
    }
}