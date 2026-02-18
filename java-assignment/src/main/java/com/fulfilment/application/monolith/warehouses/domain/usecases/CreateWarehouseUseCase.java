package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    if (warehouse == null) {
      throw new WarehouseValidationException("Warehouse must not be null");
    }

    // Required fields: business unit code and location
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new WarehouseValidationException("Business unit code is required");
    }
    if (warehouse.location == null || warehouse.location.isBlank()) {
      throw new WarehouseValidationException("Location is required");
    }

    // Capacity and stock: required, non-negative, stock <= capacity
    if (warehouse.capacity == null) {
      throw new WarehouseValidationException("Capacity is required");
    }
    if (warehouse.stock == null) {
      throw new WarehouseValidationException("Stock is required");
    }
    int capacity = warehouse.capacity;
    int stock = warehouse.stock;
    if (capacity < 0) {
      throw new WarehouseValidationException("Capacity cannot be negative");
    }
    if (stock < 0) {
      throw new WarehouseValidationException("Stock cannot be negative");
    }
    if (stock > capacity) {
      throw new WarehouseValidationException(
          "Stock (" + stock + ") cannot exceed capacity (" + capacity + ")");
    }

    // Business unit code must not already exist
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new WarehouseValidationException(
          "Business unit code already exists: " + warehouse.businessUnitCode);
    }

    // Location must be valid
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new WarehouseValidationException("Invalid or unknown location: " + warehouse.location);
    }

    // Creation feasibility: max number of warehouses at location not reached
    long countAtLocation = warehouseStore.countActiveByLocation(warehouse.location);
    if (countAtLocation >= location.maxNumberOfWarehouses) {
      throw new WarehouseValidationException(
          "Maximum number of warehouses ("
              + location.maxNumberOfWarehouses
              + ") already reached at location: "
              + warehouse.location);
    }

    // Total capacity at location must not exceed location max
    int currentCapacityAtLocation = warehouseStore.sumCapacityByLocation(warehouse.location);
    if (currentCapacityAtLocation + capacity > location.maxCapacity) {
      throw new WarehouseValidationException(
          "Total capacity at location would exceed maximum ("
              + location.maxCapacity
              + ") for location: "
              + warehouse.location);
    }

    warehouseStore.create(warehouse);
  }
}
