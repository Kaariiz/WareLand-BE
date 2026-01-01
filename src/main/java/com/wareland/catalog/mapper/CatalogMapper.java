package com.wareland.catalog.mapper;

import org.springframework.stereotype.Component;

import com.wareland.catalog.dto.CatalogPropertyResponse;
import com.wareland.catalog.dto.CatalogPropertyResponse.SellerInfo;
import com.wareland.property.model.Property;
import com.wareland.user.model.Seller;

@Component
public class CatalogMapper {

    public CatalogPropertyResponse toResponse(Property property) {
        if (property == null) return null;
        
        SellerInfo sellerInfo = null;
        Seller seller = property.getSeller();
        if (seller != null) {
            sellerInfo = new SellerInfo(
                    seller.getUserId(),
                    seller.getUsername(),
                    seller.getName(),
                    seller.getEmail(),
                    seller.getPhoneNumber(),
                    seller.getUserRole() != null ? seller.getUserRole().name() : null,
                    seller.getCreatedAt(),
                    seller.getUpdatedAt()
            );
        }
        
        return new CatalogPropertyResponse(
                property.getPropertyId() == null ? 0 : property.getPropertyId(),
                property.getAddress(),
                property.getPrice(),
                property.getDescription(),
                property.getImageUrl(),
                sellerInfo
        );
    }
}
