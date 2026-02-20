package com.precious.user.service;

import com.precious.shared.dto.AuthDto;
import com.precious.shared.dto.CheckPrice;
import com.precious.shared.exception.BadRequestException;
import com.precious.shared.exception.NotFoundException;
import com.precious.user.model.ScheduledPrice;
import com.precious.user.model.User;
import com.precious.user.repository.ScheduledPriceRepository;
import com.precious.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ScheduledPriceRepository scheduledPriceRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");

    @Retryable(
            value = {DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Transactional
    @Override
    public User register(AuthDto authDto, String timezone) {
        validEmail(authDto.email());
        if (userRepository.existsByEmail(authDto.email())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }
        User user = new User(authDto.email(), passwordEncoder.encode(authDto.password()), timezone);
        return userRepository.save(user);
    }

    @Retryable(
            value = {DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Transactional(readOnly = true)
    @Override
    public UserDetails getByEmail(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("Пользователь с email: " + email + " не найден."));
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    authorities
            );
        } catch (Exception e) {
            throw new NotFoundException("Пользователь с email: " + email + " не найден.");
        }
    }

    @Retryable(
            value = {DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Retryable(
            value = {DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Transactional(readOnly = true)
    @Override
    public void addScheduledPrice(String email, CheckPrice checkPrice) {
        User user = findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        ScheduledPrice scheduledPrice = new ScheduledPrice(
                user,
                checkPrice.bank().name(),
                checkPrice.typePrice().name(),
                checkPrice.currentPrice().name(),
                checkPrice.name(),
                checkPrice.price());
        scheduledPriceRepository.save(scheduledPrice);
    }

    @Retryable(
            value = {DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Transactional(readOnly = true)
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Retryable(
            value = {DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Transactional(readOnly = true)
    @Override
    public List<ScheduledPrice> getScheduledPrice(String email) {
        return scheduledPriceRepository.findByUserEmail(email);
    }

    @Retryable(
            value = {DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 1.5)
    )
    @Transactional(readOnly = true)
    public StringBuilder getAllScheduledPrices(String email) {
        User user = findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        List<ScheduledPrice> scheduledPrices = user.getScheduledPrices();
        StringBuilder stringBuilder = new StringBuilder();
        for (ScheduledPrice scheduledPrice : scheduledPrices) {
            stringBuilder.append(scheduledPrice.toString());
        }
        return stringBuilder;
    }

    @Retryable(
            value = {DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Transactional(readOnly = true)
    @Override
    public boolean authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Transactional()
    @Override
    public void deleteScheduledPrice(Long id, String email) {
        ScheduledPrice price = scheduledPriceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Price not found"));

        if (!price.getUser().getEmail().equals(email)) {
            throw new SecurityException("Unauthorized access");
        }
        scheduledPriceRepository.delete(price);
    }

    @Transactional()
    @Override
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        scheduledPriceRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    public String getUserZoneDateTime(String email) {
        User user = findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        return user.getTimezone();
    }

    private void validEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email cannot be empty");
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        if (!matcher.matches()) {
            throw new BadRequestException("Некорректная почта");
        }
    }
}