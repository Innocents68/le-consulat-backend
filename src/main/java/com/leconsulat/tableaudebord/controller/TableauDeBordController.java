package com.leconsulat.tableaudebord.controller;

import com.leconsulat.tableaudebord.dto.TableauDeBordDto;
import com.leconsulat.tableaudebord.service.TableauDeBordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** §6.1 — ouvert aux deux profils, cloisonné dans le service (comme Dépenses/Reporting). */
@RestController
@RequestMapping("/api/v1/tableau-de-bord")
public class TableauDeBordController {

    private final TableauDeBordService service;

    public TableauDeBordController(TableauDeBordService service) {
        this.service = service;
    }

    @GetMapping
    public TableauDeBordDto get() {
        return service.get();
    }
}
