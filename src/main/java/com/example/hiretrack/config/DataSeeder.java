package com.example.hiretrack.config;

import org.jooq.DSLContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static com.example.hiretrack.jooq.tables.Users.USERS;

@Component
public class DataSeeder implements CommandLineRunner {

    private final DSLContext dsl;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(DSLContext dsl, PasswordEncoder passwordEncoder) {
        this.dsl = dsl;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (dsl.fetchCount(USERS) == 0) {
            String hashedPassword = passwordEncoder.encode("password123");

            dsl.insertInto(USERS, USERS.EMAIL, USERS.PASSWORD_HASH, USERS.FULL_NAME, USERS.ROLE)
                    .values("admin@test.com", hashedPassword, "Admin User", "ADMIN")
                    .values("recruiter@test.com", hashedPassword, "HR Recruiter", "RECRUITER")
                    .values("interviewer@test.com", hashedPassword, "Tech Interviewer", "INTERVIEWER")
                    .execute();

            System.out.println("🔥 Seeded 3 demo users (password: password123)");
        }
    }
}