package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CreateWarehouseUseCaseTest {

  @Inject CreateWarehouseOperation createWarehouseOperation;
  @Inject WarehouseRepository warehouseRepository;

  @Test
  @Transactional
  void create_positive_persistsWarehouseAtLocationWithCapacity() {
    String bu = "TEST.CREATE." + UUID.randomUUID().toString().substring(0, 8);
    var w = new Warehouse();
    w.businessUnitCode = bu;
    w.location = "HELMOND-001";
    w.capacity = 30;
    w.stock = 10;

    createWarehouseOperation.create(w);

    var stored = warehouseRepository.findByBusinessUnitCode(bu);
    assertNotNull(stored);
    assertEquals("HELMOND-001", stored.location);
    assertEquals(30, stored.capacity);
    assertEquals(10, stored.stock);
  }

  @Test
  @Transactional
  void create_negative_duplicateBusinessUnitCode_throws409() {
    var w = new Warehouse();
    w.businessUnitCode = "MWH.001";
    w.location = "EINDHOVEN-001";
    w.capacity = 10;
    w.stock = 1;

    WarehouseValidationException ex =
        assertThrows(WarehouseValidationException.class, () -> createWarehouseOperation.create(w));
    assertEquals(409, ex.getStatusCode());
  }

  @Test
  @Transactional
  void create_negative_invalidLocation_throws400() {
    var w = new Warehouse();
    w.businessUnitCode = "TEST.CREATE.BADLOC." + UUID.randomUUID().toString().substring(0, 8);
    w.location = "NOT-A-REAL-LOCATION";
    w.capacity = 10;
    w.stock = 1;

    assertThrows(WarehouseValidationException.class, () -> createWarehouseOperation.create(w));
  }

  @Test
  @Transactional
  void create_negative_maxWarehousesPerLocationReached_throws400() {
    var w = new Warehouse();
    w.businessUnitCode = "TEST.CREATE.MAXWH." + UUID.randomUUID().toString().substring(0, 8);
    w.location = "ZWOLLE-001";
    w.capacity = 10;
    w.stock = 1;

    assertThrows(WarehouseValidationException.class, () -> createWarehouseOperation.create(w));
  }

  @Test
  @Transactional
  void create_negative_totalCapacityExceedsLocationMax_throws400() {
    var w = new Warehouse();
    w.businessUnitCode = "TEST.CREATE.CAP." + UUID.randomUUID().toString().substring(0, 8);
    w.location = "EINDHOVEN-001";
    w.capacity = 71;
    w.stock = 1;

    assertThrows(WarehouseValidationException.class, () -> createWarehouseOperation.create(w));
  }

  @Test
  @Transactional
  void create_negative_stockExceedsCapacity_throws400() {
    var w = new Warehouse();
    w.businessUnitCode = "TEST.CREATE.STK." + UUID.randomUUID().toString().substring(0, 8);
    w.location = "HELMOND-001";
    w.capacity = 10;
    w.stock = 11;

    assertThrows(WarehouseValidationException.class, () -> createWarehouseOperation.create(w));
  }
}
