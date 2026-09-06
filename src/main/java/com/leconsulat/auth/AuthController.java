package com.leconsulat.auth;

import com.leconsulat.auth.dto.LoginRequest;
import com.leconsulat.auth.dto.LoginResponse;
import com.leconsulat.auth.dto.UserDto;
import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.common.exception.UnauthorizedException;
import com.leconsulat.security.CustomUserDetails;
import com.leconsulat.security.JwtUtil;
import com.leconsulat.utilisateur.entity.Utilisateur;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentification")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final JournalOperationService journalOperationService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                           JournalOperationService journalOperationService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.journalOperationService = journalOperationService;
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion (login/password) -> JWT")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (Exception ex) {
            throw new UnauthorizedException("Identifiant ou mot de passe incorrect");
        }
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        Utilisateur utilisateur = principal.getUtilisateur();

        String token = jwtUtil.generateToken(utilisateur.getUsername(), utilisateur.getId(), utilisateur.getRole().name());
        journalOperationService.enregistrer("AUTH", "CONNEXION", "Connexion de " + utilisateur.getUsername());

        LoginResponse response = new LoginResponse(token, "Bearer", jwtUtil.getExpirationMs() / 1000, UserDto.from(utilisateur));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Utilisateur courant")
    public ResponseEntity<UserDto> me() {
        CustomUserDetails principal = currentUser();
        return ResponseEntity.ok(UserDto.from(principal.getUtilisateur()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion")
    public ResponseEntity<Void> logout() {
        CustomUserDetails principal = currentUserOrNull();
        if (principal != null) {
            journalOperationService.enregistrer("AUTH", "DECONNEXION", "Déconnexion de " + principal.getUsername());
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    private CustomUserDetails currentUser() {
        CustomUserDetails principal = currentUserOrNull();
        if (principal == null) {
            throw new UnauthorizedException("Non authentifié");
        }
        return principal;
    }

    private CustomUserDetails currentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails cud) {
            return cud;
        }
        return null;
    }
}
