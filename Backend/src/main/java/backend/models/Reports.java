package backend.models;

import java.util.*;

public class Reports {

    // INNER REPORT CLASSES
    public static class DailyReport {
        private int totalOrders;
        private double totalRevenue;
        private double averageOrderValue;
        private double dineInRate;
        private int dineInOrders;
        private int takeoutOrders;
        private double dineInRevenue;
        private double takeoutRevenue;
        private List<WaiterReport> waiterReports;
        private List<MenuItemReport> menuItemReports;

        public DailyReport() {
        }

        // GETTERS AND SETTERS
        public int getTotalOrders() {
            return totalOrders;
        }

        public void setTotalOrders(int totalOrders) {
            this.totalOrders = totalOrders;
        }

        public double getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(double totalRevenue) {
            this.totalRevenue = totalRevenue;
        }

        public double getAverageOrderValue() {
            return averageOrderValue;
        }

        public void setAverageOrderValue(double averageOrderValue) {
            this.averageOrderValue = averageOrderValue;
        }

        public double getDineInRate() {
            return dineInRate;
        }

        public void setDineInRate(double dineInRate) {
            this.dineInRate = dineInRate;
        }

        public int getDineInOrders() {
            return dineInOrders;
        }

        public void setDineInOrders(int dineInOrders) {
            this.dineInOrders = dineInOrders;
        }

        public int getTakeoutOrders() {
            return takeoutOrders;
        }

        public void setTakeoutOrders(int takeoutOrders) {
            this.takeoutOrders = takeoutOrders;
        }

        public double getDineInRevenue() {
            return dineInRevenue;
        }

        public void setDineInRevenue(double dineInRevenue) {
            this.dineInRevenue = dineInRevenue;
        }

        public double getTakeoutRevenue() {
            return takeoutRevenue;
        }

        public void setTakeoutRevenue(double takeoutRevenue) {
            this.takeoutRevenue = takeoutRevenue;
        }

        public List<WaiterReport> getWaiterReports() {
            return waiterReports;
        }

        public void setWaiterReports(List<WaiterReport> waiterReports) {
            this.waiterReports = waiterReports;
        }

        public List<MenuItemReport> getMenuItemReports() {
            return menuItemReports;
        }

        public void setMenuItemReports(List<MenuItemReport> menuItemReports) {
            this.menuItemReports = menuItemReports;
        }
    }

    public static class WaiterReport {
        private String waiterName;
        private String staffId;
        private int dineInOrders;
        private int takeoutOrders;
        private double totalSales;
        private double commission;
        private String status;

        public WaiterReport() {
        }

        public WaiterReport(String waiterName, String staffId, int dineInOrders, int takeoutOrders,
                double totalSales, double commission, String status) {
            this.waiterName = waiterName;
            this.staffId = staffId;
            this.dineInOrders = dineInOrders;
            this.takeoutOrders = takeoutOrders;
            this.totalSales = totalSales;
            this.commission = commission;
            this.status = status;
        }

        // GETTERS AND SETTERS
        public String getWaiterName() {
            return waiterName;
        }

        public void setWaiterName(String waiterName) {
            this.waiterName = waiterName;
        }

        public String getStaffId() {
            return staffId;
        }

        public void setStaffId(String staffId) {
            this.staffId = staffId;
        }

        public int getDineInOrders() {
            return dineInOrders;
        }

        public void setDineInOrders(int dineInOrders) {
            this.dineInOrders = dineInOrders;
        }

        public int getTakeoutOrders() {
            return takeoutOrders;
        }

        public void setTakeoutOrders(int takeoutOrders) {
            this.takeoutOrders = takeoutOrders;
        }

        public double getTotalSales() {
            return totalSales;
        }

        public void setTotalSales(double totalSales) {
            this.totalSales = totalSales;
        }

        public double getCommission() {
            return commission;
        }

