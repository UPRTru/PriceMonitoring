package prices.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import prices.model.MetalPrice;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetalPriceRepository extends JpaRepository<MetalPrice, Long> {

    @Query("SELECT p FROM MetalPrice p WHERE p.name = :name ORDER BY p.timestamp DESC")
    Optional<MetalPrice> findLatestByName(@Param("name") String name);

    @Query("SELECT p FROM MetalPrice p " +
            "WHERE p.name = :name " +
            "AND p.timestamp >= :from " +
            "AND p.timestamp < :to " +
            "ORDER BY p.timestamp ASC")
    List<MetalPrice> findByNameAndTimestampBetweenOrderByTimestampAsc(
            @Param("name") String name,
            @Param("from") Long from,
            @Param("to") Long to);

    @Query("SELECT p FROM MetalPrice p " +
            "WHERE p.timestamp = (SELECT MAX(p2.timestamp) FROM MetalPrice p2 WHERE p2.name = p.name)")
    List<MetalPrice> findLatestUniqueByName();
}