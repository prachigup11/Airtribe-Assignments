# Smart Parking Lot System

Java 17 low-level design for a multi-floor smart parking lot. The demo covers vehicle check-in/check-out, size-aware spot allocation, real-time availability, duration-based fees, and concurrent requests.

## Design

- `ParkingLotService` owns the allocation and transaction lifecycle.
- `ParkingSpot` enforces capacity compatibility and occupancy state.
- The allocator chooses the smallest available compatible spot, preserving large spots for buses.
- `FeeCalculator` charges per started hour: motorcycle `10.00`, car `20.00`, bus `40.00`.
- Service methods are synchronized, making the check-then-occupy and close-then-release operations atomic in this in-memory implementation.
- `Clock` is injected so fee and timestamp behavior can be tested with a fixed clock.

## Data model

```text
parking_spot(
  spot_id primary key,
  floor_number int not null,
  spot_type varchar not null,
  occupied boolean not null
)

vehicle(
  license_plate primary key,
  vehicle_type varchar not null
)

parking_transaction(
  ticket_id primary key,
  license_plate foreign key,
  spot_id foreign key,
  entry_time timestamp not null,
  exit_time timestamp null,
  fee decimal(10,2) null
)
```

In production, replace the in-memory maps with a repository and use a database transaction or row-level lock around spot reservation. A unique active-ticket constraint on `license_plate` and an atomic update such as `occupied = false -> true` preserve the same invariants across multiple application instances.

## Run

```bash
mvn compile
mvn exec:java
```