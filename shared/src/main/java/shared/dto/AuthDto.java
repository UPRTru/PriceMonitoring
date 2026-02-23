package shared.dto;

import java.util.Objects;

public record AuthDto(String email, String password) {

    public AuthDto {
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(password, "Password cannot be null");

        if (email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
    }

    public static AuthDto of(String email, String password) {
        return new AuthDto(email, password);
    }

    public String normalizedEmail() {
        return email.trim().toLowerCase();
    }
}