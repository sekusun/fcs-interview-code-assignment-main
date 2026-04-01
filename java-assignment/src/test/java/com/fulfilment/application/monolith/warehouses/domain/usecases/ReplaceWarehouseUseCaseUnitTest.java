package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReplaceWarehouseUseCaseUnitTest {

  private InMemoryWarehouseStore store;
  private ReplaceWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    store = new InMemoryWarehouseStore();
    LocationResolver resolver = id -> new Location(id, 5, 500);
    WarehouseValidator validator = new WarehouseValidator(store, resolver);
    useCase = new ReplaceWarehouseUseCase(store, validator);

    Warehouse existing = warehouse("BU-OLD", "LOC-1", 100, 30);
    store.create(existing);
  }

  @Test
  void replace_archivesExistingAndCreatesNewWarehouse() {
    Warehouse replacement = warehouse("BU-OLD", "LOC-1", 120, 30);

    useCase.replace(replacement);

    Warehouse active = store.findByBusinessUnitCode("BU-OLD");
    assertNotNull(active);
    assertNotNull(active.createdAt);
  }

  @Test
  void replace_throwsWhenStockMismatch() {
    Warehouse replacement = warehouse("BU-OLD", "LOC-1", 120, 29);

    assertThrows(WarehouseValidationException.class, () -> useCase.replace(replacement));
  }

  private static Warehouse warehouse(String bu, String loc, int capacity, int stock) {
    Warehouse w = new Warehouse();
    w.businessUnitCode = bu;
    w.location = loc;
    w.capacity = capacity;
    w.stock = stock;
    return w;
  }

  private static class InMemoryWarehouseStore implements WarehouseStore {
    private final List<Warehouse> records = new ArrayList<>();

    @Override
    public List<Warehouse> getAll() {
      return records;
    }

    @Override
    public void create(Warehouse warehouse) {
      records.add(warehouse);
    }

    @Override
    public void update(Warehouse warehouse) {}

    @Override
    public void remove(Warehouse warehouse) {
      records.remove(warehouse);
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return records.stream()
          .filter(w -> buCode.equals(w.businessUnitCode) && w.archivedAt == null)
          .findFirst()
          .orElse(null);
    }

    @Override
    public List<Warehouse> findActiveByLocation(String location) {
      return records.stream()
          .filter(w -> location.equals(w.location) && w.archivedAt == null)
          .toList();
    }
  }
}
