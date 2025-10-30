package backend.models;

import java.util.*;

public class Waiter {
    private String name;
    private String staffId;
    private double totalSales = 0;
    private double totalCommission = 0;
    private int dineInCount = 0;
    private int takeOutCount = 0;
    private int assignedDineInCount = 0;
    public Waiter nextWaiter;

    // queue management
    public static Waiter head = null;
    public static Waiter tail = null;
    public static Waiter current = null;
    private static Map<String, Waiter> waitersMap = new HashMap<>();

    // Inner class for statistics
    public static class WaiterStats {
        private String name;
        private String staffId;
        private int dineInCount;
        private int takeOutCount;
        private int totalCustomers;
        private String status;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStaffId() {
            return staffId;
        }

        public void setStaffId(String staffId) {
            this.staffId = staffId;
        }

        public int getDineInCount() {
            return dineInCount;
        }

        public void setDineInCount(int dineInCount) {
            this.dineInCount = dineInCount;
        }

        public int getTakeOutCount() {
            return takeOutCount;
        }

        public void setTakeOutCount(int takeOutCount) {
            this.takeOutCount = takeOutCount;
        }

        public int getTotalCustomers() {
            return totalCustomers;
        }

        public void setTotalCustomers(int totalCustomers) {
            this.totalCustomers = totalCustomers;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    // Create new waiter and add to circular queue

    public Waiter(String name, String staffId) {
        this.name = name;
        this.staffId = staffId;
        this.nextWaiter = null;

        if (head == null) {
            head = this;
            tail = this;
            tail.nextWaiter = head;
        } else {
            tail.nextWaiter = this;
            tail = this;
            tail.nextWaiter = head;
        }

        if (current == null) {
            current = head;
        }

        waitersMap.put(staffId, this);
    }

    public String getName() {
        return name;
    }

    public String getStaffId() {
        return staffId;
    }

    public int getDineInCount() {
        return dineInCount;
    }

    public int getTakeOutCount() {
        return takeOutCount;
    }

    public int getAssignedDineInCount() {
        return assignedDineInCount;
    }

    public double getTotalSales() {
        return totalSales;
    }

    public double getTotalCommission() {
        return totalCommission;
    }

    // Get available waiter for dine-in with round-robin

    public static Waiter getAvailableWaiterForDineIn() {
        if (current == null)
            return null;

        Waiter start = current;
        int attempts = 0;
        final int maxAttempts = waitersMap.size();

        do {
            if (current.assignedDineInCount < 4) {
                Waiter assigned = current;
                current = current.nextWaiter;
                assigned.assignedDineInCount++;
                return assigned;
            }
            current = current.nextWaiter;
            attempts++;
        } while (current != start && attempts < maxAttempts);

        return null;
    }

    // Get waiter for takeout with round-robin

    public static Waiter getAvailableWaiterForTakeOut() {
        if (current == null)
            return null;

        Waiter assigned = current;
        current = current.nextWaiter;
        return assigned;
    }

    // Find waiter by staff ID

    public static Waiter findWaiterByStaffId(String staffId) {
        if (staffId == null)
            return null;
        return waitersMap.get(staffId);
    }

    // Release dine-in slot

    public void releaseDineInSlot() {
        if (this.assignedDineInCount > 0) {
            this.assignedDineInCount--;
        }
    }

    // Check availability for dine-in

    public boolean isAvailableForDineIn() {
        return this.assignedDineInCount < 4;
    }

    // Check availability for takeout

    public boolean isAvailableForTakeOut() {
        return true;
    }

    // Add order item and update statistics

    public void addOrderItem(double amount, boolean isDineIn) {
        this.totalSales += amount;
        this.totalCommission = this.totalSales * 0.15;

        if (isDineIn) {
            this.dineInCount++;
        } else {
            this.takeOutCount++;
        }
    }

    // Get waiter statistics for API

    public static List<WaiterStats> getWaiterStats() {
        List<WaiterStats> stats = new ArrayList<>();
        Waiter current = head;

        if (current != null) {
            do {
                WaiterStats waiterStat = new WaiterStats();
                waiterStat.setName(current.getName());
                waiterStat.setStaffId(current.getStaffId());
                waiterStat.setDineInCount(current.getDineInCount());
                waiterStat.setTakeOutCount(current.getTakeOutCount());
                waiterStat.setTotalCustomers(current.getDineInCount() + current.getTakeOutCount());
                waiterStat.setStatus(current.getAssignedDineInCount() < 4 ? "Available" : "Fully Booked");
                stats.add(waiterStat);
                current = current.nextWaiter;
            } while (current != head);
        }
        return stats;
    }

    // Get detailed waiter information for API

    public static List<Map<String, Object>> getWaitersDetailed() {
        List<Map<String, Object>> waitersData = new ArrayList<>();
        Waiter current = head;

        if (current != null) {
            do {
                Map<String, Object> waiterData = new HashMap<>();
                waiterData.put("name", current.getName());
                waiterData.put("staffId", current.getStaffId());
                waiterData.put("dineInCount", current.getDineInCount());
                waiterData.put("takeOutCount", current.getTakeOutCount());
                waiterData.put("totalCustomers", current.getDineInCount() + current.getTakeOutCount());
                waiterData.put("totalSales", current.getTotalSales());
                waiterData.put("totalCommission", current.getTotalCommission());
                waiterData.put("status", current.getAssignedDineInCount() < 4 ? "Available" : "Fully Booked");
                waitersData.add(waiterData);
                current = current.nextWaiter;
            } while (current != head);
        }
        return waitersData;
    }

    // Initialize waiters

    public static void defineWaiters() {
        head = null;
        tail = null;
        current = null;
        waitersMap.clear();

        // Create waiters

        new Waiter("Thandi Mthembu", "STF001");
        new Waiter("Sphiwe Ndlovu", "STF002");
        new Waiter("Naledi Mokoena", "STF003");
        new Waiter("Mpho Sithole", "STF004");

    }

    public static java.util.Collection<Waiter> getAllWaiters() {
        return waitersMap.values();
    }

    public static int getWaiterCount() {
        return waitersMap.size();
    }

    // Check if waiter exists

    public static boolean waiterExists(String staffId) {
        return waitersMap.containsKey(staffId);
    }

    // Get individual waiter stats

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("name", this.name);
        stats.put("staffId", this.staffId);
        stats.put("dineInCount", this.dineInCount);
        stats.put("takeOutCount", this.takeOutCount);
        stats.put("assignedDineInCount", this.assignedDineInCount);
        stats.put("totalSales", this.totalSales);
        stats.put("totalCommission", this.totalCommission);
        stats.put("availableForDineIn", this.isAvailableForDineIn());
        stats.put("availableForTakeOut", this.isAvailableForTakeOut());
        return stats;
    }

    // Get statistics for all waiters

    public static java.util.List<Map<String, Object>> getAllWaiterStats() {
        java.util.List<Map<String, Object>> allStats = new java.util.ArrayList<>();
        for (Waiter waiter : waitersMap.values()) {
            allStats.add(waiter.getStats());
        }
        return allStats;
    }

    // Find free waiter for dine-in

    public static Waiter findFreeWaiterForDineIn() {
        return getAvailableWaiterForDineIn();
    }

    // Find free waiter for takeout

    public static Waiter findFreeWaiterForTakeout() {
        return getAvailableWaiterForTakeOut();
    }

    // Release waiter when order is paid

    public static void releaseWaiterAfterPayment(String staffId, boolean isDineIn) {
        Waiter waiter = findWaiterByStaffId(staffId);
        if (waiter != null) {
            if (isDineIn) {
                waiter.releaseDineInSlot();
            }
        }
    }

    // Check if any waiters are available for dine-in

    public static boolean hasAvailableWaitersForDineIn() {
        if (head == null)
            return false;

        Waiter current = head;
        do {
            if (current.getAssignedDineInCount() < 4) {
                return true;
            }
            current = current.nextWaiter;
        } while (current != head);

        return false;
    }

}