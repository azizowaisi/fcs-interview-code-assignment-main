package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreResourceQuarkusTest {

  private static final String STORE = "store";

  @Test
  void get_returns200_andInitialStores() {
    given()
        .when()
        .get(STORE)
        .then()
        .statusCode(200)
        .body("size()", equalTo(3))
        .body("name", hasItems("TONSTAD", "KALLAX", "BESTÅ"));
  }

  @Test
  void getSingle_validId_returns200() {
    given()
        .when()
        .get(STORE + "/1")
        .then()
        .statusCode(200)
        .body("id", equalTo(1))
        .body("name", equalTo("TONSTAD"))
        .body("quantityProductsInStock", equalTo(10));
  }

  @Test
  void getSingle_unknownId_returns404() {
    given()
        .when()
        .get(STORE + "/99999")
        .then()
        .statusCode(404);
  }

  @Test
  void create_validStore_returns201() {
    String json = "{\"name\":\"NEWSTORE\",\"quantityProductsInStock\":7}";

    given()
        .contentType(ContentType.JSON)
        .body(json)
        .when()
        .post(STORE)
        .then()
        .statusCode(201)
        .body("name", equalTo("NEWSTORE"))
        .body("quantityProductsInStock", equalTo(7))
        .body("id", notNullValue());
  }

  @Test
  void create_withIdSet_returns422() {
    String json = "{\"id\":1,\"name\":\"X\",\"quantityProductsInStock\":0}";

    given()
        .contentType(ContentType.JSON)
        .body(json)
        .when()
        .post(STORE)
        .then()
        .statusCode(422);
  }

  @Test
  void update_validRequest_returns200() {
    String json = "{\"name\":\"TONSTAD\",\"quantityProductsInStock\":15}";

    given()
        .contentType(ContentType.JSON)
        .body(json)
        .when()
        .put(STORE + "/1")
        .then()
        .statusCode(200)
        .body("name", equalTo("TONSTAD"))
        .body("quantityProductsInStock", equalTo(15));
  }

  @Test
  void update_nameNull_returns422() {
    String json = "{\"quantityProductsInStock\":5}";

    given()
        .contentType(ContentType.JSON)
        .body(json)
        .when()
        .put(STORE + "/1")
        .then()
        .statusCode(422);
  }

  @Test
  void update_unknownId_returns404() {
    String json = "{\"name\":\"X\",\"quantityProductsInStock\":0}";

    given()
        .contentType(ContentType.JSON)
        .body(json)
        .when()
        .put(STORE + "/99999")
        .then()
        .statusCode(404);
  }

  @Test
  void patch_validRequest_returns200() {
    String json = "{\"name\":\"KALLAX\",\"quantityProductsInStock\":8}";

    given()
        .contentType(ContentType.JSON)
        .body(json)
        .when()
        .patch(STORE + "/2")
        .then()
        .statusCode(200)
        .body("quantityProductsInStock", equalTo(8));
  }

  @Test
  void patch_nameNull_returns422() {
    String json = "{\"quantityProductsInStock\":1}";

    given()
        .contentType(ContentType.JSON)
        .body(json)
        .when()
        .patch(STORE + "/1")
        .then()
        .statusCode(422);
  }

  @Test
  void delete_validId_returns204() {
    // Create then delete so we don't break initial data for other tests
    String json = "{\"name\":\"TODELETE\",\"quantityProductsInStock\":0}";
    Integer id =
        given()
            .contentType(ContentType.JSON)
            .body(json)
            .when()
            .post(STORE)
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    given()
        .when()
        .delete(STORE + "/" + id)
        .then()
        .statusCode(204);
  }

  @Test
  void delete_unknownId_returns404() {
    given()
        .when()
        .delete(STORE + "/99999")
        .then()
        .statusCode(404);
  }
}
