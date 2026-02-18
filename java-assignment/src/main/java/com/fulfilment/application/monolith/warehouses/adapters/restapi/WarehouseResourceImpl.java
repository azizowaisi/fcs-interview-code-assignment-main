package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.jboss.logging.Logger;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  private static final Logger LOG = Logger.getLogger(WarehouseResourceImpl.class);

  @Inject private WarehouseRepository warehouseRepository;
  @Inject private CreateWarehouseOperation createWarehouseOperation;
  @Inject private ReplaceWarehouseOperation replaceWarehouseOperation;
  @Inject private ArchiveWarehouseOperation archiveWarehouseOperation;

  @Override
  public List<com.warehouse.api.beans.Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  public com.warehouse.api.beans.Warehouse createANewWarehouseUnit(
      @NotNull com.warehouse.api.beans.Warehouse data) {
    if (data == null) {
      throw new WebApplicationException(
          Response.status(400).entity("Request body is required").build());
    }
    Warehouse domain = toDomain(data);
    try {
      createWarehouseOperation.create(domain);
    } catch (WarehouseValidationException e) {
      LOG.warnf("Warehouse create validation failed: %s", e.getMessage());
      throw new WebApplicationException(
          Response.status(400).entity(messageOrDefault(e, "Validation failed")).build());
    }
    return toWarehouseResponse(domain);
  }

  @Override
  public com.warehouse.api.beans.Warehouse getAWarehouseUnitByID(String id) {
    if (id == null || id.isBlank()) {
      throw new WebApplicationException(
          Response.status(400).entity("Warehouse id is required").build());
    }
    Long idLong = parseId(id);
    Warehouse domain = warehouseRepository.getById(idLong);
    if (domain == null) {
      LOG.debugf("Warehouse not found for id: %s", id);
      throw new WebApplicationException(
          Response.status(404).entity("Warehouse not found: " + id).build());
    }
    return toWarehouseResponse(domain);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    if (id == null || id.isBlank()) {
      throw new WebApplicationException(
          Response.status(400).entity("Warehouse id is required").build());
    }
    Long idLong = parseId(id);
    Warehouse domain = new Warehouse();
    domain.id = idLong;
    try {
      archiveWarehouseOperation.archive(domain);
    } catch (WarehouseNotFoundException e) {
      LOG.warnf("Archive failed: %s", e.getMessage());
      throw new WebApplicationException(
          Response.status(404).entity(messageOrDefault(e, "Warehouse not found")).build());
    } catch (WarehouseValidationException e) {
      LOG.warnf("Archive validation failed: %s", e.getMessage());
      throw new WebApplicationException(
          Response.status(400).entity(messageOrDefault(e, "Validation failed")).build());
    }
  }

  @Override
  public com.warehouse.api.beans.Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull com.warehouse.api.beans.Warehouse data) {
    if (businessUnitCode == null || businessUnitCode.isBlank()) {
      throw new WebApplicationException(
          Response.status(400).entity("Business unit code is required").build());
    }
    if (data == null) {
      throw new WebApplicationException(
          Response.status(400).entity("Request body is required").build());
    }
    Warehouse domain = toDomain(data);
    domain.businessUnitCode = businessUnitCode;
    try {
      replaceWarehouseOperation.replace(domain);
    } catch (WarehouseNotFoundException e) {
      LOG.warnf("Replace failed (not found): %s", e.getMessage());
      throw new WebApplicationException(
          Response.status(404).entity(messageOrDefault(e, "Warehouse not found")).build());
    } catch (WarehouseValidationException e) {
      LOG.warnf("Replace validation failed: %s", e.getMessage());
      throw new WebApplicationException(
          Response.status(400).entity(messageOrDefault(e, "Validation failed")).build());
    }
    Warehouse created = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
    if (created == null) {
      LOG.errorf("Warehouse created but not found by code: %s", businessUnitCode);
      throw new WebApplicationException(
          Response.status(500)
              .entity("Warehouse was created but could not be retrieved")
              .build());
    }
    return toWarehouseResponse(created);
  }

  private static String messageOrDefault(Exception e, String defaultMessage) {
    return (e.getMessage() != null && !e.getMessage().isBlank()) ? e.getMessage() : defaultMessage;
  }

  private Warehouse toDomain(com.warehouse.api.beans.Warehouse data) {
    var domain = new Warehouse();
    domain.businessUnitCode = data.getBusinessUnitCode();
    domain.location = data.getLocation();
    domain.capacity = data.getCapacity();
    domain.stock = data.getStock();
    return domain;
  }

  private com.warehouse.api.beans.Warehouse toWarehouseResponse(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse must not be null");
    }
    var response = new com.warehouse.api.beans.Warehouse();
    if (warehouse.id != null) {
      response.setId(String.valueOf(warehouse.id));
    }
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);
    return response;
  }

  private static Long parseId(String id) {
    if (id == null || id.isBlank()) {
      throw new WebApplicationException(
          Response.status(400).entity("Warehouse id is required").build());
    }
    try {
      return Long.valueOf(id.trim());
    } catch (NumberFormatException e) {
      throw new WebApplicationException(
          Response.status(400).entity("Invalid warehouse id: " + id).build());
    }
  }
}
