package com.example.hiretrack.security;

import com.example.hiretrack.jooq.tables.records.UsersRecord;
import org.jooq.DSLContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.hiretrack.jooq.tables.Users.USERS;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final DSLContext dsl;

    public UserDetailsServiceImpl(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UsersRecord user = dsl.selectFrom(USERS)
                .where(USERS.EMAIL.eq(email))
                .fetchOne();

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + email);
        }

        return new User(
                user.getEmail(),
                user.getPasswordHash(),
                user.getActive(), true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}