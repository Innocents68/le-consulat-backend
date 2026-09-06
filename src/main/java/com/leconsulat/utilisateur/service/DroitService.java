package com.leconsulat.utilisateur.service;

import com.leconsulat.common.audit.JournalOperationService;
import com.leconsulat.utilisateur.ModulesCatalogue;
import com.leconsulat.utilisateur.dto.DroitFlagsDto;
import com.leconsulat.utilisateur.entity.Droit;
import com.leconsulat.utilisateur.entity.Role;
import com.leconsulat.utilisateur.repository.DroitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DroitService {

    private final DroitRepository repository;
    private final JournalOperationService journal;

    public DroitService(DroitRepository repository, JournalOperationService journal) {
        this.repository = repository;
        this.journal = journal;
    }

    public Map<String, Map<String, DroitFlagsDto>> getMatrice() {
        Map<String, Map<String, DroitFlagsDto>> matrice = new LinkedHashMap<>();
        List<Droit> all = repository.findAll();
        for (Role role : Role.values()) {
            matrice.put(role.name(), new LinkedHashMap<>());
        }
        for (Droit d : all) {
            matrice.computeIfAbsent(d.getRole().name(), k -> new LinkedHashMap<>())
                    .put(d.getModule(), new DroitFlagsDto(d.isVoir(), d.isAjouter(), d.isModifier(), d.isSupprimer()));
        }
        // Ensure every module is present (defaulting to no access) even if not yet seeded.
        for (Map<String, DroitFlagsDto> parModule : matrice.values()) {
            for (String module : ModulesCatalogue.MODULES) {
                parModule.putIfAbsent(module, new DroitFlagsDto(false, false, false, false));
            }
        }
        return matrice;
    }

    public Map<String, DroitFlagsDto> getDroitsPourRole(Role role) {
        return getMatrice().getOrDefault(role.name(), Map.of());
    }

    @Transactional
    public Map<String, Map<String, DroitFlagsDto>> updateMatrice(Map<String, Map<String, DroitFlagsDto>> nouvelleMatrice) {
        for (Map.Entry<String, Map<String, DroitFlagsDto>> roleEntry : nouvelleMatrice.entrySet()) {
            Role role;
            try {
                role = Role.valueOf(roleEntry.getKey());
            } catch (Exception e) {
                continue; // ignore unknown role keys rather than failing the whole update
            }
            for (Map.Entry<String, DroitFlagsDto> moduleEntry : roleEntry.getValue().entrySet()) {
                String module = moduleEntry.getKey();
                DroitFlagsDto flags = moduleEntry.getValue();
                Droit droit = repository.findByRoleAndModule(role, module).orElseGet(() -> new Droit(role, module, false, false, false, false));
                droit.setRole(role);
                droit.setModule(module);
                droit.setVoir(flags.voir());
                droit.setAjouter(flags.ajouter());
                droit.setModifier(flags.modifier());
                droit.setSupprimer(flags.supprimer());
                repository.save(droit);
            }
        }
        journal.enregistrer("PROFILS", "MODIFICATION", "Mise à jour de la matrice des droits");
        return getMatrice();
    }

    @Transactional
    public void seedDefault(Role role, String module, boolean voir, boolean ajouter, boolean modifier, boolean supprimer) {
        if (repository.findByRoleAndModule(role, module).isEmpty()) {
            repository.save(new Droit(role, module, voir, ajouter, modifier, supprimer));
        }
    }
}
