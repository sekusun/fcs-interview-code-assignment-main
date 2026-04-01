package com.fulfilment.application.monolith.fulfillment;

import static io.restassured.RestAssured.given;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FulfillmentResourceTest {

  @Inject ProductRepository productRepository;
  @Inject WarehouseRepository warehouseRepository;

  @Test
  @Order(1)
  void post_positive_createsAssignment_returns201() {
    given()
        .contentType(ContentType.JSON)
        .body(Map.of("productId", 2, "storeId", 2, "warehouseId", 2))
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(201);
  }

  @Test
  @Order(2)
  void get_positive_list_returns200() {
    given().when().get("/fulfillment").then().statusCode(200);
  }

  @Test
  @Order(3)
  void get_positive_byStore_returns200() {
    given().when().get("/fulfillment/store/2").then().statusCode(200);
  }

  @Test
  @Order(4)
  void get_positive_byWarehouse_returns200() {
    given().when().get("/fulfillment/warehouse/2").then().statusCode(200);
  }

  @Test
  @Order(5)
  void post_error_duplicateAssignment_returns409() {
    given()
        .contentType(ContentType.JSON)
        .body(Map.of("productId", 2, "storeId", 2, "warehouseId", 2))
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(409);
  }

  @Test
  @Order(6)
  void post_negative_missingProductId_returns422() {
    given()
        .contentType(ContentType.JSON)
        .body(Map.of("storeId", 1, "warehouseId", 1))
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(422);
  }

  @Test
  @Order(7)
  void post_error_unknownProduct_returns404() {
    given()
        .contentType(ContentType.JSON)
        .body(Map.of("productId", 99999, "storeId", 1, "warehouseId", 1))
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(8)
  void post_negative_maxTwoWarehousesPerProductPerStore_returns400() {
    given()
        .contentType(ContentType.JSON)
        .body(Map.of("productId", 3, "storeId", 3, "warehouseId", 1))
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("productId", 3, "storeId", 3, "warehouseId", 2))
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("productId", 3, "storeId", 3, "warehouseId", 3))
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(9)
  void post_negative_maxThreeWarehousesPerStore_returns400() {
    Long w4Id =
        QuarkusTransaction.requiringNew()
            .call(
                () -> {
                  DbWarehouse w4 = new DbWarehouse();
                  w4.businessUnitCode = "TEST.FUL.W4";
                  w4.location = "EINDHOVEN-001";
                  w4.capacity = 40;
                  w4.stock = 0;
                  w4.createdAt = LocalDateTime.now();
                  warehouseRepository.persist(w4);
                  warehouseRepository.flush();
                  return w4.id;
                });

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("productId", 2, "storeId", 1, "warehouseId", 1))
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("productId", 3, "storeId", 1, "warehouseId", 2))
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("productId", 2, "storeId", 1, "warehouseId", 3))
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(
            Map.of(
                "productId",
                1,
                "storeId",
                1,
                "warehouseId",
                w4Id.intValue()))
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(10)
  void post_negative_maxFiveProductTypesPerWarehouse_returns400() {
    long[] ids =
        QuarkusTransaction.requiringNew()
            .call(
                () -> {
                  Product p4 = new Product();
                  p4.name = "FUL-TEST-P4-" + System.nanoTime();
                  p4.stock = 1;
                  productRepository.persist(p4);
                  Product p5 = new Product();
                  p5.name = "FUL-TEST-P5-" + System.nanoTime();
                  p5.stock = 1;
                  productRepository.persist(p5);
                  Product p6 = new Product();
                  p6.name = "FUL-TEST-P6-" + System.nanoTime();
                  p6.stock = 1;
                  productRepository.persist(p6);
                  productRepository.flush();
                  return new long[] {p4.id, p5.id, p6.id};
                });
    long p4id = ids[0];
    long p5id = ids[1];
    long p6id = ids[2];

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("productId", 1, "storeId", 2, "warehouseId", 1))
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("productId", 2, "storeId", 2, "warehouseId", 1))
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("productId", 3, "storeId", 2, "warehouseId", 1))
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(body(p4id, 2, 1))
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(body(p5id, 2, 1))
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(body(p6id, 2, 1))
        .when()
        .post("/fulfillment")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(11)
  void get_negative_byStore_invalidId_returns400() {
    given().when().get("/fulfillment/store/0").then().statusCode(400);
  }

  @Test
  @Order(12)
  void get_negative_byWarehouse_invalidId_returns400() {
    given().when().get("/fulfillment/warehouse/0").then().statusCode(400);
  }

  @Test
  @Order(13)
  void delete_negative_invalidId_returns400() {
    given().when().delete("/fulfillment/0").then().statusCode(400);
  }

  @Test
  @Order(14)
  void get_negative_byStore_unknownId_returns404() {
    given().when().get("/fulfillment/store/999999").then().statusCode(404);
  }

  @Test
  @Order(15)
  void get_negative_byWarehouse_unknownId_returns404() {
    given().when().get("/fulfillment/warehouse/999999").then().statusCode(404);
  }

  @Test
  @Order(16)
  void delete_negative_unknownId_returns404() {
    given().when().delete("/fulfillment/999999").then().statusCode(404);
  }

  private static Map<String, Object> body(long productId, long storeId, long warehouseId) {
    Map<String, Object> m = new HashMap<>();
    m.put("productId", productId);
    m.put("storeId", storeId);
    m.put("warehouseId", warehouseId);
    return m;
  }
}
