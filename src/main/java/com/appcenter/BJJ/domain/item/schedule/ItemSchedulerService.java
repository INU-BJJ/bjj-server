package com.appcenter.BJJ.domain.item.schedule;

import com.appcenter.BJJ.domain.item.enums.ItemType;
import com.appcenter.BJJ.domain.item.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemSchedulerService {
    @Qualifier("taskScheduler")
    private final TaskScheduler taskScheduler;
    private final ItemTaskRepository itemTaskRepository;
    private final InventoryRepository inventoryRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initTask() { //Scheduler의 task는 메모리에 있어서 서버 재실행시 모두 날라감 => 재실행시 init해주기 (db조회를 통해)
        List<ItemTask> itemTasks = itemTaskRepository.findAllByTaskStatus(TaskStatus.PENDING);
        for (ItemTask itemTask : itemTasks) {
            Runnable task = getTask(itemTask);
            if (itemTask.getExpiresAt().isAfter(LocalDateTime.now())) {
                taskScheduler.schedule(task, Instant.from(itemTask.getExpiresAt()));
            } else {
                taskScheduler.schedule(task, Instant.now()); //현재보다 전 => 지금 바로 스케줄러 시작
            }
        }
    }

    public void addOrUpdateTask(Long memberId, Integer itemIdx, ItemType itemType, LocalDateTime expiresAt) {
        ItemTask itemTask = itemTaskRepository.findByMemberIdAndItemIdxAndItemType(memberId, itemIdx, itemType).orElseGet(
                () -> ItemTask.create(memberId, itemIdx, itemType, expiresAt)
        );
        itemTask.updateExpiresAt(expiresAt);
        itemTask.updateTaskStatue(TaskStatus.PENDING);

        Runnable task = getTask(itemTask);
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        taskScheduler.schedule(task, itemTask.getExpiresAt().atZone(zoneId).toInstant());
        itemTaskRepository.save(itemTask);
    }

    private Runnable getTask(ItemTask itemTask) {
        return () -> {
            inventoryRepository.deleteByMemberIdAndItemIdxAndItemType(itemTask.getMemberId(), itemTask.getItemIdx(), itemTask.getItemType());
            itemTask.updateTaskStatue(TaskStatus.COMPLETE);
            itemTaskRepository.save(itemTask); //Runnable가 실행되면 영속성 컨텍스트가 사라짐 => itemTask는 detached 상태라 수동 저장해야 함
        };
    }
}
