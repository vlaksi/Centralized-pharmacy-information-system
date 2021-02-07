package com.pharmacy.cpis.drugservice.controller;

import com.pharmacy.cpis.drugservice.dto.DrugDTO;
import com.pharmacy.cpis.drugservice.dto.DrugRegisterDTO;
import com.pharmacy.cpis.drugservice.dto.PharmacyDrugPriceDTO;
import com.pharmacy.cpis.drugservice.model.drug.Drug;
import com.pharmacy.cpis.drugservice.model.drug.DrugClass;
import com.pharmacy.cpis.drugservice.model.drugsales.AvailableDrug;
import com.pharmacy.cpis.drugservice.service.IDrugService;
import com.pharmacy.cpis.pharmacyservice.service.IPharmacyService;
import com.pharmacy.cpis.userservice.dto.PatientDTO;
import com.pharmacy.cpis.userservice.model.users.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "api/drugs")
public class DrugController {

    @Autowired
    private IDrugService drugService;

    @Autowired
    private IPharmacyService pharmacyService;

    @GetMapping()
    public ResponseEntity<List<DrugDTO>> getAllDrugs() {
        List<Drug> drugs = drugService.findAll();

        // convert drugs to DTOs
        List<DrugDTO> drugsDTO = new ArrayList<>();
        for (Drug drug : drugs) {
            drugsDTO.add(new DrugDTO(drug, drugService));
        }

        return new ResponseEntity<>(drugsDTO,HttpStatus.OK);
    }

    // Post is only because i want to send data through body
    @PostMapping(value = "/availabledrugs")
    public ResponseEntity<List<PharmacyDrugPriceDTO>> getDrugPharmaciesPrices(@RequestBody DrugDTO drugDTO){
        List<PharmacyDrugPriceDTO> pharmacyDrugPriceDTOS = new ArrayList<>();
        List<AvailableDrug> availableDrugs = drugService.findAvailableDrugsByCode(drugDTO.getCode());
        for(AvailableDrug availableDrug : availableDrugs){
            pharmacyDrugPriceDTOS.add(new PharmacyDrugPriceDTO(availableDrug));
        }
        return new ResponseEntity<>(pharmacyDrugPriceDTOS,HttpStatus.OK);
    }

    @GetMapping(value = "/types")
    public ResponseEntity<List<DrugClass>> getAllDrugsTypes() {
        List<DrugClass> drugsTypes = drugService.findAllDrugClass();
        return new ResponseEntity<>(drugsTypes,HttpStatus.OK);
    }

    @PostMapping(value = "/register", consumes = "application/json")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Drug> addDrug(@RequestBody DrugRegisterDTO drugRegisterDTO) {
        Drug addedPharmacy = drugService.registerDrug(drugRegisterDTO);
        return new ResponseEntity<>(addedPharmacy, HttpStatus.CREATED);
    }
}
