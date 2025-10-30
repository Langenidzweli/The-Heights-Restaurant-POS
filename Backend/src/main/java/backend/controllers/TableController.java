package backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import backend.models.TableManager;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tables")
@CrossOrigin(origins = "*")
public class TableController {

    // TABLES ENDPOINTS: Get all tables with details
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllTables() {
        try {
            List<Map<String, Object>> tablesData = TableManager.getAllTablesWithDetails();
            return ResponseEntity.ok(tablesData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(Map.of("error", "Failed to load tables: " + e.getMessage())));
        }
    }

    // TABLES ENDPOINTS: Get table status summary
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getTableStatus() {
        try {
            Map<String, Object> status = TableManager.getTableStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get table status"));
        }
    }
}