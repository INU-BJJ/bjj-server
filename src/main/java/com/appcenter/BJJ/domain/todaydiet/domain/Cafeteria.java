package com.appcenter.BJJ.domain.todaydiet.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "cafeteria_tb")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cafeteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String corner;

    private String location;

    private String semesterOperationTime; // 학기 중 운영 시간

    private String vacationOperationTime; // 방학 중 운영 시간

    private String image;

    @Builder
    private Cafeteria(String name, String corner, String location, String semesterOperationTime, String vacationOperationTime, String image) {
        this.name = name;
        this.corner = corner;
        this.location = location;
        this.semesterOperationTime = semesterOperationTime;
        this.vacationOperationTime = vacationOperationTime;
        this.image = image;
    }
}
