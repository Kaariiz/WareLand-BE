package com.wareland.property.service;

import com.wareland.common.exception.BadRequestException;
import com.wareland.common.exception.BusinessException;
import com.wareland.common.exception.ResourceNotFoundException;
import com.wareland.property.model.Property;
import com.wareland.property.repository.PropertyRepository;
import com.wareland.user.model.Seller;
import com.wareland.user.model.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    // Validasi ownership dilakukan di setiap operasi yang memodifikasi data
    public Property createProperty(Seller seller, Property data) {
        validateSeller(seller);
        if (data == null) {
            throw new BadRequestException("Data property tidak boleh kosong");
        }
        data.setSeller(seller); // set ownership
        return propertyRepository.save(data);
    }

    public void updateProperty(Seller seller, Property property) {
        validateSeller(seller);
        if (property == null || property.getPropertyId() == null) {
            throw new BadRequestException("Property ID wajib diisi untuk update");
        }

        Property existing = propertyRepository.findByPropertyIdAndSeller(property.getPropertyId(), seller)
                .orElseThrow(() -> new BusinessException("Anda tidak berhak mengubah property ini atau property tidak ditemukan"));

        existing.updateDetails(property.getAddress(), property.getPrice(), property.getDescription());
        propertyRepository.save(existing);
    }

    public void deleteProperty(Seller seller, int propertyId) {
        validateSeller(seller);
        Property existing = propertyRepository.findByPropertyIdAndSeller(propertyId, seller)
                .orElseThrow(() -> new BusinessException("Anda tidak berhak menghapus property ini atau property tidak ditemukan"));
        propertyRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public boolean verifyOwnership(Seller seller, int propertyId) {
        validateSeller(seller);
        return propertyRepository.existsByPropertyIdAndSeller(propertyId, seller);
    }

    @Transactional(readOnly = true)
    public List<Property> getSellerProperties(Seller seller) {
        validateSeller(seller);
        return propertyRepository.findBySeller(seller);
    }

    private void validateSeller(Seller seller) {
        if (seller == null || seller.getUserRole() != UserRole.SELLER) {
            throw new BusinessException("Hanya Seller yang dapat mengelola Property");
        }
    }
}
