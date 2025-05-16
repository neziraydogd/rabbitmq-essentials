package na.library.rabbitmqessentials.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Sample message model representing a generic message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {
    
    private String id;
    private String content;
    private String sender;
    private LocalDateTime timestamp;
    private int priority;
    
    @Builder.Default
    private MessageType type = MessageType.INFO;
    
    public enum MessageType {
        INFO, 
        WARNING, 
        ERROR,
        CRITICAL
    }
}