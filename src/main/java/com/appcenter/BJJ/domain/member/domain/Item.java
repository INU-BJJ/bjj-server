package com.appcenter.BJJ.domain.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "item_tb")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String type;

    private int price;

    private String level;

    private String path;

    @Builder
    private Item(String name, String type, int price, String level, String path) {
        this.name = name;
        this.type= type;
        this.price = price;
        this.level = level;
        this.path = path;
    }
}
