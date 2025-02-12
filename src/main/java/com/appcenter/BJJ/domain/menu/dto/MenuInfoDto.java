package com.appcenter.BJJ.domain.menu.dto;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MenuInfoDto {
    private final Long menuId;
    private final String menuName;
    private final String cafeteriaName;
    private final String cafeteriaCorner;

}
