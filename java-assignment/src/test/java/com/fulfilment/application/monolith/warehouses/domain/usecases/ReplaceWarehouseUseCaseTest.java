package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ReplaceWarehouseUseCaseTest {

  @Inject ReplaceWarehouseOperation replaceWarehouseOperation;
  @Inject WarehouseRepository warehouseRepository;

  @Test
  @Transactional
  void replace_positive_archivesOldAndCreatesNewWithSameBusinessUnitCode() {
    var newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.012";
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.capacity = 45;
    newWarehouse.stock = 5;

    replaceWarehouseOperation.replace(newWarehouse);

    var active = warehouseRepository.findByBusinessUnitCode("MWH.012");
    assertNotNull(active);
    assertEquals(45, active.capacity);
    assertEquals(5, active.stock);
    assertEquals("AMSTERDAM-001", active.location);
  }

  @Test
  @Transactional
  void replace_error_unknownBusinessUnit_throws404() {
    var newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "NON.EXISTENT.BU";
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.capacity = 50;
    newWarehouse.stock = 5;

    WarehouseValidationException ex =
        assertThrows(
            WarehouseValidationException.class, () -> replaceWarehouseOperation.replace(newWarehouse));
    assertEquals(404, ex.getStatusCode());
  }

  @Test
  @Transactional
  void replace_negative_capacityLessThanExistingStock_throws400() {
    var newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.023";
    newWarehouse.location = "TILBURG-001";
    newWarehouse.capacity = 26;
    newWarehouse.stock = 27;

    assertThrows(WarehouseValidationException.class, () -> replaceWarehouseOperation.replace(newWarehouse));
  }

  @Test
  @Transactional
  void replace_negative_stockMismatch_throws400() {
    var newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.023";
    newWarehouse.location = "TILBURG-001";
    newWarehouse.capacity = 50;
    newWarehouse.stock = 1;

    assertThrows(WarehouseValidationException.class, () -> replaceWarehouseOperation.replace(newWarehouse));
  }
}
