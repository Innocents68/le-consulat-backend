package com.leconsulat.utilisateur.repository;

import com.leconsulat.utilisateur.entity.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByUsername(String username);

    boolean existsByUsername(String username);

    Page<Utilisateur> findByNomContainingIgnoreCaseOrUsernameContainingIgnoreCase(String nom, String username, Pageable pageable);

    long countByRole(com.leconsulat.utilisateur.entity.Role role);
}
