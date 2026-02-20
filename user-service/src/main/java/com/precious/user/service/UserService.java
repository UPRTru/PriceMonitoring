package com.precious.user.service;

import com.precious.shared.dto.AuthDto;
import com.precious.shared.dto.CheckPrice;
import com.precious.user.model.ScheduledPrice;
import com.precious.user.model.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface UserService {

    boolean authenticate(String email, String password);

    void deleteScheduledPrice(Long id, String email);

    void deleteUser(String email);

    User register(AuthDto authDto, String timezone);

    UserDetails getByEmail(String email);

    Optional<User> findByEmail(String email);

    void addScheduledPrice(String email, CheckPrice checkPrice);

    List<User> getAllUsers();

    StringBuilder getAllScheduledPrices(String email);

    String getUserZoneDateTime(String email);

    List<ScheduledPrice> getScheduledPrice(String email);
}