package com.pharmacy.cpis.consultations.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.pharmacy.cpis.pharmacy.model.Pharmacy;
import com.pharmacy.cpis.users.model.Consultant;
import com.pharmacy.cpis.users.model.Patient;
import com.pharmacy.cpis.util.DateRange;

@Entity
public class Consultation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private ConsultationStatus status;

	@Embedded
	private DateRange time;

	@Column(nullable = false)
	private Double price;

	@ManyToOne(optional = false)
	private Pharmacy pharmacy;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Consultant consultant;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Patient patient;

	public Consultation() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ConsultationStatus getStatus() {
		return status;
	}

	public void setStatus(ConsultationStatus status) {
		this.status = status;
	}

	public DateRange getTime() {
		return time;
	}

	public void setTime(DateRange time) {
		this.time = time;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Pharmacy getPharmacy() {
		return pharmacy;
	}

	public void setPharmacy(Pharmacy pharmacy) {
		this.pharmacy = pharmacy;
	}

	public Consultant getConsultant() {
		return consultant;
	}

	public void setConsultant(Consultant consultant) {
		this.consultant = consultant;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
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
		Consultation other = (Consultation) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
