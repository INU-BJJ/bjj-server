package com.appcenter.BJJ.domain.item.schedule;

import com.appcenter.BJJ.domain.item.enums.ItemType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "item_task_tb")
public class ItemTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private Integer itemIdx;

    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;

    @Builder(access = AccessLevel.PRIVATE)
    private ItemTask(Long memberId, Integer itemIdx, ItemType itemType, LocalDateTime expiresAt, TaskStatus taskStatus) {
        this.memberId = memberId;
        this.itemIdx = itemIdx;
        this.itemType = itemType;
        this.expiresAt = expiresAt;
        this.taskStatus = taskStatus;
    }

    public static ItemTask create(Long memberId, Integer itemIdx, ItemType itemType, LocalDateTime expiresAt) {
        return ItemTask.builder()
                .memberId(memberId)
                .itemIdx(itemIdx)
                .itemType(itemType)
                .expiresAt(expiresAt)
                .taskStatus(TaskStatus.PENDING)
                .build();
    }

    public void updateTaskStatue(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public void updateExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
