package backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import backend.models.Reports;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportsController {

    // REPORTS ENDPOINTS: Get complete daily report
    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDailyReport() {
        try {
            Map<String, Object> response = Reports.getCompleteDailyReport();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // REPORTS ENDPOINTS: Get overview statistics
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverviewReport() {
        try {
            Map<String, Object> overview = Reports.getOverviewReport();
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // REPORTS ENDPOINTS: Get staff performance report
    @GetMapping("/staff")
    public ResponseEntity<Map<String, Object>> getStaffReport() {
        try {
            Map<String, Object> staff = Reports.getStaffReport();
            return ResponseEntity.ok(staff);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // REPORTS ENDPOINTS: Get menu analysis report
    @GetMapping("/menu")
    public ResponseEntity<Map<String, Object>> getMenuReport() {
        try {
            Map<String, Object> menu = Reports.getMenuReport();
            return ResponseEntity.ok(menu);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // REPORTS ENDPOINTS: Get financial report
    @GetMapping("/finance")
    public ResponseEntity<Map<String, Object>> getFinanceReport() {
        try {
            Map<String, Object> finance = Reports.getFinanceReport();
            return ResponseEntity.ok(finance);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}