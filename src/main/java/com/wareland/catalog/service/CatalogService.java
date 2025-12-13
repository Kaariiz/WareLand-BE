package com.wareland.catalog.service;

import com.wareland.catalog.dto.CatalogPropertyResponse;
import com.wareland.catalog.dto.CatalogSearchRequest;
import com.wareland.catalog.mapper.CatalogMapper;
import com.wareland.catalog.repository.CatalogRepository;
import com.wareland.property.model.Property;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    private final CatalogRepository catalogRepository;
    private final CatalogMapper catalogMapper;

    public CatalogService(CatalogRepository catalogRepository, CatalogMapper catalogMapper) {
        this.catalogRepository = Objects.requireNonNull(catalogRepository);
        this.catalogMapper = Objects.requireNonNull(catalogMapper);
    }

    public List<CatalogPropertyResponse> showAllProperties() {
        List<Property> props = catalogRepository.findAll();
        return props.stream().map(catalogMapper::toResponse).collect(Collectors.toList());
    }

    public List<CatalogPropertyResponse> searchProperties(CatalogSearchRequest request) {
        // Pilih filterByCriteria untuk fleksibilitas penuh
        List<Property> props = catalogRepository.filterByCriteria(
                request != null ? request.getKeyword() : null,
                request != null ? request.getMinPrice() : null,
                request != null ? request.getMaxPrice() : null
        );
        return props.stream().map(catalogMapper::toResponse).collect(Collectors.toList());
    }

    public CatalogPropertyResponse getPropertyDetail(int propertyId) {
        return catalogRepository.findById(propertyId)
                .map(catalogMapper::toResponse)
                .orElse(null); // katalog publik: jika kosong, bukan error
    }
}
