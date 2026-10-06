package com.leconsulat.stock.repository;

import com.leconsulat.stock.entity.DemandeTransfert;
import com.leconsulat.stock.entity.StatutDemandeTransfert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemandeTransfertRepository extends JpaRepository<DemandeTransfert, Long> {

    Page<DemandeTransfert> findByProduitSource_Etablissement_IdOrderByDateDemandeDesc(Long etablissementId, Pageable pageable);

    Page<DemandeTransfert> findByProduitSource_Etablissement_IdAndStatutOrderByDateDemandeDesc(Long etablissementId, StatutDemandeTransfert statut, Pageable pageable);

    Page<DemandeTransfert> findByEtablissementDestination_IdOrderByDateDemandeDesc(Long etablissementId, Pageable pageable);

    Page<DemandeTransfert> findByEtablissementDestination_IdAndStatutOrderByDateDemandeDesc(Long etablissementId, StatutDemandeTransfert statut, Pageable pageable);

    long countByEtablissementDestination_IdAndStatut(Long etablissementId, StatutDemandeTransfert statut);

    long countByStatut(StatutDemandeTransfert statut);
}
