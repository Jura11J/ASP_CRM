package com.aspcrm.shop.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "OrderStatusHistory")
public class OrderStatusHistoryEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId", nullable = false)
    private OrderEntity order;

    @Enumerated(EnumType.ORDINAL)
    @JdbcTypeCode(SqlTypes.INTEGER)
    @Column(name = "Status", nullable = false, columnDefinition = "integer")
    private OrderStatus status;

    @Column(name = "ChangedAt", nullable = false)
    private OffsetDateTime changedAt;

    @Column(name = "Note", length = 200)
    private String note;

    @PrePersist
    public void prePersist() {
        if (changedAt == null) {
            changedAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public Integer getId() { return id; }

    public OrderEntity getOrder() { return order; }
    public void setOrder(OrderEntity order) { this.order = order; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public OffsetDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(OffsetDateTime changedAt) { this.changedAt = changedAt; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
