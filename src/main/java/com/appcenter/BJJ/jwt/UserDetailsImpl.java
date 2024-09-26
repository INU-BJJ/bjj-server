package com.appcenter.BJJ.jwt;

import com.appcenter.BJJ.domain.Member;
import com.appcenter.BJJ.oauth.OAuth2UserInfo;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class UserDetailsImpl implements UserDetails, OAuth2User {
    private final Member member;
    private OAuth2UserInfo oAuth2UserInfo;

    public UserDetailsImpl(Member member) {
        this.member = member;
    }

    //JWT
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority(member.getRole()));
        return roles;
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }


    //OAuth2
    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
