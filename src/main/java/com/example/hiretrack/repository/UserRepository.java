package com.example.hiretrack.repository;


import static com.example.hiretrack.jooq.tables.Users.USERS;
import com.example.hiretrack.jooq.tables.records.UsersRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {
    private final DSLContext dsl;

    public UserRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<UsersRecord> findbyEmail(String email){
        return Optional.ofNullable(
                dsl.selectFrom(USERS).where(USERS.EMAIL.equal(email)).fetchOne()
        );
    }
}
