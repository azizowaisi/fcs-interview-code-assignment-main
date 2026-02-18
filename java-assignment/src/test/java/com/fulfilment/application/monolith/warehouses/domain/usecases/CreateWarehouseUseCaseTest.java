package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateWarehouseUseCase")
class CreateWarehouseUseCaseTest {

  @Mock private WarehouseStore warehouseStore;
  @Mock private LocationResolver locationResolver;

  private CreateWarehouseUseCase useCase;
  private static final Location VALID_LOCATION = new Location("ZWOLLE-001", 2, 80);

  @BeforeEach
  void setUp() {
    useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
  }

  private Warehouse validWarehouse() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "MWH.NEW";
    w.location = "ZWOLLE-001";
    w.capacity = 30;
    w.stock = 10;
    return w;
  }

  @Nested
  @DisplayName("Positive cases")
  class PositiveCases {

    @Test
    @DisplayName("creates warehouse when all validations pass")
    void whenValid_createsWarehouse() {
      Warehouse w = validWarehouse();
      when(warehouseStore.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
      when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(VALID_LOCATION);
      when(warehouseStore.countActiveByLocation("ZWOLLE-001")).thenReturn(0L);
      when(warehouseStore.sumCapacityByLocation("ZWOLLE-001")).thenReturn(0);

      assertDoesNotThrow(() -> useCase.create(w));
      verify(warehouseStore).create(w);
    }
  }

  @Nested
  @DisplayName("Negative / error - validation failures")
  class ValidationFailures {

    @Test
    @DisplayName("throws when warehouse is null")
    void whenNull_throws() {
      assertThrows(WarehouseValidationException.class, () -> useCase.create(null));
      verify(warehouseStore, never()).create(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("throws when business unit code is blank")
    void whenBlankBusinessUnitCode_throws() {
      Warehouse w = validWarehouse();
      w.businessUnitCode = "  ";
      assertThrows(WarehouseValidationException.class, () -> useCase.create(w));
      verify(warehouseStore, never()).create(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("throws when location is blank")
    void whenBlankLocation_throws() {
      Warehouse w = validWarehouse();
      w.location = "";
      assertThrows(WarehouseValidationException.class, () -> useCase.create(w));
      verify(warehouseStore, never()).create(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("throws when capacity is null")
    void whenCapacityNull_throws() {
      Warehouse w = validWarehouse();
      w.capacity = null;
      assertThrows(WarehouseValidationException.class, () -> useCase.create(w));
    }

    @Test
    @DisplayName("throws when stock is null")
    void whenStockNull_throws() {
      Warehouse w = validWarehouse();
      w.stock = null;
      assertThrows(WarehouseValidationException.class, () -> useCase.create(w));
    }

    @Test
    @DisplayName("throws when capacity is negative")
    void whenCapacityNegative_throws() {
      Warehouse w = validWarehouse();
      w.capacity = -1;
      assertThrows(WarehouseValidationException.class, () -> useCase.create(w));
    }

    @Test
    @DisplayName("throws when stock exceeds capacity")
    void whenStockExceedsCapacity_throws() {
      Warehouse w = validWarehouse();
      w.stock = 50;
      w.capacity = 30;
      assertThrows(WarehouseValidationException.class, () -> useCase.create(w));
    }

    @Test
    @DisplayName("throws when business unit code already exists")
    void whenBusinessUnitCodeExists_throws() {
      Warehouse w = validWarehouse();
      when(warehouseStore.findByBusinessUnitCode("MWH.NEW")).thenReturn(new Warehouse());
      assertThrows(WarehouseValidationException.class, () -> useCase.create(w));
      verify(warehouseStore, never()).create(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("throws when location is invalid")
    void whenLocationInvalid_throws() {
      Warehouse w = validWarehouse();
      when(warehouseStore.findByBusinessUnitCode(anyString())).thenReturn(null);
      when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(null);
      assertThrows(WarehouseValidationException.class, () -> useCase.create(w));
      verify(warehouseStore, never()).create(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("throws when max warehouses at location reached")
    void whenMaxWarehousesReached_throws() {
      Warehouse w = validWarehouse();
      when(warehouseStore.findByBusinessUnitCode(anyString())).thenReturn(null);
      when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(VALID_LOCATION);
      when(warehouseStore.countActiveByLocation("ZWOLLE-001")).thenReturn(2L);
      assertThrows(WarehouseValidationException.class, () -> useCase.create(w));
      verify(warehouseStore, never()).create(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("throws when total capacity at location would be exceeded")
    void whenCapacityExceeded_throws() {
      Warehouse w = validWarehouse();
      w.capacity = 90;
      when(warehouseStore.findByBusinessUnitCode(anyString())).thenReturn(null);
      when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(VALID_LOCATION);
      when(warehouseStore.countActiveByLocation("ZWOLLE-001")).thenReturn(0L);
      when(warehouseStore.sumCapacityByLocation("ZWOLLE-001")).thenReturn(0);
      assertThrows(WarehouseValidationException.class, () -> useCase.create(w));
      verify(warehouseStore, never()).create(org.mockito.ArgumentMatchers.any());
    }
  }
}
