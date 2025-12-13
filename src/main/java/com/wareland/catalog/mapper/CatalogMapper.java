package com.wareland.catalog.mapper;

import com.wareland.catalog.dto.CatalogPropertyResponse;
import com.wareland.property.model.Property;
import org.springframework.stereotype.Component;

@Component
public class CatalogMapper {

    public CatalogPropertyResponse toResponse(Property property) {
        if (property == null) return null;
        return new CatalogPropertyResponse(
                property.getPropertyId() == null ? 0 : property.getPropertyId(),
                property.getAddress(),
                property.getPrice(),
                property.getDescription()
        );
    }
}
