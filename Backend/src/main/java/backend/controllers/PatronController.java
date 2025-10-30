package backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import backend.models.Patron;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patrons")
@CrossOrigin(origins = "*")
public class PatronController {

    // REQUEST/RESPONSE CLASSES

    static class PatronRequest {
        private Integer groupSize;
        private int orderType;

        public Integer getGroupSize() {
            return groupSize;
        }

        public int getOrderType() {
            return orderType;
        }
    }

    static class PatronResponse {
        public int id;
        public int groupSize;
        public int orderType;
        public Integer tableNumber;
        public String waiterName;

        public PatronResponse(Patron p) {
            this.id = p.getId();
            this.groupSize = p.getGroupSize();
            this.orderType = p.getServiceType();
            this.tableNumber = p.getTableId() != 0 ? p.getTableId() : null;
            this.waiterName = p.getWaiter() != null ? p.getWaiter().getName() : null;
        }
    }

    // PATRON CREATION ENDPOINTS

    @PostMapping("/add")
    public ResponseEntity<?> addPatron(@RequestBody PatronRequest request) {
        try {
            Map<String, Object> result = Patron.createNewPatron(request.getOrderType(), request.getGroupSize());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // PATRON RETRIEVAL ENDPOINTS

    @GetMapping("/{id}")
    public ResponseEntity<PatronResponse> getPatronById(@PathVariable int id) {
        Patron patron = Patron.findById(id);
        return patron != null ? ResponseEntity.ok(new PatronResponse(patron)) : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<PatronResponse> getAllPatrons() {
        List<PatronResponse> allPatrons = new ArrayList<>();
        for (Patron patron : Patron.getAll()) {
            allPatrons.add(new PatronResponse(patron));
        }
        return allPatrons;
    }

    // QUEUE AND ORDER MANAGEMENT ENDPOINTS
    @GetMapping("/queue-counts")
    public ResponseEntity<Map<String, Integer>> getQueueCounts() {
        try {
            Map<String, Integer> counts = Patron.getQueueCounts();
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", -1));
        }
    }

    @GetMapping("/without-orders")
    public ResponseEntity<List<PatronResponse>> getPatronsWithoutOrders() {
        try {
            List<PatronResponse> responses = new ArrayList<>();
            for (Patron patron : Patron.getWithoutOrders()) {
                responses.add(new PatronResponse(patron));
            }
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/with-orders/dinein")
    public ResponseEntity<List<PatronResponse>> getDineInPatronsWithOrders() {
        try {
            List<PatronResponse> responses = new ArrayList<>();
            for (Patron patron : Patron.getDineInWithOrders()) {
                responses.add(new PatronResponse(patron));
            }
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/pending-counts")
    public ResponseEntity<Map<String, Integer>> getPendingOrderCounts() {
        try {
            Map<String, Integer> counts = Patron.getPendingOrderCounts();
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("dineInPending", 0, "takeoutPending", 0));
        }
    }

    // AVAILABILITY CHECKING ENDPOINTS
    @GetMapping("/check-dinein-availability")
    public ResponseEntity<Map<String, Object>> checkDineInAvailability(@RequestParam int groupSize) {
        try {
            Map<String, Object> availability = Patron.checkDineInAvailability(groupSize);
            return ResponseEntity.ok(availability);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "canAccept", false,
                            "message", "Error checking availability: " + e.getMessage()));
        }
    }
}