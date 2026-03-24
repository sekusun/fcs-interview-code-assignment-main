package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class FulfillmentRepository implements PanacheRepository<FulfillmentAssignment> {

  public List<FulfillmentAssignment> findByProductAndStore(Long productId, Long storeId) {
    return list("product.id = ?1 AND store.id = ?2", productId, storeId);
  }

  public List<FulfillmentAssignment> findByStore(Long storeId) {
    return list("store.id", storeId);
  }

  public List<FulfillmentAssignment> findByWarehouse(Long warehouseId) {
    return list("warehouse.id", warehouseId);
  }

  public long countDistinctWarehousesForProductAndStore(Long productId, Long storeId) {
    return count(
        "product.id = ?1 AND store.id = ?2", productId, storeId);
  }

  public long countDistinctWarehousesForStore(Long storeId) {
    return find("store.id", storeId)
        .stream()
        .map(a -> a.warehouse.id)
        .distinct()
        .count();
  }

  public long countDistinctProductsForWarehouse(Long warehouseId) {
    return find("warehouse.id", warehouseId)
        .stream()
        .map(a -> a.product.id)
        .distinct()
        .count();
  }

  public boolean existsDuplicate(Long productId, Long storeId, Long warehouseId) {
    return count(
            "product.id = ?1 AND store.id = ?2 AND warehouse.id = ?3",
            productId,
            storeId,
            warehouseId)
        > 0;
  }
}
