package com.pharmacy.cpis.drugservice.service.impl;


import com.pharmacy.cpis.drugservice.dto.DrugReservationDTO;
import com.pharmacy.cpis.drugservice.model.drugsales.Reservation;
import com.pharmacy.cpis.drugservice.repository.IDrugRepository;
import com.pharmacy.cpis.drugservice.repository.IReservationRepository;
import com.pharmacy.cpis.drugservice.service.IAvailableDrugService;
import com.pharmacy.cpis.drugservice.service.IReservationService;
import com.pharmacy.cpis.pharmacyservice.service.IPharmacyService;
import com.pharmacy.cpis.userservice.service.EmailService;
import com.pharmacy.cpis.userservice.service.IPatientService;
import com.pharmacy.cpis.drugservice.dto.ReservationDTO;
import com.pharmacy.cpis.drugservice.model.drugsales.*;
import com.pharmacy.cpis.drugservice.repository.IDrugPurchaseRepository;
import com.pharmacy.cpis.pharmacyservice.model.pharmacy.Pharmacy;
import com.pharmacy.cpis.scheduleservice.model.workschedule.WorkingTimes;
import com.pharmacy.cpis.userservice.model.loyaltyprogram.UserCategory;
import com.pharmacy.cpis.userservice.model.users.Consultant;
import com.pharmacy.cpis.userservice.service.IConsultantService;
import com.pharmacy.cpis.userservice.service.ILoyaltyProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class ReservationService implements IReservationService {

    @Autowired
    private IReservationRepository reservationRepository;

    @Autowired
    private IDrugRepository drugRepository;

    @Autowired
    private IPatientService patientService;

    @Autowired
    private IPharmacyService pharmacyService;

    @Autowired
    private IAvailableDrugService availableDrugService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private IConsultantService consultantService;

    @Autowired
    private IDrugPurchaseRepository drugPurchaseRepository;

    @Autowired
    private ILoyaltyProgramService loyaltyProgramService;

    @Override
    public Reservation saveReservation(DrugReservationDTO reservationDTO) {
        Reservation reservation=new Reservation();
        reservation.setAmount(reservationDTO.getAmount());
        reservation.setDeadline(reservationDTO.getDeadline());
        reservation.setPatient(patientService.findByEmail(reservationDTO.getPatientEmail()));
        reservation.setDateOfCreation(reservationDTO.getDateOfCreation());
        reservation.setPharmacy(pharmacyService.getById(reservationDTO.getPharmacyID()));
        reservation.setDrug(drugRepository.findByCode(reservationDTO.getDrugCode()));
        reservation.setIsPickedUp(false);

        availableDrugService.updateAmount(reservationDTO.getPharmacyID(),reservationDTO.getDrugCode(),reservationDTO.getAmount());

        try {
            System.out.println("Sending mail in process ..");
            emailService.sendConfirmReservationOfDrugEmailAsync(reservationDTO.getPatientEmail(),
                    reservationDTO, reservation);

        } catch (Exception e) {
            System.out.println("Error during sending email: " + e.getMessage());
        }

        return reservationRepository.save(reservation);
    }
    @Override
    public ReservationDTO isReservationValid(ReservationDTO reservationDTO) {
        Reservation reservation = reservationRepository.getOne(reservationDTO.getReservationID());

        Boolean isValid = false;

        Instant now = Instant.now(); //current date
        Instant before24h = now.plus(Duration.ofDays(1));
        Date dateBefore24h = Date.from(before24h);

        Consultant consultant = consultantService.findByEmail(reservationDTO.getConsultantEmail());

        Pharmacy pharmacy = findPharmacyWhereConsultantWork(consultant.getId());
        if(pharmacy.getId().equals(reservation.getPharmacy().getId())){
            if(reservationRepository.existsById(reservationDTO.getReservationID()) && reservation.getDeadline().compareTo(dateBefore24h) > 0){
                isValid = true;
                reservationDTO.setValid(isValid);
                reservationDTO.setAmount(reservation.getAmount());
                reservationDTO.setDateOfCreation(reservation.getDateOfCreation());
                reservationDTO.setDeadLine(reservation.getDeadline());
                reservationDTO.setPhatientName(reservation.getPatient().getName());
                reservationDTO.setPharmacyName(reservation.getPharmacy().getName());
                return reservationDTO;
            }
        }

        reservationDTO.setValid(isValid);

        return reservationDTO;
    }

    @Override
    public ReservationDTO dispensingMedicine(ReservationDTO reservationDTO) {
        Reservation reservation = reservationRepository.getOne(reservationDTO.getReservationID());

        reservation.setIsPickedUp(true);
        reservationRepository.save(reservation);
        sendDrugPurchase(reservation);

        return reservationDTO;
    }

    private void sendDrugPurchase(Reservation reservation) {
        DrugPurchase drugPurchase = new DrugPurchase();

        drugPurchase.setAmount(reservation.getAmount());
        drugPurchase.setDrug(reservation.getDrug());
        drugPurchase.setPatient(reservation.getPatient());
        drugPurchase.setPharmacy(reservation.getPharmacy());

        drugPurchase.setPrice(calculatePriceDrugPurchaseWithDiscount(reservation));
        drugPurchase.setTimestamp(new Date());
        drugPurchase.setType(DrugPurchaseType.RESERVATION);

        drugPurchaseRepository.save(drugPurchase);
    }

    private Double calculatePriceDrugPurchaseWithDiscount(Reservation reservation) {
        Double priceWithDiscount;

        AvailableDrug availableDrug = availableDrugService.getByPharmacyAndDrug(reservation.getPharmacy().getId(), reservation.getDrug().getCode());
        Double priceWithoutDiscount = availableDrug.findPrice(reservation.getDateOfCreation()).getPrice();
        UserCategory userCategory = loyaltyProgramService.findUserCategoryByLoyaltyPoints(reservation.getPatient().getLoyaltyPoints());
        Double discount = userCategory.getReservationDiscount();

        System.out.println("POPUST JE AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA " + discount);

        if(discount != 0){
            priceWithDiscount = (priceWithoutDiscount -(priceWithoutDiscount*(discount/100))) * reservation.getAmount();
        }else{
            priceWithDiscount =priceWithoutDiscount;
        }
        return priceWithDiscount;
    }


    Pharmacy findPharmacyWhereConsultantWork(Long consultantID){
        List<Pharmacy> allPharmacies = pharmacyService.findAll();

        for (Pharmacy p : allPharmacies) {
            for(WorkingTimes wt : p.getConsultants()){
                if(wt.getConsultant().getId().equals(consultantID)){
                    return p;
                }
            }
        }
        return new Pharmacy();
    }

}