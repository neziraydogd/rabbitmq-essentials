package na.library.rabbitmqessentials.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import na.library.rabbitmqessentials.model.Message;
import na.library.rabbitmqessentials.model.Order;
import na.library.rabbitmqessentials.model.Payment;
import na.library.rabbitmqessentials.service.PublisherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for RabbitMQ publishing operations
 */
@RestController
@RequestMapping("/api/rabbitmq")
@RequiredArgsConstructor
@Slf4j
public class RabbitMQController {

    private final PublisherService publisherService;

    /**
     * Publish a message to the direct exchange
     *
     * @param message    The message to publish
     * @param routingKey Optional routing key (defaults to direct routing key if not provided)
     * @return Response with confirmation
     */
    @PostMapping("/direct") //Publisher → Exchange → [Binding + Routing Key] → Queue → Consumer
    public ResponseEntity<String> publishToDirectExchange(
            @RequestBody Message message,
            @RequestParam(required = false, defaultValue = "direct.key") String routingKey) {

        publisherService.publishToDirectExchange(message, routingKey);
        return ResponseEntity.ok("Message published to direct exchange with routing key: " + routingKey);
    }

    /**
     * Publish a persistent message to the direct exchange
     *
     * @param message    The message to publish
     * @param routingKey Optional routing key (defaults to direct routing key if not provided)
     * @return Response with confirmation
     */
    @PostMapping("/direct/persistent")
    public ResponseEntity<String> publishPersistentMessage(
            @RequestBody Message message,
            @RequestParam(required = false, defaultValue = "direct.key") String routingKey) {

        publisherService.publishPersistentMessage(message, routingKey);
        return ResponseEntity.ok("Persistent message published with routing key: " + routingKey);
    }

    /**
     * Publish a message to the fanout exchange
     *
     * @param message The message to broadcast
     * @return Response with confirmation
     */
    @PostMapping("/fanout")
    public ResponseEntity<String> publishToFanoutExchange(@RequestBody Message message) {
        publisherService.publishToFanoutExchange(message);
        return ResponseEntity.ok("Message broadcast to all queues bound to fanout exchange");
    }

    /**
     * Publish an order event to the topic exchange
     *
     * @param order  The order to publish
     * @param action The action taking place (create, update, etc.)
     * @return Response with confirmation
     */
    @PostMapping("/topic/order")
    public ResponseEntity<String> publishOrderEvent(
            @RequestBody Order order,
            @RequestParam String action) {

        publisherService.publishOrderEvent(order, action);
        return ResponseEntity.ok("Order event published with action: " + action);
    }

    /**
     * Publish a payment event to the topic exchange
     *
     * @param payment The payment to publish
     * @param action  The action taking place (create, update, etc.)
     * @return Response with confirmation
     */
    @PostMapping("/topic/payment")
    public ResponseEntity<String> publishPaymentEvent(
            @RequestBody Payment payment,
            @RequestParam String action) {

        publisherService.publishPaymentEvent(payment, action);
        return ResponseEntity.ok("Payment event published with action: " + action);
    }

    /**
     * Publish a message using headers exchange
     *
     * @param message The message to publish
     * @param headers The headers to use for routing
     * @return Response with confirmation
     */
    @PostMapping("/headers")
    public ResponseEntity<String> publishWithHeaders(
            @RequestBody Message message,
            @RequestParam Map<String, Object> headers) {
        //@RequestHeader Map<String, String> headers
        publisherService.publishWithHeaders(message, headers);
        return ResponseEntity.ok("Message published with headers: " + headers);
    }

    /**
     * Publish a message that will be processed or sent to DLQ if processing fails
     *
     * @param message The message to publish
     * @return Response with confirmation
     */
    @PostMapping("/dlq-process")
    public ResponseEntity<String> publishToProcessQueue(@RequestBody Message message) {
        publisherService.publishToProcessQueue(message);
        return ResponseEntity.ok("Message published to process queue");
    }

    /**
     * Publish a message to the delayed exchange with a specified delay
     *
     * @param message The message to publish
     * @param delayMs The delay in milliseconds
     * @return Response with confirmation
     */
    @PostMapping("/delayed")
    public ResponseEntity<String> publishWithDelay(
            @RequestBody Message message,
            @RequestParam(defaultValue = "5000") long delayMs) {

        publisherService.publishWithDelay(message, delayMs);
        return ResponseEntity.ok("Message published with delay: " + delayMs + "ms");
    }

    /**
     * Publish a message to the main queue with retry policy
     *
     * @param message The message to publish
     * @return Response with confirmation
     */
    @PostMapping("/retry")
    public ResponseEntity<String> publishToMainQueue(@RequestBody Message message) {
        publisherService.publishToMainQueue(message);
        return ResponseEntity.ok("Message published to main queue with retry policy");
    }

    /**
     * Publish a message with custom properties
     *
     * @param message       The message content
     * @param priority      Optional message priority
     * @param expiration    Optional message expiration time in milliseconds
     * @param correlationId Optional message correlation ID for tracking
     * @return Response with confirmation
     */
    @PostMapping("/custom-properties")
    public ResponseEntity<String> publishWithCustomProperties(
            @RequestBody Message message,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) String expiration,
            @RequestParam(required = false) String correlationId) {

        // Generate correlation ID if not provided
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        publisherService.publishWithCustomProperties(message, priority, expiration, correlationId);

        return ResponseEntity.ok("Message published with custom properties" +
                (priority != null ? ", priority: " + priority : "") +
                (expiration != null ? ", expiration: " + expiration + "ms" : "") +
                ", correlationId: " + correlationId);
    }

    /**
     * Error handler for validation errors
     *
     * @param ex The exception thrown
     * @return Error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        log.error("Error processing request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error processing request: " + ex.getMessage());
    }
}