package com.atamanahmet.vinylexchange.repository.user;

import java.util.Optional;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.enums.UserStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatusHistoryRepository extends JpaRepository<UserStatusHistory, UUID> {
    Optional<UserStatusHistory> findByUserId(UUID userId);
}
