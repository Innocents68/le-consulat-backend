package com.leconsulat.utilisateur.repository;

import com.leconsulat.utilisateur.entity.Droit;
import com.leconsulat.utilisateur.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DroitRepository extends JpaRepository<Droit, Long> {
    List<Droit> findByRole(Role role);

    Optional<Droit> findByRoleAndModule(Role role, String module);

    boolean existsByRole(Role role);
}
