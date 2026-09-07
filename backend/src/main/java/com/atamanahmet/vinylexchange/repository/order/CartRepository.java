package com.atamanahmet.vinylexchange.repository.order;

import java.util.Optional;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    @EntityGraph(attributePaths = "cartItems")
    Optional<Cart> findByUserId(UUID userId);

    @Transactional
    void deleteByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
