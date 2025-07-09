package com.appcenter.BJJ.domain.item.schedule;

import com.appcenter.BJJ.domain.item.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ItemTaskRepository extends JpaRepository<ItemTask, Long> {

    @Query("""
            SELECT task
            FROM ItemTask task
            WHERE task.taskStatus = :taskStatus
            """)
    List<ItemTask> findAllByTaskStatus(TaskStatus taskStatus);

    Optional<ItemTask> findByMemberIdAndItemIdxAndItemType(Long memberId, Integer ItemIdx, ItemType itemType);
}
