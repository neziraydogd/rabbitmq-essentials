package na.library.rabbitmqessentials.constants;

/**
 * Constants used throughout the RabbitMQ application
 */
public class QueueConstants {

    // Exchange names
    public static final String DIRECT_EXCHANGE = "direct.exchange";
    public static final String FANOUT_EXCHANGE = "fanout.exchange";
    public static final String TOPIC_EXCHANGE = "topic.exchange";
    public static final String HEADERS_EXCHANGE = "headers.exchange";
    public static final String DEAD_LETTER_EXCHANGE = "dead-letter.exchange";
    public static final String PROCESS_EXCHANGE = "process.exchange";
    public static final String DELAYED_EXCHANGE = "delayed.exchange";
    public static final String RETRY_EXCHANGE = "retry.exchange";
    public static final String MAIN_EXCHANGE = "main.exchange";

    // Queue names
    public static final String DURABLE_QUEUE = "durable.queue";
    public static final String NON_DURABLE_QUEUE = "non-durable.queue";
    public static final String EXCLUSIVE_QUEUE = "exclusive.queue";
    public static final String AUTO_DELETE_QUEUE = "auto-delete.queue";
    public static final String FANOUT_QUEUE_1 = "fanout.queue.1";
    public static final String FANOUT_QUEUE_2 = "fanout.queue.2";
    public static final String ORDER_QUEUE = "order.queue";
    public static final String PAYMENT_QUEUE = "payment.queue";
    public static final String INVENTORY_QUEUE = "inventory.queue";
    public static final String HEADERS_QUEUE = "headers.queue";
    public static final String DEAD_LETTER_QUEUE = "dead-letter.queue";
    public static final String PROCESS_QUEUE = "process.queue";
    public static final String DELAYED_QUEUE = "delayed.queue";
    public static final String RETRY_QUEUE = "retry.queue";
    public static final String MAIN_QUEUE = "main.queue";

    // Routing keys
    public static final String DIRECT_ROUTING_KEY = "direct.key";
    public static final String NON_DURABLE_ROUTING_KEY = "non-durable.key";
    public static final String EXCLUSIVE_ROUTING_KEY = "exclusive.key";
    public static final String AUTO_DELETE_ROUTING_KEY = "auto-delete.key";
    public static final String DEAD_LETTER_ROUTING_KEY = "dead-letter.key";
    public static final String PROCESS_ROUTING_KEY = "process.key";
    public static final String DELAYED_ROUTING_KEY = "delayed.key";
    public static final String RETRY_ROUTING_KEY = "retry.key";
    public static final String MAIN_ROUTING_KEY = "main.key";
    
    // Consumer tags
    public static final String CONSUMER_TAG_PREFIX = "consumer-";
    
    private QueueConstants() {
        // Private constructor to prevent instantiation
    }
}