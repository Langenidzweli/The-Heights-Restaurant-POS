package backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import backend.models.Order;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    static class OrderRequest {
        private int patronId;
        private List<Order.OrderItemRequest> items;

        public int getPatronId() {
            return patronId;
        }

        public void setPatronId(int patronId) {
            this.patronId = patronId;
        }

        public List<Order.OrderItemRequest> getItems() {
            return items;
        }

        public void setItems(List<Order.OrderItemRequest> items) {
            this.items = items;
        }
    }

    // ORDER CREATION ENDPOINTS
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody OrderRequest request) {
        try {
            Map<String, Object> result = Order.createOrder(request.getPatronId(), request.getItems());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create order: " + e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/add-items")
    public ResponseEntity<Map<String, Object>> addItemsToOrder(
            @PathVariable int orderId,
            @RequestBody List<Order.OrderItemRequest> newItems) {
        try {
            Map<String, Object> result = Order.addItemsToOrder(orderId, newItems);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ORDER RETRIEVAL ENDPOINTS
    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable int orderId) {
        try {
            Map<String, Object> result = Order.getOrderDetails(orderId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load order details: " + e.getMessage()));
        }
    }

    @GetMapping("/unpaid")
    public ResponseEntity<List<Map<String, Object>>> getUnpaidOrders() {
        try {
            List<Map<String, Object>> unpaidOrders = Order.getUnpaidOrders();
            return ResponseEntity.ok(unpaidOrders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingOrders() {
        try {
            List<Map<String, Object>> pendingOrders = Order.getPendingOrders();
            return ResponseEntity.ok(pendingOrders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // PAYMENT PROCESSING ENDPOINTS
    @PostMapping("/{orderId}/mark-paid")
    public ResponseEntity<Map<String, Object>> markOrderAsPaid(@PathVariable int orderId) {
        try {
            Map<String, Object> result = Order.markOrderAsPaid(orderId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to mark order as paid: " + e.getMessage()));
        }
    }

    @GetMapping("/patron/{patronId}")
    public ResponseEntity<Map<String, Object>> getOrderByPatronId(@PathVariable int patronId) {
        try {
            Map<String, Object> result = Order.getOrderByPatronId(patronId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to load order details: " + e.getMessage()));
        }
    }
}