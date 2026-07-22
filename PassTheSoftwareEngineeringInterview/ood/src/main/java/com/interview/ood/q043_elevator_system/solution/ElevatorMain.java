package com.interview.ood.q043_elevator_system.solution;

// Runnable demo — not a test. Walks the same "user journey" a live interviewer would want
// talked through: request → arrive → attempt an invalid transition → a second request
// queues while busy → closing the door auto-dispatches it — one continuous story, then a
// side-by-side comparison showing FCFS and SCAN genuinely disagree on the same input.
public class ElevatorMain {

    public static void main(String[] args) {
        System.out.println("=== Happy path: request, arrive, door opens ===");
        Elevator elevator = new Elevator("E1", 0);
        ElevatorService service = new ElevatorService(elevator, new FcfsSchedulingStrategy());
        service.requestFloor(5, Direction.UP);
        System.out.println("Elevator at floor " + elevator.getCurrentFloor() + ", status: " + elevator.getStatus());

        System.out.println();
        System.out.println("=== Invalid transition: moving while the door is open ===");
        try {
            elevator.move();
        } catch (IllegalStateException e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== A second request queues while busy, dispatches on door close ===");
        service.requestFloor(2, Direction.DOWN);
        System.out.println("Pending while door still open: " + service.getPendingRequests().size());
        service.closeDoor();
        System.out.println("After closeDoor() — elevator at floor " + elevator.getCurrentFloor()
                + ", status: " + elevator.getStatus() + ", pending: " + service.getPendingRequests().size());

        System.out.println();
        System.out.println("=== FCFS vs SCAN: same pending requests, different next floor ===");
        Elevator fcfsElevator = new Elevator("E2", 0);
        ElevatorService fcfsService = new ElevatorService(fcfsElevator, new FcfsSchedulingStrategy());
        Elevator scanElevator = new Elevator("E3", 0);
        ElevatorService scanService = new ElevatorService(scanElevator, new ScanSchedulingStrategy());

        // First request on each drains immediately (both start IDLE) — this just gets both
        // elevators to floor 0 with the door open, so the next two requests genuinely queue.
        fcfsService.requestFloor(0, Direction.UP);
        scanService.requestFloor(0, Direction.UP);

        // Queued in this order on purpose: 18 first, 3 second — FCFS should pick 18
        // (first added), SCAN should pick 3 (nearest to current floor 0).
        fcfsService.requestFloor(18, Direction.UP);
        fcfsService.requestFloor(3, Direction.UP);
        scanService.requestFloor(18, Direction.UP);
        scanService.requestFloor(3, Direction.UP);

        fcfsService.closeDoor();
        scanService.closeDoor();

        System.out.println("FCFS dispatched to floor " + fcfsElevator.getCurrentFloor() + " (expected 18 — first added)");
        System.out.println("SCAN dispatched to floor " + scanElevator.getCurrentFloor() + " (expected 3 — nearest to 0)");
    }
}
