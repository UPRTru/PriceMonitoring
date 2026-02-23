package user.service;

import shared.dto.AuthDto;
import shared.dto.CheckPrice;
import shared.exception.BadRequestException;
import shared.exception.NotFoundException;
import user.model.ScheduledPrice;
import user.model.User;
import user.repository.ScheduledPriceRepository;
import user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository userRepository;
    private final ScheduledPriceRepository scheduledPriceRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           ScheduledPriceRepository scheduledPriceRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.scheduledPriceRepository = scheduledPriceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Retryable(
            value = {DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Transactional
    @Override
    public User register(AuthDto authDto, String timezone) {
        Objects.requireNonNull(authDto, "AuthDto cannot be null");
        Objects.requireNonNull(timezone, "Timezone cannot be null");
        validateEmail(authDto.email());
        if (userRepository.existsByEmail(authDto.email())) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        User user = new User(
                authDto.email(),
                passwordEncoder.encode(authDto.password()),
                timezone
        );
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
        Objects.requireNonNull(email, "Email cannot be null");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User with email: " + email + " not found"));
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }

    @Retryable(
            value = {DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<User> findByEmail(String email) {
        Objects.requireNonNull(email, "Email cannot be null");
        return userRepository.findByEmail(email);
    }

    @Retryable(
            value = {DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    @Transactional
    @Override
    public void addScheduledPrice(String email, CheckPrice checkPrice) {
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(checkPrice, "CheckPrice cannot be null");
        User user = findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        ScheduledPrice scheduledPrice = new ScheduledPrice(
                user,
                checkPrice.bank().name(),
                checkPrice.typePrice().name(),
                checkPrice.currentPrice().name(),
                checkPrice.name(),
                checkPrice.price()
        );
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
        Objects.requireNonNull(email, "Email cannot be null");
        return scheduledPriceRepository.findByUserEmail(email);
    }

    @Retryable(
            value = {DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 1.5)
    )
    @Transactional(readOnly = true)
    @Override
    public StringBuilder getAllScheduledPrices(String email) {
        Objects.requireNonNull(email, "Email cannot be null");
        User user = findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
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
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(password, "Password cannot be null");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Transactional
    @Override
    public void deleteScheduledPrice(Long id, String email) {
        Objects.requireNonNull(id, "ID cannot be null");
        Objects.requireNonNull(email, "Email cannot be null");
        ScheduledPrice price = scheduledPriceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Price not found with id: " + id));
        if (!price.getUser().getEmail().equals(email)) {
            throw new SecurityException("Unauthorized access");
        }
        scheduledPriceRepository.delete(price);
    }

    @Transactional
    @Override
    public void deleteUser(String email) {
        Objects.requireNonNull(email, "Email cannot be null");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        scheduledPriceRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    @Override
    public String getUserZoneDateTime(String email) {
        Objects.requireNonNull(email, "Email cannot be null");
        User user = findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getTimezone();
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email cannot be empty");
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        if (!matcher.matches()) {
            throw new BadRequestException("Invalid email format");
        }
    }
}