package na.library.rabbitmqessentials.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Sample Order model for demonstrating topic exchange
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {
    private UUID id;
    private String customerId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private OrderStatus status;
    
    public enum OrderStatus {
        CREATED,
        PAID,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }
}