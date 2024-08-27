package com.appcenter.BJJ.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CafeteriaData {
    STUDENT_CAFETERIA("학생식당", "https://inucoop.com/main.php?mkey=2&w=2"),
    BUILDING_2_CAFETERIA("2호관식당", "https://inucoop.com/main.php?mkey=2&w=2&l=2"), // 2호관 식당
    DORMITORY_1_CAFETERIA("제1기숙사식당", "https://inucoop.com/main.php?mkey=2&w=2&l=3"), // 제1기숙사식당
    BUILDING_27_CAFETERIA("27호관식당", "https://inucoop.com/main.php?mkey=2&w=2&l=4"), // 27호관식당
    EDUCATION_COLLEGE_CAFETERIA("사범대식당", "https://inucoop.com/main.php?mkey=2&w=2&l=5"); // 사범대식당


    private final String name;
    private final String url;
}
