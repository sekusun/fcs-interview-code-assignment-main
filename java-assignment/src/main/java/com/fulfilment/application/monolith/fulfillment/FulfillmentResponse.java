package com.fulfilment.application.monolith.fulfillment;

public class FulfillmentResponse {
  public Long id;
  public Long productId;
  public String productName;
  public Long storeId;
  public String storeName;
  public Long warehouseId;
  public String warehouseBusinessUnitCode;

  public static FulfillmentResponse from(FulfillmentAssignment a) {
    var r = new FulfillmentResponse();
    r.id = a.id;
    r.productId = a.product.id;
    r.productName = a.product.name;
    r.storeId = a.store.id;
    r.storeName = a.store.name;
    r.warehouseId = a.warehouse.id;
    r.warehouseBusinessUnitCode = a.warehouse.businessUnitCode;
    return r;
  }
}
