package com.leconsulat.restaurant.service;

import com.leconsulat.common.exception.ResourceNotFoundException;
import com.leconsulat.restaurant.dto.TableRestaurantDto;
import com.leconsulat.restaurant.entity.StatutTable;
import com.leconsulat.restaurant.entity.TableRestaurant;
import com.leconsulat.restaurant.repository.TableRestaurantRepository;
import com.leconsulat.restaurant.ws.RestaurantEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TableRestaurantService {

    private final TableRestaurantRepository repository;
    private final RestaurantEventPublisher publisher;

    public TableRestaurantService(TableRestaurantRepository repository, RestaurantEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public List<TableRestaurantDto> list() {
        return repository.findAll().stream().map(TableRestaurantDto::from).toList();
    }

    @Transactional
    public TableRestaurantDto create(TableRestaurantDto dto) {
        TableRestaurant t = new TableRestaurant();
        apply(t, dto);
        t.setStatut(StatutTable.LIBRE);
        return TableRestaurantDto.from(repository.save(t));
    }

    @Transactional
    public TableRestaurantDto update(Long id, TableRestaurantDto dto) {
        TableRestaurant t = findEntity(id);
        apply(t, dto);
        TableRestaurant saved = repository.save(t);
        publisher.tableMiseAJour(TableRestaurantDto.from(saved));
        return TableRestaurantDto.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findEntity(id));
    }

    private void apply(TableRestaurant t, TableRestaurantDto dto) {
        t.setNumero(dto.numero());
        t.setCapacite(dto.capacite());
        t.setZone(dto.zone());
        t.setPositionX(dto.positionX());
        t.setPositionY(dto.positionY());
    }

    private TableRestaurant findEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Table", id));
    }
}
