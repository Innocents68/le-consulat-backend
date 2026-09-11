package com.leconsulat.numerotation.service;

import com.leconsulat.etablissement.entity.Etablissement;
import com.leconsulat.numerotation.repository.SequenceNumerotationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

/** Génère les numéros de documents au format [CODE_ETABLISSEMENT]-[TYPE]-[AAAA]-[NNNNNN]
 * (RG-020), continus et sans trou (RG-021). */
@Service
public class NumerotationService {

    private final SequenceNumerotationRepository repository;

    public NumerotationService(SequenceNumerotationRepository repository) {
        this.repository = repository;
    }

    /** Toujours dans sa propre transaction, même si l'appelant en a déjà une : le numéro doit
     * rester réservé (jamais réutilisé, RG-021) même si le reste de l'opération appelante
     * échoue ensuite et annule tout le reste (RG-032) — sinon un numéro "consommé" par une
     * commande qui échoue à se créer serait perdu, ce qui est le comportement voulu ici, pas
     * une fuite : la séquence avance, elle ne recule jamais. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String genererNumero(Etablissement etablissement, String type) {
        int annee = Year.now().getValue();
        repository.upsertEtIncrementer(etablissement.getId(), type, annee);
        int numero = repository.lireNumeroCourant(etablissement.getId(), type, annee);
        return "%s-%s-%d-%06d".formatted(etablissement.getCode(), type, annee, numero);
    }
}
