package com.atamanahmet.vinylexchange.repository.user;

import java.util.Optional;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u.username from User u " +
            "WHERE u.id = :userId")
    Optional<String> findUsernameById(@Param("userId") UUID userId);
}
