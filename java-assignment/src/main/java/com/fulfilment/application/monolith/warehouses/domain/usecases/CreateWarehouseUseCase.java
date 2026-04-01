package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator warehouseValidator;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, WarehouseValidator warehouseValidator) {
    this.warehouseStore = warehouseStore;
    this.warehouseValidator = warehouseValidator;
  }

  @Override
  public void create(Warehouse warehouse) {
    warehouseValidator.ensureBusinessUnitCodeDoesNotExist(warehouse.businessUnitCode);
    warehouseValidator.validateLocationConstraints(warehouse.location, warehouse.capacity, null);
    warehouseValidator.validateStockNotExceedingCapacity(warehouse.stock, warehouse.capacity);

    warehouse.createdAt = LocalDateTime.now();
    warehouseStore.create(warehouse);
  }
}
