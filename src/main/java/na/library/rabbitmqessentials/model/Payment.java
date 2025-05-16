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
 * Sample Payment model for demonstrating topic exchange
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment implements Serializable {
    private UUID id;
    private String orderId;
    private String paymentMethod;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private PaymentStatus status;
    
    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        REFUNDED
    }
}