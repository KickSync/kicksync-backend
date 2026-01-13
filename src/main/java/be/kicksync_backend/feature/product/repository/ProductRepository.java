package be.kicksync_backend.feature.product.repository;

import be.kicksync_backend.feature.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForce(@Param("id") Long id);

    // 재고 증가 (주문 취소 시 사용) - DB 레벨의 원자적 업데이트
    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity WHERE p.id = :id")
    void increaseStock(@Param("id") Long id, @Param("quantity") int quantity);
}