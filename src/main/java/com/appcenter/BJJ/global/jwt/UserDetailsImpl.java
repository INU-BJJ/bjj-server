package com.appcenter.BJJ.global.jwt;

import com.appcenter.BJJ.domain.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private Member member;

    public String getNickname() {
        return member.getNickname();
    }

    //JWT
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(member.getRole().name()));
        return roles;
    }

    @Override
    public String getUsername() {
        return member.getProviderId();
    }

    @Override
    public String getPassword() {
        return null;
    }

}
