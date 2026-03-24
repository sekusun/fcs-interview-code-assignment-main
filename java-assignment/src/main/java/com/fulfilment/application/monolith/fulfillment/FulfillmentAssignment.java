package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "fulfillment_assignment",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"product_id", "store_id", "warehouse_id"}))
@Cacheable
public class FulfillmentAssignment {

  @Id @GeneratedValue public Long id;

  @ManyToOne
  @JoinColumn(name = "product_id", nullable = false)
  public Product product;

  @ManyToOne
  @JoinColumn(name = "store_id", nullable = false)
  public Store store;

  @ManyToOne
  @JoinColumn(name = "warehouse_id", nullable = false)
  public DbWarehouse warehouse;

  public FulfillmentAssignment() {}
}
