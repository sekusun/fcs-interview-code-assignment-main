package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ArchiveWarehouseUseCaseTest {

  @Inject ArchiveWarehouseOperation archiveWarehouseOperation;
  @Inject CreateWarehouseOperation createWarehouseOperation;
  @Inject WarehouseRepository warehouseRepository;

  @Test
  @Transactional
  void archive_positive_setsArchivedAtAndRemovesFromActiveLookup() {
    String bu = "TEST.ARCHIVE." + UUID.randomUUID().toString().substring(0, 8);
    var toCreate = new Warehouse();
    toCreate.businessUnitCode = bu;
    toCreate.location = "HELMOND-001";
    toCreate.capacity = 30;
    toCreate.stock = 5;
    createWarehouseOperation.create(toCreate);

    Warehouse active = warehouseRepository.findByBusinessUnitCode(bu);
    archiveWarehouseOperation.archive(active);

    assertNull(warehouseRepository.findByBusinessUnitCode(bu));
    assertNotNull(
        warehouseRepository.find("businessUnitCode = ?1 AND archivedAt IS NOT NULL", bu).firstResult());
  }
}
