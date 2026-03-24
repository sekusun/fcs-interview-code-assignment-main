package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

@ApplicationScoped
public class StoreLegacySyncObserver {

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;

  void onCommitted(@Observes(during = TransactionPhase.AFTER_SUCCESS) StoreLegacySyncEvent event) {
    Store store = new Store();
    store.id = event.id();
    store.name = event.name();
    store.quantityProductsInStock = event.quantityProductsInStock();

    switch (event.kind()) {
      case CREATE -> legacyStoreManagerGateway.createStoreOnLegacySystem(store);
      case UPDATE -> legacyStoreManagerGateway.updateStoreOnLegacySystem(store);
    }
  }
}
