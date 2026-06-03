package com.atamanahmet.vinylexchange.repository.user;

import java.util.Optional;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.atamanahmet.vinylexchange.domain.enums.RoleName;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleName name);

    boolean existsByName(RoleName name);
}