package com.atamanahmet.vinylexchange.service.user;

import com.atamanahmet.vinylexchange.domain.entity.Role;
import com.atamanahmet.vinylexchange.repository.user.RoleRepository;
import org.springframework.stereotype.Service;

import com.atamanahmet.vinylexchange.domain.enums.RoleName;
import com.atamanahmet.vinylexchange.exception.RoleNotFoundException;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {

        this.roleRepository = roleRepository;
    }

    public Role getRoleByName(RoleName name) {

        return roleRepository
                .findByName(name)
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));
    }

    public Role saveRole(Role role) {

        return roleRepository.save(role);
    }

    public boolean existsByName(RoleName roleName) {

        return roleRepository.existsByName(roleName);
    }

}
