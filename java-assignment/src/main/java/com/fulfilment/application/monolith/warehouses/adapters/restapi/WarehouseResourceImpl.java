package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;
  @Inject private CreateWarehouseOperation createWarehouseOperation;
  @Inject private ReplaceWarehouseOperation replaceWarehouseOperation;
  @Inject private ArchiveWarehouseOperation archiveWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    var domainWarehouse = toDomainModel(data);
    try {
      createWarehouseOperation.create(domainWarehouse);
    } catch (WarehouseValidationException e) {
      throw new WebApplicationException(e.getMessage(), e.getStatusCode());
    }
    return toWarehouseResponse(domainWarehouse);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    DbWarehouse entity = warehouseRepository.findById(parseWarehouseId(id));
    if (entity == null || entity.archivedAt != null) {
      throw new WebApplicationException("Warehouse not found: " + id, 404);
    }
    return toWarehouseResponse(entity.toWarehouse());
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    DbWarehouse entity = warehouseRepository.findById(parseWarehouseId(id));
    if (entity == null || entity.archivedAt != null) {
      throw new WebApplicationException("Warehouse not found: " + id, 404);
    }
    try {
      archiveWarehouseOperation.archive(entity.toWarehouse());
    } catch (WarehouseValidationException e) {
      throw new WebApplicationException(e.getMessage(), e.getStatusCode());
    }
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    var domainWarehouse = toDomainModel(data);
    domainWarehouse.businessUnitCode = businessUnitCode;
    try {
      replaceWarehouseOperation.replace(domainWarehouse);
    } catch (WarehouseValidationException e) {
      throw new WebApplicationException(e.getMessage(), e.getStatusCode());
    }
    return toWarehouseResponse(domainWarehouse);
  }

  private Long parseWarehouseId(String id) {
    try {
      return Long.parseLong(id);
    } catch (NumberFormatException e) {
      throw new WebApplicationException("Invalid warehouse id: " + id, 400);
    }
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomainModel(
      Warehouse apiBean) {
    var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.businessUnitCode = apiBean.getBusinessUnitCode();
    warehouse.location = apiBean.getLocation();
    warehouse.capacity = apiBean.getCapacity();
    warehouse.stock = apiBean.getStock();
    return warehouse;
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);
    return response;
  }
}
