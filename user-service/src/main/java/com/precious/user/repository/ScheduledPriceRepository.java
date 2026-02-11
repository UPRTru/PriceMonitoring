package com.precious.user.repository;

import com.precious.user.model.ScheduledPrice;
import com.precious.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduledPriceRepository extends JpaRepository<ScheduledPrice, Long> {

    List<ScheduledPrice> findByUserEmail(String email);

    List<ScheduledPrice> findByUserId(Long userId);

    void deleteByUser(User user);
}