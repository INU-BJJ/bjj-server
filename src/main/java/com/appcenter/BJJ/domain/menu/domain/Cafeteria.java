package com.appcenter.BJJ.domain.menu.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    @GeneratedValue
    private Long id;

    private String name;

    private String corner;

    private String location;

    private String operationTime;

    private String image;

    @Builder
    private Cafeteria(String name, String corner, String location, String operationTime, String image) {
        this.name = name;
        this.corner = corner;
        this.location = location;
        this.operationTime = operationTime;
        this.image = image;
    }
}
