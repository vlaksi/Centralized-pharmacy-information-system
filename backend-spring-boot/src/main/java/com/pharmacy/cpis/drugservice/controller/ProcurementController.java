package com.pharmacy.cpis.drugservice.controller;

import com.pharmacy.cpis.drugservice.dto.DrugOrderDTO;
import com.pharmacy.cpis.drugservice.dto.SupplierOfferDTO;
import com.pharmacy.cpis.drugservice.model.drugprocurement.DrugOrder;
import com.pharmacy.cpis.drugservice.model.drugprocurement.Offer;
import com.pharmacy.cpis.drugservice.service.IDrugOrderService;
import com.pharmacy.cpis.drugservice.service.IOfferService;
import com.pharmacy.cpis.userservice.model.users.Supplier;
import com.pharmacy.cpis.userservice.model.users.UserAccount;
import com.pharmacy.cpis.userservice.service.ISupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "api/procurement")
public class ProcurementController {

    @Autowired
    private ISupplierService supplierService;

    @Autowired
    private IOfferService offerService;

    @Autowired
    private IDrugOrderService drugOrderService;

    @GetMapping(value = "/offers")
    public ResponseEntity<List<SupplierOfferDTO>> getSupplierOffers(){
        List<SupplierOfferDTO> supplierOfferDTOS = new ArrayList<>();

        UserAccount account = (UserAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Supplier supplier = supplierService.getSupplierByUserAccount(account);

        List<Offer> supplierOffers = offerService.findOffersBySupplier(supplier);
        for(Offer offer : supplierOffers){
            supplierOfferDTOS.add(new SupplierOfferDTO(offer));
        }
        return new ResponseEntity<>(supplierOfferDTOS, HttpStatus.OK);
    }

    @GetMapping(value = "/orders")
    public ResponseEntity<List<DrugOrderDTO>> getAllDrugOrders(){
        List<DrugOrderDTO> drugOrderDTOS = new ArrayList<>();
        List<DrugOrder> drugOrders = drugOrderService.findAll();
        for(DrugOrder drugOrder: drugOrders){
            DrugOrderDTO drugOrderDTO = new DrugOrderDTO(drugOrder);
            drugOrderDTOS.add(drugOrderDTO);
        }
        return new ResponseEntity<>(drugOrderDTOS, HttpStatus.OK);
    }
}