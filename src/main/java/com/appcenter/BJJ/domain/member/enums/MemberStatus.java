package com.appcenter.BJJ.domain.member.enums;

import com.appcenter.BJJ.global.exception.CustomException;
import com.appcenter.BJJ.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum MemberStatus {
    ACTIVE {
        @Override
        public void valid() {
            //활동 상태
        }
    }, SUSPENDED {
        @Override
        public void valid() {
            //리뷰 작성 정지 상태
            //리뷰 작성에서만 blocking
        }
    }, BAN {
        @Override
        public void valid() {
            //밴 상태
            throw new CustomException(ErrorCode.MEMBER_BANNED);
        }
    };

    public abstract void valid();
}
