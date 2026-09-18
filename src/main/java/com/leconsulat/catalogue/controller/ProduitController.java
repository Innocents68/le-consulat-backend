package com.leconsulat.catalogue.controller;

import com.leconsulat.catalogue.dto.*;
import com.leconsulat.catalogue.service.ProduitService;
import com.leconsulat.catalogue.util.QrCodeGenerator;
import com.leconsulat.common.exception.BusinessRuleException;
import com.leconsulat.common.util.PageableUtil;
import com.leconsulat.common.web.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/produits")
public class ProduitController {

    private final ProduitService service;

    public ProduitController(ProduitService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<ProduitDto> search(@RequestParam(required = false) Long etablissementId,
                                            @RequestParam(required = false) Long categorieId,
                                            @RequestParam(required = false) Boolean actif,
                                            @RequestParam(required = false) Boolean suiviStock,
                                            @RequestParam(required = false) String search,
                                            @RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer size,
                                            @RequestParam(required = false) String sort) {
        Pageable pageable = PageableUtil.build(page, size, sort);
        Page<ProduitDto> result = service.search(etablissementId, categorieId, actif, suiviStock, search, pageable);
        return PageResponse.ofDto(result);
    }

    @GetMapping("/{id}")
    public ProduitDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/{id}/historique-prix")
    public List<HistoriquePrixDto> historiquePrix(@PathVariable Long id) {
        return service.historiquePrix(id);
    }

    /** Recommandations et corrections.md §5 : QR code unique du produit, généré à la volée
     * (pas stocké) — consultable, téléchargeable et imprimable depuis le frontend. */
    @GetMapping(value = "/{id}/qrcode.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrcode(@PathVariable Long id) {
        service.get(id); // vérifie l'existence et le périmètre avant de générer l'image
        byte[] png = QrCodeGenerator.genererPng(QrCodeGenerator.contenuPourProduit(id), 320);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
    }

    /** Recommandations et corrections.md §6/7 : retrouve un produit à partir du texte décodé
     * d'un QR scanné (Nouvelle entrée/sortie, Nouvelle commande) — même contrôle de périmètre
     * que {@link #get}. */
    @GetMapping("/scanner")
    public ProduitDto parQrCode(@RequestParam String code) {
        Long id = QrCodeGenerator.extraireIdProduit(code);
        if (id == null) {
            throw new BusinessRuleException("QR code illisible ou invalide");
        }
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProduitDto create(@Valid @RequestBody CreateProduitRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public ProduitDto update(@PathVariable Long id, @Valid @RequestBody UpdateProduitRequest req) {
        return service.update(id, req);
    }

    @PatchMapping("/{id}/statut")
    public void toggleActif(@PathVariable Long id) {
        service.toggleActif(id);
    }
}
