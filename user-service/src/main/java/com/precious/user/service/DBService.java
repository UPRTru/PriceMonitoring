package com.precious.user.service;

import com.precious.user.model.ScheduledPrice;
import com.precious.user.model.User;
import com.precious.user.repository.ScheduledPriceRepository;
import com.precious.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DBService {

    private final ScheduledPriceRepository scheduledPriceRepository;
    private final UserRepository userRepository;

    @Retryable(
            value = {DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 1.5)
    )
    @Transactional(readOnly = true)
    public List<ScheduledPrice> getAllScheduledPrices() {
        log.info("Fetching all scheduled prices from database");
        return scheduledPriceRepository.findAll();
    }

    @Retryable(
            value = {DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Transactional(readOnly = true)
    public List<ScheduledPrice> getScheduledPricesByUser(String email) {
        log.info("Fetching scheduled prices for user: {}", email);
        return scheduledPriceRepository.findByUserEmail(email);
    }

    @Retryable(
            value = {DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.info("Fetching all users from database");
        return userRepository.findAll();
    }

    @Retryable(
            value = {DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Recover
    public List<ScheduledPrice> recoverScheduledPrices(DataAccessException e) {
        log.warn("Failed to fetch scheduled prices after retries: {}", e.getMessage());
        return Collections.emptyList();
    }

    @Recover
    public List<User> recoverUsers(DataAccessException e) {
        log.warn("Failed to fetch users after retries: {}", e.getMessage());
        return Collections.emptyList();
    }

    @Recover
    public Optional<User> recoverUser(DataAccessException e) {
        log.warn("Failed to fetch user after retries: {}", e.getMessage());
        return Optional.empty();
    }
}