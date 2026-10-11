// OrderStatusRequestDTO.java
package hcmute.dto.request;

import hcmute.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusRequestDTO {
    private OrderStatus status;
}