package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseResourceQuarkusTest {

  private static final String WAREHOUSE = "warehouse";

  @Test
  void listAll_returns200_andInitialWarehouses() {
    given()
        .when()
        .get(WAREHOUSE)
        .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(3))
        .body("businessUnitCode", hasItems("MWH.001", "MWH.012", "MWH.023"));
  }

  @Test
  void create_validBody_returns201() {
    // EINDHOVEN-001 has no warehouses in initial data, maxCapacity 70
    var body =
        Map.of(
            "businessUnitCode", "MWH.NEW",
            "location", "EINDHOVEN-001",
            "capacity", 25,
            "stock", 5);

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post(WAREHOUSE)
        .then()
        .statusCode(anyOf(equalTo(200), equalTo(201)))
        .body("businessUnitCode", equalTo("MWH.NEW"))
        .body("location", equalTo("EINDHOVEN-001"))
        .body("capacity", equalTo(25))
        .body("stock", equalTo(5))
        .body("id", notNullValue());
  }

  @Test
  void create_nullBody_returns400() {
    given()
        .contentType(ContentType.JSON)
        .body("")
        .when()
        .post(WAREHOUSE)
        .then()
        .statusCode(400);
  }

  @Test
  void getById_validId_returns200() {
    given()
        .when()
        .get(WAREHOUSE + "/1")
        .then()
        .statusCode(200)
        .body("id", equalTo("1"))
        .body("businessUnitCode", notNullValue());
  }

  @Test
  void getById_unknownId_returns404() {
    given()
        .when()
        .get(WAREHOUSE + "/99999")
        .then()
        .statusCode(404);
  }

  @Test
  void getById_invalidId_returns400() {
    given()
        .when()
        .get(WAREHOUSE + "/abc")
        .then()
        .statusCode(400);
  }

  @Test
  void archive_validId_returns204() {
    // Create at EINDHOVEN-001 (no initial warehouses), then archive it
    var body =
        Map.of(
            "businessUnitCode", "MWH.TOARCHIVE",
            "location", "EINDHOVEN-001",
            "capacity", 10,
            "stock", 0);
    var created =
        given()
            .contentType(ContentType.JSON)
            .body(body)
            .when()
            .post(WAREHOUSE)
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(201)))
            .extract()
            .path("id");

    given()
        .when()
        .delete(WAREHOUSE + "/" + created)
        .then()
        .statusCode(204);
  }

  @Test
  void archive_invalidId_returns400() {
    given()
        .when()
        .delete(WAREHOUSE + "/abc")
        .then()
        .statusCode(400);
  }

  @Test
  void replace_validRequest_returns200() {
    // MWH.001 has stock 10; replace at AMSTERDAM-001 (maxCapacity 100, current 50) so new capacity <= 50
    // New stock must match current stock (10)
    var body =
        Map.of(
            "businessUnitCode", "MWH.001",
            "location", "AMSTERDAM-001",
            "capacity", 50,
            "stock", 10);

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post(WAREHOUSE + "/MWH.001/replacement")
        .then()
        .statusCode(200)
        .body("businessUnitCode", equalTo("MWH.001"))
        .body("capacity", equalTo(50))
        .body("stock", equalTo(10));
  }

  @Test
  void replace_blankBusinessUnitCode_returns400() {
    var body =
        Map.of(
            "businessUnitCode", "X",
            "location", "ZWOLLE-001",
            "capacity", 10,
            "stock", 0);

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post(WAREHOUSE + "/ /replacement")
        .then()
        .statusCode(400);
  }

  @Test
  void replace_nullBody_returns400() {
    given()
        .contentType(ContentType.JSON)
        .body("")
        .when()
        .post(WAREHOUSE + "/MWH.012/replacement")
        .then()
        .statusCode(400);
  }
}
