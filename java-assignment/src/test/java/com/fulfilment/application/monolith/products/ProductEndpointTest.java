package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ProductEndpointTest {

  @Test
  void testCrudProduct() {
    final String path = "product";
    String uniqueName = "CRUD-TEST-" + System.nanoTime();

    int id =
        given()
            .contentType("application/json")
            .body("{\"name\": \"" + uniqueName + "\", \"stock\": 1}")
            .when()
            .post(path)
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString(uniqueName));

    given().when().delete(path + "/" + id).then().statusCode(204);

    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(not(containsString(uniqueName)), containsString("TONSTAD"), containsString("KALLAX"));
  }
}
