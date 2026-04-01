package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

class WarehouseValidatorTest {

  private InMemoryWarehouseStore warehouseStore;
  private LocationResolver locationResolver;
  private WarehouseValidator validator;

  @BeforeEach
  void setUp() {
    warehouseStore = new InMemoryWarehouseStore();
    locationResolver =
        identifier -> {
          if (!"LOC-1".equals(identifier)) {
            throw new IllegalArgumentException("Unknown location");
          }
          return new Location("LOC-1", 2, 100);
        };
    validator = new WarehouseValidator(warehouseStore, locationResolver);
  }

  @Test
  void ensureBusinessUnitCodeDoesNotExist_throwsWhenDuplicate() {
    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "BU-1";
    warehouseStore.records.add(existing);

    assertThrows(
        WarehouseValidationException.class, () -> validator.ensureBusinessUnitCodeDoesNotExist("BU-1"));
  }

  @Test
  void resolveValidLocation_throwsForUnknownLocation() {
    assertThrows(
        WarehouseValidationException.class, () -> validator.resolveValidLocation("UNKNOWN-LOCATION"));
  }

  @Test
  void validateLocationConstraints_throwsWhenMaxWarehousesReached() {
    warehouseStore.activeAtLocation.add(warehouseWith("BU-1", 20));
    warehouseStore.activeAtLocation.add(warehouseWith("BU-2", 30));

    assertThrows(
        WarehouseValidationException.class,
        () -> validator.validateLocationConstraints("LOC-1", 10, null));
  }

  @Test
  void validateLocationConstraints_throwsWhenCapacityExceeded() {
    warehouseStore.activeAtLocation.add(warehouseWith("BU-1", 60));

    assertThrows(
        WarehouseValidationException.class,
        () -> validator.validateLocationConstraints("LOC-1", 50, null));
  }

  @Test
  void validateLocationConstraints_allowsReplacementWhenExcludingCurrentWarehouse() {
    warehouseStore.activeAtLocation.add(warehouseWith("BU-OLD", 70));
    warehouseStore.activeAtLocation.add(warehouseWith("BU-OTHER", 20));

    assertDoesNotThrow(() -> validator.validateLocationConstraints("LOC-1", 80, "BU-OLD"));
  }

  @Test
  void validateStockNotExceedingCapacity_throwsWhenStockGreaterThanCapacity() {
    assertThrows(
        WarehouseValidationException.class, () -> validator.validateStockNotExceedingCapacity(11, 10));
  }

  @Test
  void validateStockNotExceedingCapacity_allowsWhenValid() {
    assertDoesNotThrow(() -> validator.validateStockNotExceedingCapacity(10, 10));
  }

  private Warehouse warehouseWith(String buCode, int capacity) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = buCode;
    warehouse.capacity = capacity;
    return warehouse;
  }

  private static class InMemoryWarehouseStore implements WarehouseStore {
    private final List<Warehouse> records = new ArrayList<>();
    private final List<Warehouse> activeAtLocation = new ArrayList<>();

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
    public void remove(Warehouse warehouse) {}

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return records.stream().filter(w -> buCode.equals(w.businessUnitCode)).findFirst().orElse(null);
    }

    @Override
    public List<Warehouse> findActiveByLocation(String location) {
      return activeAtLocation;
    }
  }
}
