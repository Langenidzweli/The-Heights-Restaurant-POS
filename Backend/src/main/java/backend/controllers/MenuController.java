package backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import backend.models.Order;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "*")
public class MenuController {

    // Get all menu items
    @GetMapping("/items")
    public ResponseEntity<List<Map<String, Object>>> getMenuItems() {
        try {
            List<Map<String, Object>> menuData = Order.getAllMenuItems();
            return ResponseEntity.ok(menuData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(Map.of("error", "Failed to load menu items")));
        }
    }

    // Get menu items with descriptions
    @GetMapping("/items-with-descriptions")
    public ResponseEntity<List<Map<String, Object>>> getMenuItemsWithDescriptions() {
        try {
            List<Map<String, Object>> menuData = Order.getMenuItemsWithDescriptions();
            return ResponseEntity.ok(menuData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(Map.of("error", "Failed to load menu items")));
        }
    }

    // Get menu items by category
    @GetMapping("/categories/{category}")
    public ResponseEntity<List<Map<String, Object>>> getMenuItemsByCategory(@PathVariable String category) {
        try {
            List<Map<String, Object>> menuData = Order.getMenuItemsByCategory(category);
            return ResponseEntity.ok(menuData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(Map.of("error", "Failed to load menu items by category")));
        }
    }
}