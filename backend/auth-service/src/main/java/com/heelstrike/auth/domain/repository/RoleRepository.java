package com.heelstrike.auth.domain.repository;

import com.heelstrike.auth.domain.entity.RoleEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class RoleRepository implements PanacheRepositoryBase<RoleEntity, Long> {

    public Optional<RoleEntity> findRoleById(Long id) {
        return findByIdOptional(id);
    }
}
