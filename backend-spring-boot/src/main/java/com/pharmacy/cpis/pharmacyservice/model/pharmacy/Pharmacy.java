package com.pharmacy.cpis.pharmacyservice.model.pharmacy;

import com.pharmacy.cpis.drugservice.model.drugsales.AvailableDrug;
import com.pharmacy.cpis.scheduleservice.model.workschedule.WorkingTimes;
import com.pharmacy.cpis.userservice.model.ratings.PharmacyRating;
import com.pharmacy.cpis.userservice.model.users.Patient;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Pharmacy {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(length = 500)
	private String description;

	@Column(nullable = false)
	private Double dermatologistConsultationPrice;

	@Column(nullable = false)
	private Double pharmacistConsultationPrice;

	@Embedded
	@AttributeOverrides(value = { @AttributeOverride(name = "latitude", column = @Column(nullable = false)),
			@AttributeOverride(name = "longitude", column = @Column(nullable = false)) })
	private Location location;

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "subscriptions")
	private Set<Patient> subscribers;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pharmacy")
	private Set<AvailableDrug> availableDrugs;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pharmacy")
	private Set<WorkingTimes> consultants;

	@OneToMany(mappedBy = "pharmacy", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<PharmacyRating> ratings;

	public Pharmacy() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Double getDermatologistConsultationPrice() {
		return dermatologistConsultationPrice;
	}

	public void setDermatologistConsultationPrice(Double dermatologistConsultationPrice) {
		this.dermatologistConsultationPrice = dermatologistConsultationPrice;
	}

	public Double getPharmacistConsultationPrice() {
		return pharmacistConsultationPrice;
	}

	public void setPharmacistConsultationPrice(Double pharmacistConsultationPrice) {
		this.pharmacistConsultationPrice = pharmacistConsultationPrice;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Set<Patient> getSubscribers() {
		return subscribers;
	}

	public void setSubscribers(Set<Patient> subscribers) {
		this.subscribers = subscribers;
	}

	public void addSubscriber(Patient patient){
		this.subscribers.add(patient);
	}

	public void removeSubscriber(Patient patient){
		this.subscribers.removeIf(subscribedPatient -> (subscribedPatient.getId().equals(patient.getId())));
	}

	public Set<AvailableDrug> getAvailableDrugs() {
		return availableDrugs;
	}

	public void setAvailableDrugs(Set<AvailableDrug> availableDrugs) {
		this.availableDrugs = availableDrugs;
	}

	public Set<WorkingTimes> getConsultants() {
		return consultants;
	}

	public void setConsultants(Set<WorkingTimes> consultants) {
		this.consultants = consultants;
	}

	public Set<PharmacyRating> getRatings() {
		return ratings;
	}

	public void setRatings(Set<PharmacyRating> ratings) {
		this.ratings = ratings;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pharmacy other = (Pharmacy) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public Double getAverageRating() {
		if (ratings.isEmpty())
			return 0.0;

		double sum = 0;
		for (PharmacyRating rating : ratings) {
			sum = sum + rating.getRating().getRating();
		}

		return sum / ratings.size();
	}

}
