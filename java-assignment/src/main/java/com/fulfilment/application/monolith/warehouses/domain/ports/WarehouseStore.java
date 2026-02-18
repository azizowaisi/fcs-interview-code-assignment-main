package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.util.List;

public interface WarehouseStore {

  List<Warehouse> getAll();

  void create(Warehouse warehouse);

  void update(Warehouse warehouse);

  void remove(Warehouse warehouse);

  /** Returns active (non-archived) warehouse with the given business unit code, or null. */
  Warehouse findByBusinessUnitCode(String buCode);

  Warehouse findById(Long id);

  /** Count of active warehouses at the given location identifier. */
  long countActiveByLocation(String locationIdentifier);

  /** Sum of capacity of active warehouses at the given location. */
  int sumCapacityByLocation(String locationIdentifier);
}
