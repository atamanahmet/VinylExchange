package com.atamanahmet.vinylexchange.domain.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum UserStatus {
    PENDING,
    ACTIVE,
    INACTIVE,
    SUSPENDED,
    BANNED,
    DELETED;

    private static final Map<UserStatus, Set<UserStatus>> VALID_TRANSITIONS = Map.of(
            PENDING, Set.copyOf(EnumSet.of(ACTIVE, DELETED)),
            ACTIVE, Set.copyOf(EnumSet.of(INACTIVE, SUSPENDED, DELETED)),
            INACTIVE, Set.copyOf(EnumSet.of(ACTIVE, DELETED)),
            SUSPENDED, Set.copyOf(EnumSet.of(ACTIVE, BANNED, DELETED)),
            BANNED, Set.copyOf(EnumSet.noneOf(UserStatus.class)),
            DELETED, Set.copyOf(EnumSet.noneOf(UserStatus.class)));

    public boolean canTransitionTo(UserStatus newStatus) {

        Set<UserStatus> allowedTransitions = VALID_TRANSITIONS.get(this);

        return allowedTransitions != null && allowedTransitions.contains(newStatus);
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isSuspended() {
        return this == SUSPENDED;
    }

    public boolean canLogin() {
        return (this == ACTIVE || this == PENDING);
    }

    public boolean isPermanant() {
        return (this == BANNED || this == DELETED);
    }

}
