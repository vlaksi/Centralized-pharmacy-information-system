package com.pharmacy.cpis.drugservice.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacy.cpis.drugservice.dto.AddAvailableDrugDTO;
import com.pharmacy.cpis.drugservice.dto.AvailableDrugDTO;
import com.pharmacy.cpis.drugservice.model.drugsales.AvailableDrug;
import com.pharmacy.cpis.drugservice.service.IAvailableDrugService;
import com.pharmacy.cpis.userservice.model.users.UserAccount;
import com.pharmacy.cpis.util.aspects.EmployeeAccountActive;
import com.pharmacy.cpis.util.exceptions.PSForbiddenException;

@RestController
@RequestMapping("api/pharmacy/drugs")
public class DrugInPharmacyController {

	@Autowired
	private IAvailableDrugService availableDrugService;

	@GetMapping
	@PreAuthorize("hasRole('PHARMACY_ADMIN')")
	@EmployeeAccountActive
	public ResponseEntity<Iterable<AvailableDrugDTO>> getDrugs() {
		UserAccount user = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (user.getPharmacyId() == null)
			throw new PSForbiddenException("You are not authorized to administrate a pharmacy.");

		List<AvailableDrugDTO> availableDrugs = new ArrayList<>();
		for (AvailableDrug availableDrug : availableDrugService.getByPharmacy(user.getPharmacyId())) {
			availableDrugs.add(new AvailableDrugDTO(availableDrug));
		}

		return ResponseEntity.ok(availableDrugs);
	}

	@PostMapping(value = "", consumes = "application/json")
	@PreAuthorize("hasRole('PHARMACY_ADMIN')")
	@EmployeeAccountActive
	public ResponseEntity<AvailableDrugDTO> addDrug(@RequestBody @Valid AddAvailableDrugDTO drugInfo) {
		UserAccount user = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (user.getPharmacyId() == null)
			throw new PSForbiddenException("You are not authorized to administrate a pharmacy.");

		AvailableDrug added = availableDrugService.addToPharmacy(user.getPharmacyId(), drugInfo);

		return new ResponseEntity<AvailableDrugDTO>(new AvailableDrugDTO(added), HttpStatus.CREATED);
	}

	@GetMapping("/{code}")
	@PreAuthorize("hasRole('PHARMACY_ADMIN')")
	@EmployeeAccountActive
	public ResponseEntity<AvailableDrugDTO> getDrug(@PathVariable(required = true) String code) {
		UserAccount user = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (user.getPharmacyId() == null)
			throw new PSForbiddenException("You are not authorized to administrate a pharmacy.");

		AvailableDrug drug = availableDrugService.getByPharmacyAndDrug(user.getPharmacyId(), code);

		return ResponseEntity.ok(new AvailableDrugDTO(drug));
	}

	@DeleteMapping("/{code}")
	@PreAuthorize("hasRole('PHARMACY_ADMIN')")
	@EmployeeAccountActive
	public ResponseEntity<Void> deleteDrug(@PathVariable(required = true) String code) {
		UserAccount user = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (user.getPharmacyId() == null)
			throw new PSForbiddenException("You are not authorized to administrate a pharmacy.");

		availableDrugService.deleteFromPharmacy(user.getPharmacyId(), code);

		return ResponseEntity.noContent().build();
	}

}
