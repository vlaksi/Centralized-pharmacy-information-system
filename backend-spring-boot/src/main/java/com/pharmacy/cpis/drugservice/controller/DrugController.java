package com.pharmacy.cpis.drugservice.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacy.cpis.drugservice.dto.DrugDTO;
import com.pharmacy.cpis.drugservice.dto.DrugRegisterDTO;
import com.pharmacy.cpis.drugservice.dto.DrugWithoutAlergiesDTO;
import com.pharmacy.cpis.drugservice.dto.PharmacyDrugPriceDTO;
import com.pharmacy.cpis.drugservice.model.drug.Drug;
import com.pharmacy.cpis.drugservice.model.drug.DrugClass;
import com.pharmacy.cpis.drugservice.model.drug.DrugForm;
import com.pharmacy.cpis.drugservice.model.drugsales.AvailableDrug;
import com.pharmacy.cpis.drugservice.service.IAvailableDrugService;
import com.pharmacy.cpis.drugservice.service.IDrugService;
import com.pharmacy.cpis.util.CollectionUtil;

@RestController
@RequestMapping(value = "api/drugs")
public class DrugController {

	@Autowired
	private IDrugService drugService;

	@Autowired
	private IAvailableDrugService availableDrugService;

	@GetMapping()
	public ResponseEntity<List<DrugDTO>> getAllDrugs() {
		List<Drug> drugs = drugService.findAll();

		// convert drugs to DTOs
		List<DrugDTO> drugsDTO = new ArrayList<>();
		for (Drug drug : drugs) {
			drugsDTO.add(new DrugDTO(drug, drugService));
		}

		return new ResponseEntity<>(drugsDTO, HttpStatus.OK);
	}

	@GetMapping(value = "/pharmacy/{id}")
	@PreAuthorize("hasRole('PATIENT')")
	public ResponseEntity<Collection<DrugDTO>> getAvailableByPharmacy(@PathVariable(required = true) Long id,
			Authentication authentication) {
		Collection<AvailableDrug> drugs = availableDrugService.getByPharmacyForPatient(id, authentication.getName());

		Collection<DrugDTO> mapped = CollectionUtil.map(drugs, drug -> new DrugDTO(drug.getDrug(), drugService));

		return new ResponseEntity<>(mapped, HttpStatus.OK);
	}

	// Post is only because i want to send data through body
	@PostMapping(value = "/availabledrugs")
	public ResponseEntity<List<PharmacyDrugPriceDTO>> getDrugPharmaciesPrices(@RequestBody DrugDTO drugDTO) {
		List<PharmacyDrugPriceDTO> pharmacyDrugPriceDTOS = new ArrayList<>();
		List<AvailableDrug> availableDrugs = drugService.findAvailableDrugsByCode(drugDTO.getCode());
		for (AvailableDrug availableDrug : availableDrugs) {
			pharmacyDrugPriceDTOS.add(new PharmacyDrugPriceDTO(availableDrug));
		}
		return new ResponseEntity<>(pharmacyDrugPriceDTOS, HttpStatus.OK);
	}

	@GetMapping(value = "/types")
	public ResponseEntity<List<DrugClass>> getAllDrugsTypes() {
		List<DrugClass> drugsTypes = drugService.findAllDrugClass();
		return new ResponseEntity<>(drugsTypes, HttpStatus.OK);
	}

	@GetMapping(value = "/forms")
	public ResponseEntity<Collection<DrugForm>> getAllDrugForms() {
		Collection<DrugForm> drugsForms = drugService.findAllDrugForms();
		return new ResponseEntity<>(drugsForms, HttpStatus.OK);
	}

	@PostMapping(value = "/register", consumes = "application/json")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Drug> addDrug(@RequestBody DrugRegisterDTO drugRegisterDTO) {
		Drug addedPharmacy = drugService.registerDrug(drugRegisterDTO);
		return new ResponseEntity<>(addedPharmacy, HttpStatus.CREATED);
	}

	@PostMapping(value = "/alldrugswithoutalergies", consumes = "application/json")
	@PreAuthorize("hasRole('DERMATOLOGIST') || hasRole('PHARMACIST')")
	public ResponseEntity<List<DrugDTO>> addDrug(@RequestBody DrugWithoutAlergiesDTO drugWithoutAlergiesDTO) {
		List<DrugDTO> drugDTO = drugService.getDrugsForPhatientWithoutAlergies(drugWithoutAlergiesDTO.getPaatientID(),
				drugService);
		return new ResponseEntity<>(drugDTO, HttpStatus.OK);
	}

}
