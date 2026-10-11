// File: OrderStatus.java
package hcmute.enums;

public enum OrderStatus {
    PENDING("Chờ duyệt"),
    DELIVERING("Đang giao"),
    COMPLETED("Hoàn thành"),
    CANCELLED("Đã hủy");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}