package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseNotFoundException;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseValidationException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArchiveWarehouseUseCase")
class ArchiveWarehouseUseCaseTest {

  @Mock private WarehouseStore warehouseStore;

  private ArchiveWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  @Nested
  @DisplayName("Positive cases")
  class PositiveCases {

    @Test
    @DisplayName("archives warehouse when found by id")
    void whenFoundById_archives() {
      Warehouse ref = new Warehouse();
      ref.id = 1L;
      Warehouse existing = new Warehouse();
      existing.id = 1L;
      existing.businessUnitCode = "MWH.001";
      existing.archivedAt = null;
      when(warehouseStore.getById(1L)).thenReturn(existing);

      assertDoesNotThrow(() -> useCase.archive(ref));
      verify(warehouseStore).update(existing);
    }

    @Test
    @DisplayName("archives warehouse when found by business unit code")
    void whenFoundByCode_archives() {
      Warehouse ref = new Warehouse();
      ref.businessUnitCode = "MWH.001";
      Warehouse existing = new Warehouse();
      existing.businessUnitCode = "MWH.001";
      existing.archivedAt = null;
      when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

      assertDoesNotThrow(() -> useCase.archive(ref));
      verify(warehouseStore).update(existing);
    }

    @Test
    @DisplayName("idempotent when already archived")
    void whenAlreadyArchived_doesNothing() {
      Warehouse ref = new Warehouse();
      ref.id = 1L;
      Warehouse existing = new Warehouse();
      existing.id = 1L;
      existing.archivedAt = java.time.LocalDateTime.now();
      when(warehouseStore.getById(1L)).thenReturn(existing);

      assertDoesNotThrow(() -> useCase.archive(ref));
      verify(warehouseStore, never()).update(any());
    }
  }

  @Nested
  @DisplayName("Negative / error cases")
  class NegativeAndErrorCases {

    @Test
    @DisplayName("throws when warehouse reference is null")
    void whenNull_throws() {
      assertThrows(WarehouseValidationException.class, () -> useCase.archive(null));
      verify(warehouseStore, never()).update(any());
    }

    @Test
    @DisplayName("throws when both id and business unit code are missing")
    void whenNoIdOrCode_throws() {
      Warehouse ref = new Warehouse();
      assertThrows(WarehouseValidationException.class, () -> useCase.archive(ref));
      verify(warehouseStore, never()).update(any());
    }

    @Test
    @DisplayName("throws when warehouse not found by id")
    void whenNotFoundById_throws() {
      Warehouse ref = new Warehouse();
      ref.id = 999L;
      when(warehouseStore.getById(999L)).thenReturn(null);
      assertThrows(WarehouseNotFoundException.class, () -> useCase.archive(ref));
      verify(warehouseStore, never()).update(any());
    }

    @Test
    @DisplayName("throws when warehouse not found by business unit code")
    void whenNotFoundByCode_throws() {
      Warehouse ref = new Warehouse();
      ref.businessUnitCode = "MWH.UNKNOWN";
      when(warehouseStore.findByBusinessUnitCode("MWH.UNKNOWN")).thenReturn(null);
      assertThrows(WarehouseNotFoundException.class, () -> useCase.archive(ref));
      verify(warehouseStore, never()).update(any());
    }
  }
}
