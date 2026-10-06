package com.leconsulat.stock.entity;

/** Demandes_amelioration_logiciel_Le_Consulat_Professionnel.docx §5 : une demande de transfert
 * entre établissements doit être explicitement acceptée ou refusée par le responsable de
 * l'établissement destinataire avant que le mouvement de stock n'ait lieu. */
public enum StatutDemandeTransfert {
    EN_ATTENTE,
    ACCEPTEE,
    REFUSEE
}