        public void setCommission(double commission) {
            this.commission = commission;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class MenuItemReport {
        private String itemName;
        private String category;
        private int quantitySold;
        private double revenue;
        private double averagePrice;

        public MenuItemReport() {
        }

        public MenuItemReport(String itemName, String category, int quantitySold, double revenue, double averagePrice) {
            this.itemName = itemName;
            this.category = category;
            this.quantitySold = quantitySold;
            this.revenue = revenue;
            this.averagePrice = averagePrice;
        }

        // GETTERS AND SETTERS
        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public int getQuantitySold() {
            return quantitySold;
        }

        public void setQuantitySold(int quantitySold) {
            this.quantitySold = quantitySold;
        }

        public double getRevenue() {
            return revenue;
        }

        public void setRevenue(double revenue) {
            this.revenue = revenue;
        }

        public double getAveragePrice() {
            return averagePrice;
        }

        public void setAveragePrice(double averagePrice) {
            this.averagePrice = averagePrice;
        }
    }

    // REPORT GENERATION: Generate complete daily report
    public static DailyReport generateDailyReport() {
        DailyReport report = new DailyReport();

        List<WaiterReport> waiterReports = generateWaiterReports();
        List<Order> paidOrders = getPaidOrders();

        // Calculate business metrics from paid orders
        int totalDineInOrders = waiterReports.stream().mapToInt(WaiterReport::getDineInOrders).sum();
        int totalTakeoutOrders = waiterReports.stream().mapToInt(WaiterReport::getTakeoutOrders).sum();
        double totalRevenue = paidOrders.stream().mapToDouble(Order::getTotalAmount).sum();
        double dineInRevenue = paidOrders.stream()
                .filter(order -> order.getTableNumber() > 0)
                .mapToDouble(Order::getTotalAmount).sum();

        // Set all report data
        report.setTotalOrders(totalDineInOrders + totalTakeoutOrders);
        report.setTotalRevenue(totalRevenue);
        report.setAverageOrderValue(report.getTotalOrders() > 0 ? totalRevenue / report.getTotalOrders() : 0);
        report.setDineInOrders(totalDineInOrders);
        report.setTakeoutOrders(totalTakeoutOrders);
        report.setDineInRate(report.getTotalOrders() > 0 ? (totalDineInOrders * 100.0) / report.getTotalOrders() : 0);
        report.setDineInRevenue(dineInRevenue);
        report.setTakeoutRevenue(totalRevenue - dineInRevenue);
        report.setWaiterReports(waiterReports);
        report.setMenuItemReports(generateMenuItemReports(paidOrders));

        return report;
    }

    // REPORT DATA: Get overview report data
    public static Map<String, Object> getOverviewReport() {
        DailyReport report = generateDailyReport();
        Map<String, Object> overview = new HashMap<>();

        overview.put("totalOrders", report.getTotalOrders());
        overview.put("totalRevenue", Math.round(report.getTotalRevenue() * 100.0) / 100.0);
        overview.put("averageOrderValue", Math.round(report.getAverageOrderValue() * 100.0) / 100.0);
        overview.put("dineInRate", Math.round(report.getDineInRate() * 100.0) / 100.0);
        overview.put("dineInOrders", report.getDineInOrders());
        overview.put("takeoutOrders", report.getTakeoutOrders());
        overview.put("dineInRevenue", Math.round(report.getDineInRevenue() * 100.0) / 100.0);
        overview.put("takeoutRevenue", Math.round(report.getTakeoutRevenue() * 100.0) / 100.0);

        return overview;
    }

    // REPORT DATA: Get staff report data
    public static Map<String, Object> getStaffReport() {
        DailyReport report = generateDailyReport();
        Map<String, Object> staff = new HashMap<>();
        staff.put("waiters", report.getWaiterReports());
        return staff;
    }

    // REPORT DATA: Get menu report data
    public static Map<String, Object> getMenuReport() {
        DailyReport report = generateDailyReport();
        Map<String, Object> menu = new HashMap<>();
        menu.put("items", report.getMenuItemReports());
        return menu;
    }

    // REPORT DATA: Get finance report data
    public static Map<String, Object> getFinanceReport() {
        DailyReport report = generateDailyReport();
        Map<String, Object> finance = new HashMap<>();

        double totalRevenue = report.getTotalRevenue();
        double totalCommission = report.getWaiterReports().stream()
                .mapToDouble(WaiterReport::getCommission)
                .sum();
        double netIncome = totalRevenue - totalCommission;

        finance.put("totalRevenue", Math.round(totalRevenue * 100.0) / 100.0);
        finance.put("staffSalary", Math.round(totalCommission * 100.0) / 100.0);
        finance.put("netIncome", Math.round(netIncome * 100.0) / 100.0);

        return finance;
    }

    // REPORT DATA: Get complete daily report for API
    public static Map<String, Object> getCompleteDailyReport() {
        DailyReport report = generateDailyReport();
        Map<String, Object> response = new HashMap<>();

        response.put("overview", getOverviewReport());
        response.put("staff", getStaffReport());
        response.put("menu", getMenuReport());
        response.put("finance", getFinanceReport());

        return response;
    }

    // HELPER METHODS
    private static List<WaiterReport> generateWaiterReports() {
        List<WaiterReport> waiterReports = new ArrayList<>();
        Waiter current = Waiter.head;

        if (current != null) {
            do {
                WaiterReport wr = new WaiterReport(
                        current.getName(),
                        current.getStaffId(),
                        current.getDineInCount(),
                        current.getTakeOutCount(),
                        current.getTotalSales(),
                        current.getTotalCommission(),
                        current.getAssignedDineInCount() < 4 ? "Available" : "Fully Booked");
                waiterReports.add(wr);
                current = current.nextWaiter;
            } while (current != Waiter.head);
        }
        return waiterReports;
    }

    private static List<MenuItemReport> generateMenuItemReports(List<Order> paidOrders) {
        Map<String, MenuItemReport> itemMap = new HashMap<>();

        for (Order order : paidOrders) {
            for (Order.OrderItem item : order.getItems()) {
                String itemName = item.getName();
                MenuItemReport report = itemMap.get(itemName);

                if (report == null) {
                    report = new MenuItemReport();
                    report.setItemName(itemName);
                    report.setCategory(item.getCategory());
                    report.setQuantitySold(0);
                    report.setRevenue(0);
                    report.setAveragePrice(item.getPrice());
                    itemMap.put(itemName, report);
                }

                report.setQuantitySold(report.getQuantitySold() + item.getQuantity());
                report.setRevenue(report.getRevenue() + (item.getPrice() * item.getQuantity()));
            }
        }

        return new ArrayList<>(itemMap.values());
    }

    private static List<Order> getPaidOrders() {
        List<Order> paidOrders = new ArrayList<>();
        List<Order> allOrders = getAllOrders();

        for (Order order : allOrders) {
            if (order.isPaid()) {
                paidOrders.add(order);
            }
        }
        return paidOrders;
    }

    private static List<Order> getAllOrders() {
        List<Order> allOrders = new ArrayList<>();
        Order current = Order.firstOrder;

        while (current != null) {
            allOrders.add(current);
            current = current.nextOrder;
            if (current == Order.firstOrder)
                break;
        }
        return allOrders;
    }
}