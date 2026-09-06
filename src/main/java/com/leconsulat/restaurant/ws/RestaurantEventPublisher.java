package com.leconsulat.restaurant.ws;

import com.leconsulat.restaurant.dto.CommandeDto;
import com.leconsulat.restaurant.dto.TableRestaurantDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes live state changes to the STOMP topics defined in API_CONTRACT.md §4 :
 * /topic/commandes, /topic/tables, /topic/cuisine.
 */
@Component
public class RestaurantEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public RestaurantEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void commandeMiseAJour(CommandeDto commande) {
        messagingTemplate.convertAndSend("/topic/commandes", commande);
    }

    public void tableMiseAJour(TableRestaurantDto table) {
        messagingTemplate.convertAndSend("/topic/tables", table);
    }

    public void evenementCuisine(CommandeDto commande) {
        messagingTemplate.convertAndSend("/topic/cuisine", commande);
    }
}
