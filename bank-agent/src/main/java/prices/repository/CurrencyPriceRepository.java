package prices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import prices.model.CurrencyPrice;
import prices.model.MetalPrice;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyPriceRepository extends JpaRepository<CurrencyPrice, Long> {

    @Query("SELECT p FROM CurrencyPrice p WHERE p.name = :name ORDER BY p.timestamp DESC")
    Optional<CurrencyPrice> findLatestByName(String name);

    @Query("SELECT p FROM CurrencyPrice p " +
            "WHERE p.name = :currencyName " +
            "AND p.timestamp >= :from " +
            "AND p.timestamp < :to " +
            "ORDER BY p.timestamp ASC")
    List<CurrencyPrice> findByNameAndTimestampBetweenOrderByTimestampAsc(
            @Param("currencyName") String name,
            @Param("from") Long from,
            @Param("to") Long to);

    @Query("SELECT p FROM CurrencyPrice p " +
            "WHERE p.timestamp = (SELECT MAX(p2.timestamp) FROM CurrencyPrice p2 WHERE p2.name = p.name)")
    List<CurrencyPrice> findLatestUniqueByName();
}