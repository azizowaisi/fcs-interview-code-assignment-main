package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("LocationGateway")
class LocationGatewayTest {

  private LocationGateway locationGateway;

  @BeforeEach
  void setUp() {
    locationGateway = new LocationGateway();
  }

  @Nested
  @DisplayName("resolveByIdentifier - positive")
  class PositiveCases {

    @Test
    @DisplayName("returns location when identifier exists")
    void whenExistingIdentifier_returnsLocation() {
      Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");
      assertEquals("ZWOLLE-001", location.identification);
      assertEquals(1, location.maxNumberOfWarehouses);
      assertEquals(40, location.maxCapacity);
    }

    @Test
    @DisplayName("returns different location for AMSTERDAM-001")
    void whenAmsterdam001_returnsCorrectLocation() {
      Location location = locationGateway.resolveByIdentifier("AMSTERDAM-001");
      assertEquals("AMSTERDAM-001", location.identification);
      assertEquals(5, location.maxNumberOfWarehouses);
      assertEquals(100, location.maxCapacity);
    }
  }

  @Nested
  @DisplayName("resolveByIdentifier - negative / error")
  class NegativeAndErrorCases {

    @Test
    @DisplayName("returns null when identifier is unknown")
    void whenUnknownIdentifier_returnsNull() {
      assertNull(locationGateway.resolveByIdentifier("UNKNOWN-999"));
    }

    @Test
    @DisplayName("returns null when identifier is null")
    void whenNullIdentifier_returnsNull() {
      assertNull(locationGateway.resolveByIdentifier(null));
    }

    @Test
    @DisplayName("returns null when identifier is blank")
    void whenBlankIdentifier_returnsNull() {
      assertNull(locationGateway.resolveByIdentifier("   "));
    }
  }
}
