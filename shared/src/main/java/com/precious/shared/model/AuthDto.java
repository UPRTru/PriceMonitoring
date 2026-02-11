package com.precious.shared.model;

public class AuthDto {

    private final String email;
    private final String password;

    public AuthDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean equals(Object authDto) {
        return toString().equals(authDto.toString());
    }

    @Override
    public String toString() {
        return "Email : " + getEmail() + ", Password: " + getPassword();
    }
}
