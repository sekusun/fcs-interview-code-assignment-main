package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("fulfillment")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfillmentResource {

  private static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;
  private static final int MAX_WAREHOUSES_PER_STORE = 3;
  private static final int MAX_PRODUCTS_PER_WAREHOUSE = 5;

  @Inject FulfillmentRepository fulfillmentRepository;
  @Inject ProductRepository productRepository;
  @Inject WarehouseRepository warehouseRepository;

  @GET
  public List<FulfillmentResponse> listAll() {
    return fulfillmentRepository.listAll().stream().map(FulfillmentResponse::from).toList();
  }

  @GET
  @Path("store/{storeId}")
  public List<FulfillmentResponse> getByStore(Long storeId) {
    validateId(storeId, "store");
    Store store = Store.findById(storeId);
    if (store == null) {
      throw new WebApplicationException("Store not found: " + storeId, 404);
    }
    return fulfillmentRepository.findByStore(storeId).stream()
        .map(FulfillmentResponse::from)
        .toList();
  }

  @GET
  @Path("warehouse/{warehouseId}")
  public List<FulfillmentResponse> getByWarehouse(Long warehouseId) {
    validateId(warehouseId, "warehouse");
    DbWarehouse warehouse = warehouseRepository.findById(warehouseId);
    if (warehouse == null || warehouse.archivedAt != null) {
      throw new WebApplicationException("Warehouse not found: " + warehouseId, 404);
    }
    return fulfillmentRepository.findByWarehouse(warehouseId).stream()
        .map(FulfillmentResponse::from)
        .toList();
  }

  @POST
  @Transactional
  public Response create(FulfillmentRequest request) {
    if (request.productId == null || request.storeId == null || request.warehouseId == null) {
      throw new WebApplicationException("productId, storeId, and warehouseId are required.", 422);
    }

    Product product = productRepository.findById(request.productId);
    if (product == null) {
      throw new WebApplicationException("Product not found: " + request.productId, 404);
    }

    Store store = Store.findById(request.storeId);
    if (store == null) {
      throw new WebApplicationException("Store not found: " + request.storeId, 404);
    }

    DbWarehouse warehouse = warehouseRepository.findById(request.warehouseId);
    if (warehouse == null || warehouse.archivedAt != null) {
      throw new WebApplicationException("Warehouse not found: " + request.warehouseId, 404);
    }

    if (fulfillmentRepository.existsDuplicate(
        request.productId, request.storeId, request.warehouseId)) {
      throw new WebApplicationException("This fulfillment assignment already exists.", 409);
    }

    long warehousesForProductStore =
        fulfillmentRepository.countDistinctWarehousesForProductAndStore(
            request.productId, request.storeId);
    if (warehousesForProductStore >= MAX_WAREHOUSES_PER_PRODUCT_PER_STORE) {
      throw new WebApplicationException(
          "Product "
              + product.name
              + " already has "
              + MAX_WAREHOUSES_PER_PRODUCT_PER_STORE
              + " warehouses assigned for store "
              + store.name,
          400);
    }

    long warehousesForStore =
        fulfillmentRepository.countDistinctWarehousesForStore(request.storeId);
    if (warehousesForStore >= MAX_WAREHOUSES_PER_STORE) {
      boolean isNewWarehouseForStore =
          fulfillmentRepository.findByStore(request.storeId).stream()
              .noneMatch(a -> a.warehouse.id.equals(request.warehouseId));
      if (isNewWarehouseForStore) {
        throw new WebApplicationException(
            "Store "
                + store.name
                + " already has "
                + MAX_WAREHOUSES_PER_STORE
                + " different warehouses assigned",
            400);
      }
    }

    long productsForWarehouse =
        fulfillmentRepository.countDistinctProductsForWarehouse(request.warehouseId);
    if (productsForWarehouse >= MAX_PRODUCTS_PER_WAREHOUSE) {
      boolean isNewProductForWarehouse =
          fulfillmentRepository.findByWarehouse(request.warehouseId).stream()
              .noneMatch(a -> a.product.id.equals(request.productId));
      if (isNewProductForWarehouse) {
        throw new WebApplicationException(
            "Warehouse "
                + warehouse.businessUnitCode
                + " already stores "
                + MAX_PRODUCTS_PER_WAREHOUSE
                + " types of products",
            400);
      }
    }

    var assignment = new FulfillmentAssignment();
    assignment.product = product;
    assignment.store = store;
    assignment.warehouse = warehouse;
    fulfillmentRepository.persist(assignment);

    return Response.ok(FulfillmentResponse.from(assignment)).status(201).build();
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    validateId(id, "fulfillment assignment");
    FulfillmentAssignment entity = fulfillmentRepository.findById(id);
    if (entity == null) {
      throw new WebApplicationException(
          "Fulfillment assignment with id " + id + " does not exist.", 404);
    }
    fulfillmentRepository.delete(entity);
    return Response.status(204).build();
  }

  private void validateId(Long id, String resourceName) {
    if (id == null || id <= 0) {
      throw new WebApplicationException("Invalid " + resourceName + " id: " + id, 400);
    }
  }
}
