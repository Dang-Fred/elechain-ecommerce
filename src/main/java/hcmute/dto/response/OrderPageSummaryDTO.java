package hcmute.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageSummaryDTO {
    private Long totalOrders;
    private Long pendingOrders;
    private Long deliveringOrders; // Dùng đúng DELIVERING
    private Long completedOrders;
    private Long cancelledOrders;
}