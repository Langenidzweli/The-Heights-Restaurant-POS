package backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import backend.models.Waiter;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/waiters")
@CrossOrigin(origins = "*")
public class WaiterController {

    // Get basic waiter statistics
    @GetMapping
    public ResponseEntity<List<Waiter.WaiterStats>> getWaiterStats() {
        try {
            List<Waiter.WaiterStats> stats = Waiter.getWaiterStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Get detailed waiter information
    @GetMapping("/detailed")
    public ResponseEntity<List<Map<String, Object>>> getWaitersDetailed() {
        try {
            List<Map<String, Object>> waitersData = Waiter.getWaitersDetailed();
            return ResponseEntity.ok(waitersData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(Map.of("error", "Failed to load waiter details")));
        }
    }

    // Check dine-in waiter availability
    @GetMapping("/availability/check-dinein")
    public ResponseEntity<Map<String, Object>> checkDineInAvailability() {
        try {
            boolean hasAvailableWaiters = Waiter.hasAvailableWaitersForDineIn();
            Map<String, Object> response = new HashMap<>();
            response.put("available", hasAvailableWaiters);
            response.put("message", hasAvailableWaiters ? "Waiters available for dine-in"
                    : "Sorry, restaurant fully booked - no available waiters");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("available", false, "message", "Error checking availability"));
        }
    }
}