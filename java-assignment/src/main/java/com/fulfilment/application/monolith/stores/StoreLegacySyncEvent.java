package com.fulfilment.application.monolith.stores;

public record StoreLegacySyncEvent(Kind kind, Long id, String name, int quantityProductsInStock) {

  public enum Kind {
    CREATE,
    UPDATE
  }
}
