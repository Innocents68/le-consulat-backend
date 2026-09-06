package com.leconsulat.restaurant.dto;

import com.leconsulat.restaurant.entity.TableRestaurant;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TableRestaurantDto(
        Long id,
        @NotBlank(message = "Le numéro de table est obligatoire") String numero,
        @Min(value = 1, message = "La capacité doit être positive") int capacite,
        String zone,
        String statut,
        Integer positionX,
        Integer positionY
) {
    public static TableRestaurantDto from(TableRestaurant t) {
        return new TableRestaurantDto(t.getId(), t.getNumero(), t.getCapacite(), t.getZone(),
                t.getStatut().name(), t.getPositionX(), t.getPositionY());
    }
}
