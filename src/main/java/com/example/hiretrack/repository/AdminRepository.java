package com.example.hiretrack.repository;


import com.example.hiretrack.jooq.tables.records.UsersRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.hiretrack.jooq.tables.Users.USERS;

@Repository
public class AdminRepository {
    private final DSLContext dsl;

    public AdminRepository(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<UsersRecord> getAllUsersByRole(String role, String status, String search, int page, int size) {
        Condition condition = buildUserCondition(role, status, search);

        return dsl.selectFrom(USERS)
                .where(condition)
                .orderBy(USERS.CREATED_AT.desc())
                .limit(size)
                .offset(page * size)
                .fetch();
    }

    public long getUsersCountByRole(String role, String status, String search) {
        Condition condition = buildUserCondition(role, status, search);

        return dsl.select(DSL.count())
                .from(USERS)
                .where(condition)
                .fetchOne(0, Long.class);
    }

    private Condition buildUserCondition(String role, String status, String search) {
        Condition condition = USERS.ROLE.eq(role);

        if (status != null && !status.isBlank()) {
            boolean isActive = Boolean.parseBoolean(status);
            condition = condition.and(USERS.ACTIVE.eq(isActive));
        }

        if (search != null && !search.isBlank()) {
            String searchPattern = "%" + search.trim() + "%";
            condition = condition.and(
                    USERS.FULL_NAME.likeIgnoreCase(searchPattern)
                            .or(USERS.EMAIL.likeIgnoreCase(searchPattern))
            );
        }

        return condition;
    }

}
