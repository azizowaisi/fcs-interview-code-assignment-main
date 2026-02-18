package com.fulfilment.application.monolith.warehouses.domain;

/** Thrown when a warehouse is not found. Maps to 404 Not Found. */
public class WarehouseNotFoundException extends RuntimeException {

  public WarehouseNotFoundException(String message) {
    super(message);
  }
}
