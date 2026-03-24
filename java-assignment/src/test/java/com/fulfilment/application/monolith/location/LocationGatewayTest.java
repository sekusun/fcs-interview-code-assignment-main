package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LocationGatewayTest {

  @Test
  void resolveByIdentifier_positive_returnsMatchingLocation() {
    LocationGateway gateway = new LocationGateway();
    var location = gateway.resolveByIdentifier("ZWOLLE-001");
    assertEquals("ZWOLLE-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  void resolveByIdentifier_negative_unknownIdentifier_throwsIllegalArgumentException() {
    LocationGateway gateway = new LocationGateway();
    assertThrows(
        IllegalArgumentException.class, () -> gateway.resolveByIdentifier("UNKNOWN-LOCATION-999"));
  }

  @Test
  void resolveByIdentifier_error_nullIdentifier_throwsNullPointerException() {
    LocationGateway gateway = new LocationGateway();
    assertThrows(NullPointerException.class, () -> gateway.resolveByIdentifier(null));
  }
}
