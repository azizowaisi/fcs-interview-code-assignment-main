package com.fulfilment.application.monolith.warehouses.domain;

/** Thrown when warehouse creation or replacement fails validation. Maps to 400 Bad Request. */
public class WarehouseValidationException extends RuntimeException {

  public WarehouseValidationException(String message) {
    super(message);
  }
}
