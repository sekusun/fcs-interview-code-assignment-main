package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existing == null) {
      throw new WarehouseValidationException(
          "Warehouse not found: " + newWarehouse.businessUnitCode, 404);
    }

    if (newWarehouse.capacity < existing.stock) {
      throw new WarehouseValidationException(
          "New warehouse capacity ("
              + newWarehouse.capacity
              + ") cannot accommodate existing stock ("
              + existing.stock
              + ")");
    }

    if (!newWarehouse.stock.equals(existing.stock)) {
      throw new WarehouseValidationException(
          "New warehouse stock ("
              + newWarehouse.stock
              + ") must match existing warehouse stock ("
              + existing.stock
              + ")");
    }

    Location location;
    try {
      location = locationResolver.resolveByIdentifier(newWarehouse.location);
    } catch (IllegalArgumentException e) {
      throw new WarehouseValidationException("Invalid location: " + newWarehouse.location);
    }

    List<Warehouse> activeAtLocation = warehouseStore.findActiveByLocation(newWarehouse.location);

    long warehouseCount = activeAtLocation.stream()
        .filter(w -> !w.businessUnitCode.equals(existing.businessUnitCode))
        .count();
    if (warehouseCount >= location.maxNumberOfWarehouses) {
      throw new WarehouseValidationException(
          "Maximum number of warehouses reached for location: " + newWarehouse.location);
    }

    int totalCapacity = activeAtLocation.stream()
        .filter(w -> !w.businessUnitCode.equals(existing.businessUnitCode))
        .mapToInt(w -> w.capacity)
        .sum();
    if (totalCapacity + newWarehouse.capacity > location.maxCapacity) {
      throw new WarehouseValidationException(
          "Warehouse capacity exceeds maximum capacity for location: " + newWarehouse.location);
    }

    existing.archivedAt = LocalDateTime.now();
    warehouseStore.update(existing);

    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}
