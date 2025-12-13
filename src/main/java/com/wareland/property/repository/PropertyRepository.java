package com.wareland.property.repository;

import com.wareland.property.model.Property;
import com.wareland.user.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Integer> {

    List<Property> findBySeller(Seller seller);

    boolean existsByPropertyIdAndSeller(Integer propertyId, Seller seller);

    Optional<Property> findByPropertyIdAndSeller(Integer propertyId, Seller seller);
}
