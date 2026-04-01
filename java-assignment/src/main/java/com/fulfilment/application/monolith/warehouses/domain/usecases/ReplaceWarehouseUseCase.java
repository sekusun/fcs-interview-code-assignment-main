package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator warehouseValidator;

  public ReplaceWarehouseUseCase(
      WarehouseStore warehouseStore, WarehouseValidator warehouseValidator) {
    this.warehouseStore = warehouseStore;
    this.warehouseValidator = warehouseValidator;
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

    warehouseValidator.validateLocationConstraints(
        newWarehouse.location, newWarehouse.capacity, existing.businessUnitCode);
    warehouseValidator.validateStockNotExceedingCapacity(newWarehouse.stock, newWarehouse.capacity);

    existing.archivedAt = LocalDateTime.now();
    warehouseStore.update(existing);

    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}
