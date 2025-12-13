package com.wareland.catalog.controller;

import com.wareland.catalog.dto.CatalogPropertyResponse;
import com.wareland.catalog.dto.CatalogSearchRequest;
import com.wareland.catalog.service.CatalogService;
import com.wareland.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    // GET /api/catalog/properties
    @GetMapping("/properties")
    public ResponseEntity<ApiResponse<List<CatalogPropertyResponse>>> getAllProperties() {
        List<CatalogPropertyResponse> data = catalogService.showAllProperties();
        if (data.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Properti tidak tersedia", data));
        }
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // GET /api/catalog/properties/search
    @GetMapping("/properties/search")
    public ResponseEntity<ApiResponse<List<CatalogPropertyResponse>>> searchProperties(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice
    ) {
        // Controller hanya mengikat request, tanpa business logic
        CatalogSearchRequest req = new CatalogSearchRequest();
        req.setKeyword(keyword);
        req.setMinPrice(minPrice);
        req.setMaxPrice(maxPrice);

        List<CatalogPropertyResponse> data = catalogService.searchProperties(req);
        if (data.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Properti tidak tersedia", data));
        }
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // GET /api/catalog/properties/{propertyId}
    @GetMapping("/properties/{propertyId}")
    public ResponseEntity<ApiResponse<CatalogPropertyResponse>> getPropertyDetail(@PathVariable int propertyId) {
        CatalogPropertyResponse detail = catalogService.getPropertyDetail(propertyId);
        if (detail == null) {
            return ResponseEntity.ok(ApiResponse.success("Properti tidak tersedia", null));
        }
        return ResponseEntity.ok(ApiResponse.success(detail));
    }
}
