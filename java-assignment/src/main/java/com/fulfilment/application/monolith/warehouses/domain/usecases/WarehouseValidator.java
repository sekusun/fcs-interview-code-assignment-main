package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class WarehouseValidator {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public WarehouseValidator(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  public void ensureBusinessUnitCodeDoesNotExist(String businessUnitCode) {
    if (warehouseStore.findByBusinessUnitCode(businessUnitCode) != null) {
      throw new WarehouseValidationException("Business unit code already exists: " + businessUnitCode, 409);
    }
  }

  public Location resolveValidLocation(String locationIdentifier) {
    try {
      return locationResolver.resolveByIdentifier(locationIdentifier);
    } catch (IllegalArgumentException e) {
      throw new WarehouseValidationException("Invalid location: " + locationIdentifier);
    }
  }

  public void validateLocationConstraints(
      String locationIdentifier, Integer candidateCapacity, String excludedBusinessUnitCode) {
    Location location = resolveValidLocation(locationIdentifier);
    List<Warehouse> activeAtLocation = warehouseStore.findActiveByLocation(locationIdentifier);

    List<Warehouse> relevantWarehouses =
        excludedBusinessUnitCode == null
            ? activeAtLocation
            : activeAtLocation.stream()
                .filter(w -> !w.businessUnitCode.equals(excludedBusinessUnitCode))
                .toList();

    if (relevantWarehouses.size() >= location.maxNumberOfWarehouses) {
      throw new WarehouseValidationException(
          "Maximum number of warehouses reached for location: " + locationIdentifier);
    }

    int totalCapacity = relevantWarehouses.stream().mapToInt(w -> w.capacity).sum();
    if (totalCapacity + candidateCapacity > location.maxCapacity) {
      throw new WarehouseValidationException(
          "Warehouse capacity exceeds maximum capacity for location: " + locationIdentifier);
    }
  }

  public void validateStockNotExceedingCapacity(Integer stock, Integer capacity) {
    if (stock != null && capacity != null && stock > capacity) {
      throw new WarehouseValidationException(
          "Stock (" + stock + ") exceeds warehouse capacity (" + capacity + ")");
    }
  }
}
