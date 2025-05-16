package na.library.rabbitmqessentials.consume;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import na.library.rabbitmqessentials.constants.QueueConstants;
import na.library.rabbitmqessentials.model.Message;
import na.library.rabbitmqessentials.model.Order;
import na.library.rabbitmqessentials.model.Payment;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Consumer class that demonstrates different ways to consume messages from RabbitMQ queues
 */
@Component
@Slf4j
public class MessageConsumer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public MessageConsumer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Consumes messages from the durable queue with manual acknowledgment
     * Demonstrates how to manually acknowledge messages
     */
    @RabbitListener(queues = QueueConstants.DURABLE_QUEUE, ackMode = "MANUAL",
            containerFactory = "rabbitListenerContainerFactory")
    public void consumeWithManualAck(@Payload Message message,
                                     Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("Received message from durable queue (manual ack): {}", message);

            // Process the message...
            boolean processSuccess = processMessage(message);

            if (processSuccess) {
                // Acknowledge the message
                channel.basicAck(deliveryTag, false);
                log.info("Message acknowledged: {}", deliveryTag);
            } else {
                // Reject the message (requeue=false means don't put it back in the queue)
                channel.basicReject(deliveryTag, false);
                log.info("Message rejected: {}", deliveryTag);
            }
        } catch (Exception e) {
            try {
                // Negative acknowledgment with requeue
                channel.basicNack(deliveryTag, false, true);
                log.error("Error processing message, requeued: {}", e.getMessage());
            } catch (IOException ioException) {
                log.error("Error sending nack: {}", ioException.getMessage());
            }
        }
    }

    /**
     * Consumes messages from the non-durable queue with auto acknowledgment
     * Demonstrates automatic message acknowledgment
     */
    @RabbitListener(queues = QueueConstants.NON_DURABLE_QUEUE, ackMode = "AUTO",
            containerFactory = "autoAckRabbitListenerContainerFactory")
    public void consumeWithAutoAck(Message message) {
        log.info("Received message from non-durable queue (auto ack): {}", message);
        // Process the message...
        processMessage(message);
        // Message is automatically acknowledged
    }

    /**
     * Consumes messages from the exclusive queue
     * Demonstrates consuming from an exclusive queue
     */
    @RabbitListener(queues = QueueConstants.EXCLUSIVE_QUEUE,
            containerFactory = "autoAckRabbitListenerContainerFactory")
    public void consumeFromExclusiveQueue(Message message,
                                          @Header(AmqpHeaders.CONSUMER_TAG) String consumerTag) {
        log.info("Received message from exclusive queue with consumer tag {}: {}", consumerTag, message);
        // Process the message...
        processMessage(message);
    }

    /**
     * Consumes messages from the auto-delete queue
     * Demonstrates consuming from an auto-delete queue
     */
    @RabbitListener(queues = QueueConstants.AUTO_DELETE_QUEUE,
            containerFactory = "autoAckRabbitListenerContainerFactory")
    public void consumeFromAutoDeleteQueue(Message message) {
        log.info("Received message from auto-delete queue: {}", message);
        // Process the message...
        processMessage(message);
    }

    /**
     * Consumes order messages from the order queue
     * Demonstrates consuming domain-specific messages
     * send auto-ack
     */
    @RabbitListener(queues = QueueConstants.ORDER_QUEUE,
            containerFactory = "autoAckRabbitListenerContainerFactory")
    public void consumeOrder(Order order,
                             @Header("amqp_receivedRoutingKey") String routingKey) {
        log.info("Received order with routing key {}: {}", routingKey, order);
        // Process the order...
    }

    /**
     * Consumes payment messages from the payment queue
     * Demonstrates consuming domain-specific messages
     */
    @RabbitListener(queues = QueueConstants.PAYMENT_QUEUE,
            containerFactory = "autoAckRabbitListenerContainerFactory")
    public void consumePayment(Payment payment,
                               @Header("amqp_receivedRoutingKey") String routingKey) {
        log.info("Received payment with routing key {}: {}", routingKey, payment);
        // Process the payment...
    }

    /**
     * Consumes header-based messages
     * Demonstrates consuming messages routed by headers
     */
    @RabbitListener(queues = QueueConstants.HEADERS_QUEUE,
            containerFactory = "autoAckRabbitListenerContainerFactory")
    public void consumeHeaderMessage(Message message,
                                     @Header("format") String format,
                                     @Header("type") String type) {
        log.info("Received header-routed message with format: {}, type: {}, message: {}",
                format, type, message);
        // Process the message...
    }

    /**
     * Consumes messages from the dead letter queue
     * Demonstrates handling messages that have been dead-lettered
     */
    @RabbitListener(queues = QueueConstants.DEAD_LETTER_QUEUE,
            containerFactory = "autoAckRabbitListenerContainerFactory")
    public void consumeDeadLetterQueue(Message message,
                                       @Header(name = "x-death", required = false) Map<String, Object> death) {
        if (death != null) {
            log.info("Received message in DLQ originally from: {}, count: {}",
                    death.get("queue"), death.get("count"));
        }
        log.info("Processing dead letter message: {}", message);
        // Handle the dead-lettered message...
    }

    /**
     * Consumes messages from the main queue with retry mechanism
     * Demonstrates handling retries and error scenarios
     */
    @RabbitListener(queues = QueueConstants.MAIN_QUEUE,
            containerFactory = "rabbitListenerContainerFactory")
    public void consumeWithRetry(@Payload Message message,
                                 Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                 @Header(value = "x-retry-count", defaultValue = "0") int retryCount) {
        try {
            log.info("Received message in main queue, retry count: {}, message: {}",
                    retryCount, message);

            // Simulate processing that might fail
            if (message.getType() == Message.MessageType.ERROR && retryCount < 3) {
                throw new RuntimeException("Simulated error, will retry");
            }

            // Process the message...
            processMessage(message);

            // Acknowledge the message
            channel.basicAck(deliveryTag, false);
            log.info("Message processed successfully");

        } catch (Exception e) {
            try {
                // Negative acknowledgment - will go to dead letter exchange (retry queue)
                channel.basicNack(deliveryTag, false, false);
                log.error("Error processing message, sending to retry queue: {}", e.getMessage());
            } catch (IOException ioException) {
                log.error("Error sending nack: {}", ioException.getMessage());
            }
        }
    }

    /**
     * Consumes messages from the delayed queue
     * Demonstrates handling delayed messages
     */
    @RabbitListener(queues = QueueConstants.DELAYED_QUEUE,
            containerFactory = "autoAckRabbitListenerContainerFactory")
    public void consumeDelayedMessage(Message message) {
        log.info("Received delayed message: {}", message);
        // Process the delayed message...
        processMessage(message);
    }

    @RabbitListener(queues = QueueConstants.FANOUT_QUEUE_1,
            containerFactory = "autoAckRabbitListenerContainerFactory")
    public void consumeFanoutMessage1(Message message) {
        log.info("FANOUT_QUEUE_1 Received delayed message: {}", message);
        // Process the delayed message...
        processMessage(message);
    } //Fanout exchange performs parallel(thread) propagation, each queue delivers the message to its own consumer.
    @RabbitListener(queues = QueueConstants.FANOUT_QUEUE_2,
            containerFactory = "autoAckRabbitListenerContainerFactory")
    public void consumeFanoutMessage2(Message message) {
        log.info("FANOUT_QUEUE_2 Received delayed message: {}", message);
        // Process the delayed message...
        processMessage(message);
    }

    /**
     * Simulates message processing
     *
     * @param message The message to process
     * @return true if processing was successful, false otherwise
     */
    private boolean processMessage(Message message) {
        // Simulate processing
        try {
            // Simulate processing time
            Thread.sleep(100);

            // Simulate failure for certain message types
            if (message.getType() == Message.MessageType.CRITICAL) {
                log.warn("Failed to process CRITICAL message: {}", message);
                return false;
            }

            log.info("Successfully processed message: {}", message);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Processing interrupted: {}", e.getMessage());
            return false;
        }
    }
}