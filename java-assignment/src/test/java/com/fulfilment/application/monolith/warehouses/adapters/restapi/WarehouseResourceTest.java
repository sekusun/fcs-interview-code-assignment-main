package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseResourceTest {

  @Test
  void get_positive_listContainsSeedWarehouses() {
    given()
        .when()
        .get("/warehouse")
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  @Test
  void get_error_unknownId_returns404() {
    given().when().get("/warehouse/999999").then().statusCode(404);
  }

  @Test
  void post_negative_invalidLocation_returns400() {
    String bu = "TEST.WH.HTTP." + UUID.randomUUID().toString().substring(0, 8);
    given()
        .contentType("application/json")
        .body(
            Map.of(
                "businessUnitCode",
                bu,
                "location",
                "NOT-A-VALID-LOCATION",
                "capacity",
                10,
                "stock",
                1))
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400);
  }
}
