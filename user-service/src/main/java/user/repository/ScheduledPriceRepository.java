package user.repository;

import user.model.ScheduledPrice;
import user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduledPriceRepository extends JpaRepository<ScheduledPrice, Long> {

    List<ScheduledPrice> findByUserEmail(String email);

    List<ScheduledPrice> findByUserId(Long userId);

    void deleteByUser(User user);
}