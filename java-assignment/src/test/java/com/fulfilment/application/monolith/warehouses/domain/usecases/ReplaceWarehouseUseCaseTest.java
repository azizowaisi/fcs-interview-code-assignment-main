package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseNotFoundException;
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
@DisplayName("ReplaceWarehouseUseCase")
class ReplaceWarehouseUseCaseTest {

  @Mock private WarehouseStore warehouseStore;
  @Mock private LocationResolver locationResolver;

  private ReplaceWarehouseUseCase useCase;
  private static final Location VALID_LOCATION = new Location("AMSTERDAM-001", 5, 100);

  @BeforeEach
  void setUp() {
    useCase = new ReplaceWarehouseUseCase(warehouseStore, locationResolver);
  }

  private Warehouse currentWarehouse() {
    Warehouse w = new Warehouse();
    w.id = 1L;
    w.businessUnitCode = "MWH.001";
    w.location = "AMSTERDAM-001";
    w.capacity = 50;
    w.stock = 20;
    return w;
  }

  private Warehouse newWarehouse() {
    Warehouse w = new Warehouse();
    w.businessUnitCode = "MWH.001";
    w.location = "AMSTERDAM-001";
    w.capacity = 60;
    w.stock = 20;
    return w;
  }

  @Nested
  @DisplayName("Positive cases")
  class PositiveCases {

    @Test
    @DisplayName("replaces warehouse when validations pass")
    void whenValid_replacesWarehouse() {
      Warehouse current = currentWarehouse();
      Warehouse newWh = newWarehouse();
      when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(current);
      when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(VALID_LOCATION);
      when(warehouseStore.sumCapacityByLocation("AMSTERDAM-001")).thenReturn(50);

      assertDoesNotThrow(() -> useCase.replace(newWh));
      verify(warehouseStore).update(any(Warehouse.class));
      verify(warehouseStore).create(newWh);
    }
  }

  @Nested
  @DisplayName("Negative / error cases")
  class NegativeAndErrorCases {

    @Test
    @DisplayName("throws when new warehouse is null")
    void whenNull_throws() {
      assertThrows(WarehouseValidationException.class, () -> useCase.replace(null));
    }

    @Test
    @DisplayName("throws when business unit code is blank")
    void whenBlankBusinessUnitCode_throws() {
      Warehouse w = newWarehouse();
      w.businessUnitCode = "";
      assertThrows(WarehouseValidationException.class, () -> useCase.replace(w));
    }

    @Test
    @DisplayName("throws when no active warehouse found for code")
    void whenNoActiveWarehouse_throwsNotFound() {
      Warehouse w = newWarehouse();
      when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(null);
      assertThrows(WarehouseNotFoundException.class, () -> useCase.replace(w));
    }

    @Test
    @DisplayName("throws when location is invalid")
    void whenInvalidLocation_throws() {
      Warehouse current = currentWarehouse();
      Warehouse w = newWarehouse();
      when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(current);
      when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(null);
      assertThrows(WarehouseValidationException.class, () -> useCase.replace(w));
    }

    @Test
    @DisplayName("throws when new capacity less than current stock")
    void whenNewCapacityInsufficient_throws() {
      Warehouse current = currentWarehouse();
      current.stock = 70;
      Warehouse w = newWarehouse();
      w.capacity = 50;
      w.stock = 70;
      when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(current);
      when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(VALID_LOCATION);
      // use case throws before calling sumCapacityByLocation
      assertThrows(WarehouseValidationException.class, () -> useCase.replace(w));
    }

    @Test
    @DisplayName("throws when new stock does not match current stock")
    void whenStockMismatch_throws() {
      Warehouse current = currentWarehouse();
      Warehouse w = newWarehouse();
      w.stock = 15;
      when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(current);
      when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(VALID_LOCATION);
      // use case throws before calling sumCapacityByLocation
      assertThrows(WarehouseValidationException.class, () -> useCase.replace(w));
    }
  }
}
