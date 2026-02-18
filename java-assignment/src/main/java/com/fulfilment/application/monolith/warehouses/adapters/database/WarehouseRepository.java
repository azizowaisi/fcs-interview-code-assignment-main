package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @PersistenceContext
  EntityManager entityManager;

  @Override
  public List<Warehouse> getAll() {
    return find("archivedAt is null").list().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse must not be null");
    }
    DbWarehouse db = fromDomain(warehouse);
    db.createdAt = LocalDateTime.now();
    db.archivedAt = null;
    persist(db);
    warehouse.id = db.id;
  }

  @Override
  public void update(Warehouse warehouse) {
    if (warehouse == null) {
      return;
    }
    DbWarehouse db =
        warehouse.id != null
            ? entityManager.find(DbWarehouse.class, warehouse.id)
            : (warehouse.businessUnitCode != null
                ? find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
                    .firstResult()
                : null);
    if (db == null) {
      return;
    }
    db.businessUnitCode = warehouse.businessUnitCode;
    db.location = warehouse.location;
    db.capacity = warehouse.capacity;
    db.stock = warehouse.stock;
    if (warehouse.archivedAt != null) {
      db.archivedAt = warehouse.archivedAt;
    }
    persist(db);
  }

  @Override
  public void remove(Warehouse warehouse) {
    if (warehouse == null) {
      return;
    }
    if (warehouse.id != null) {
      deleteById(warehouse.id);
    } else if (warehouse.businessUnitCode != null && !warehouse.businessUnitCode.isBlank()) {
      DbWarehouse db =
          find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
              .firstResult();
      if (db != null) {
        delete(db);
      }
    }
    // If both id and businessUnitCode are null/blank, no-op
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    if (buCode == null || buCode.isBlank()) {
      return null;
    }
    DbWarehouse db = find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
    return db != null ? db.toWarehouse() : null;
  }

  @Override
  public Warehouse getById(Long id) {
    if (id == null) {
      return null;
    }
    DbWarehouse db = entityManager.find(DbWarehouse.class, id);
    return db != null ? db.toWarehouse() : null;
  }

  @Override
  public long countActiveByLocation(String locationIdentifier) {
    if (locationIdentifier == null || locationIdentifier.isBlank()) {
      return 0;
    }
    return count("location = ?1 and archivedAt is null", locationIdentifier);
  }

  @Override
  public int sumCapacityByLocation(String locationIdentifier) {
    if (locationIdentifier == null || locationIdentifier.isBlank()) {
      return 0;
    }
    List<DbWarehouse> list = find("location = ?1 and archivedAt is null", locationIdentifier).list();
    return list.stream().mapToInt(w -> w.capacity != null ? w.capacity : 0).sum();
  }

  private static DbWarehouse fromDomain(Warehouse warehouse) {
    DbWarehouse db = new DbWarehouse();
    db.id = warehouse.id;
    db.businessUnitCode = warehouse.businessUnitCode;
    db.location = warehouse.location;
    db.capacity = warehouse.capacity;
    db.stock = warehouse.stock;
    db.createdAt = warehouse.createdAt;
    db.archivedAt = warehouse.archivedAt;
    return db;
  }
}
