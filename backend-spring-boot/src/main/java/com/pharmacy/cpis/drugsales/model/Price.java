package com.pharmacy.cpis.drugsales.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.pharmacy.cpis.util.DateRange;

@Entity
public class Price {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Double price;

	@Embedded
	private DateRange validityPeriod;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private AvailableDrug drug;

	public Price() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public DateRange getValidityPeriod() {
		return validityPeriod;
	}

	public void setValidityPeriod(DateRange validityPeriod) {
		this.validityPeriod = validityPeriod;
	}

	public AvailableDrug getDrug() {
		return drug;
	}

	public void setDrug(AvailableDrug drug) {
		this.drug = drug;
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
		Price other = (Price) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
