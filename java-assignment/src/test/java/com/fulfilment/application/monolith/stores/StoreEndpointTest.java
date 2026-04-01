package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreEndpointTest {

  @Test
  void get_invalidId_returns400() {
    given().when().get("/store/0").then().statusCode(400);
  }

  @Test
  void delete_invalidId_returns400() {
    given().when().delete("/store/0").then().statusCode(400);
  }
}
