package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(
      WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    if (newWarehouse == null) {
      throw new WarehouseValidationException("New warehouse must not be null");
    }
    if (newWarehouse.businessUnitCode == null || newWarehouse.businessUnitCode.isBlank()) {
      throw new WarehouseValidationException("Business unit code is required");
    }
    if (newWarehouse.location == null || newWarehouse.location.isBlank()) {
      throw new WarehouseValidationException("Location is required");
    }
    if (newWarehouse.capacity == null || newWarehouse.stock == null) {
      throw new WarehouseValidationException("Capacity and stock are required");
    }
    int newCapacity = newWarehouse.capacity;
    int newStock = newWarehouse.stock;
    if (newCapacity < 0 || newStock < 0) {
      throw new WarehouseValidationException("Capacity and stock cannot be negative");
    }

    Warehouse current =
        warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (current == null) {
      throw new WarehouseNotFoundException(
          "No active warehouse found for business unit code: " + newWarehouse.businessUnitCode);
    }

    // Location must be valid
    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      throw new WarehouseValidationException(
          "Invalid or unknown location: " + newWarehouse.location);
    }

    int currentStock = current.stock != null ? current.stock : 0;
    int currentCapacity = current.capacity != null ? current.capacity : 0;

    // New capacity must accommodate current stock
    if (newCapacity < currentStock) {
      throw new WarehouseValidationException(
          "New warehouse capacity ("
              + newCapacity
              + ") cannot accommodate current stock ("
              + currentStock
              + ")");
    }

    // Stock of new warehouse must match stock of previous warehouse
    if (newStock != currentStock) {
      throw new WarehouseValidationException(
          "New warehouse stock ("
              + newStock
              + ") must match previous warehouse stock ("
              + currentStock
              + ")");
    }

    // After replace: total capacity at location = current total - current capacity + new capacity
    int currentTotalAtLocation = warehouseStore.sumCapacityByLocation(newWarehouse.location);
    int totalAfterReplace = currentTotalAtLocation - currentCapacity + newCapacity;
    if (totalAfterReplace > location.maxCapacity) {
      throw new WarehouseValidationException(
          "Total capacity at location after replace would exceed maximum ("
              + location.maxCapacity
              + ") for location: "
              + newWarehouse.location);
    }

    // Archive current warehouse
    current.archivedAt = LocalDateTime.now();
    warehouseStore.update(current);

    // Create new warehouse with same business unit code
    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}
