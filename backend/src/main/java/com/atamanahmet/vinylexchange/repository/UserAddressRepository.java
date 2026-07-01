package com.atamanahmet.vinylexchange.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.atamanahmet.vinylexchange.domain.entity.UserAddress;
import com.atamanahmet.vinylexchange.domain.enums.AddressType;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {

    List<UserAddress> findByUserIdAndAddressType(UUID userId, AddressType addressType);

    List<UserAddress> findByUserId(UUID userId);

    Optional<UserAddress> findByUserIdAndIsDefaultTrueAndAddressType(UUID userId, AddressType addressType);

    boolean existsByUserId(UUID userId);
}
