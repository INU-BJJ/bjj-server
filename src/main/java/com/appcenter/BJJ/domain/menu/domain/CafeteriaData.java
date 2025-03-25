package com.appcenter.BJJ.domain.menu.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CafeteriaData {
    STUDENT_CAFETERIA           ("학생식당", "https://www.inu.ac.kr/inu/643/subview.do?enc=Zm5jdDF8QEB8JTJGZGluaW5nUm9vbSUyRmludSUyRnZpZXcuZG8lM0ZkYXlUeXBlJTNEd2VlayUyNnJvb21UeXBlJTNEMSUyNg%3D%3D"),
    BUILDING_2_CAFETERIA        ("2호관식당", "https://www.inu.ac.kr/inu/643/subview.do?enc=Zm5jdDF8QEB8JTJGZGluaW5nUm9vbSUyRmludSUyRnZpZXcuZG8lM0ZkYXlUeXBlJTNEd2VlayUyNnJvb21UeXBlJTNEMyUyNg%3D%3D"),
    DORMITORY_1_CAFETERIA       ("제1기숙사식당", "https://www.inu.ac.kr/inu/643/subview.do?enc=Zm5jdDF8QEB8JTJGZGluaW5nUm9vbSUyRmludSUyRnZpZXcuZG8lM0ZkYXlUeXBlJTNEd2VlayUyNnJvb21UeXBlJTNEMiUyNg%3D%3D"),
    BUILDING_27_CAFETERIA       ("27호관식당", "https://www.inu.ac.kr/inu/643/subview.do?enc=Zm5jdDF8QEB8JTJGZGluaW5nUm9vbSUyRmludSUyRnZpZXcuZG8lM0ZkYXlUeXBlJTNEd2VlayUyNnJvb21UeXBlJTNENCUyNg%3D%3D"),
    EDUCATION_COLLEGE_CAFETERIA ("사범대식당", "https://www.inu.ac.kr/inu/643/subview.do?enc=Zm5jdDF8QEB8JTJGZGluaW5nUm9vbSUyRmludSUyRnZpZXcuZG8lM0ZkYXlUeXBlJTNEd2VlayUyNnJvb21UeXBlJTNENSUyNg%3D%3D");

    private final String name;
    private final String url;
}
