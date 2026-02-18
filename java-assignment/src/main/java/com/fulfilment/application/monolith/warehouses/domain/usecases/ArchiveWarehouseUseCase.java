package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void archive(Warehouse warehouse) {
    if (warehouse == null) {
      throw new WarehouseValidationException("Warehouse reference must not be null");
    }
    if (warehouse.id == null
        && (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank())) {
      throw new WarehouseValidationException(
          "Either warehouse id or business unit code must be provided");
    }

    Warehouse toArchive =
        warehouse.id != null
            ? warehouseStore.getById(warehouse.id)
            : warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (toArchive == null) {
      String identifier =
          warehouse.id != null ? "id " + warehouse.id : "code " + warehouse.businessUnitCode;
      throw new WarehouseNotFoundException("Warehouse not found: " + identifier);
    }
    if (toArchive.archivedAt != null) {
      return; // already archived, idempotent
    }
    toArchive.archivedAt = LocalDateTime.now();
    warehouseStore.update(toArchive);
  }
}
