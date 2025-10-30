package backend.models;

import java.util.*;

public class TableManager {

    // Nested static class Table
    public static class Table {
        private int tableNumber;
        private int tableSize;
        private boolean occupied;
        private Patron patron;

        public Table(int tableNumber, int tableSize) {
            this.tableNumber = tableNumber;
            this.tableSize = tableSize;
            this.occupied = false;
            this.patron = null;
        }

        // GETTER METHODS
        public int getTableNumber() {
            return tableNumber;
        }

        public int getTableSize() {
            return tableSize;
        }

        public boolean isOccupied() {
            return occupied;
        }

        public Patron getPatron() {
            return patron;
        }

        // TABLE ASSIGNMENT: Assign patron to this table
        public void assignPatron(Patron p) {
            this.patron = p;
            this.occupied = true;
            TableManager.removeFromAvailableQueues(this);
        }

        // TABLE RELEASE: Free table when patron leaves
        public void releaseTable() {
            this.patron = null;
            this.occupied = false;
            TableManager.addToAvailableQueues(this);
            Patron.handleDineInQueue();
        }

        // TABLE ASSIGNMENT: Assign patron (alias method)
        public void assignToPatron(Patron patron) {
            assignPatron(patron);
        }

        // TABLE INFO: Get table number (alias method)
        public int getNumber() {
            return tableNumber;
        }
    }

    // Table management data structures
    private static Map<Integer, Queue<Table>> availableTablesBySize = new HashMap<>();
    private static Map<Integer, Table> allTablesMap = new HashMap<>();
    private static List<Table> allTablesList = new ArrayList<>();

    // TABLE MANAGEMENT: Initialize all restaurant tables
    public static void defineTables() {
        availableTablesBySize.clear();
        allTablesMap.clear();
        allTablesList.clear();

        // Define all tables with different sizes
        Table[] tableDefinitions = {
                new Table(1, 2), new Table(2, 2), new Table(3, 2), new Table(4, 2),
                new Table(5, 4), new Table(6, 4), new Table(7, 4), new Table(8, 4),
                new Table(9, 4), new Table(10, 4), new Table(11, 6), new Table(12, 6),
                new Table(13, 6), new Table(14, 6), new Table(15, 8), new Table(16, 8)
        };

        // Add all tables to management system
        for (Table table : tableDefinitions) {
            allTablesList.add(table);
            allTablesMap.put(table.getTableNumber(), table);
            addToAvailableQueues(table);
        }
    }

    // TABLE ASSIGNMENT: Get available table for group size
    public static Table getAvailableTable(int groupSize) {
        Integer[] possibleSizes = { 2, 4, 6, 8 };

        // Find smallest table that fits the group
        for (int size : possibleSizes) {
            if (size >= groupSize) {
                Table table = getAvailableTableOfSize(size);
                if (table != null) {
                    return table;
                }
            }
        }
        return null;
    }

    // TABLE ASSIGNMENT: Find table by size (alias method)
    public static Table findTable(int groupSize) {
        return getAvailableTable(groupSize);
    }

    // TABLE MANAGEMENT: Get table by number
    public static Table getTableByNumber(int tableNumber) {
        return allTablesMap.get(tableNumber);
    }

    // TABLE MANAGEMENT: Get all tables
    public static List<Table> getAllTables() {
        return new ArrayList<>(allTablesList);
    }

    // TABLE STATUS: Get all tables with details for API
    public static List<Map<String, Object>> getAllTablesWithDetails() {
        List<Map<String, Object>> tablesData = new ArrayList<>();

        for (Table table : allTablesList) {
            Map<String, Object> tableData = new HashMap<>();
            tableData.put("tableNumber", table.getTableNumber());
            tableData.put("tableSize", table.getTableSize());
            tableData.put("occupied", table.isOccupied());

            // Add patron info if table is occupied
            if (table.isOccupied() && table.getPatron() != null) {
                tableData.put("patron", Map.of("id", table.getPatron().getId()));
            } else {
                tableData.put("patron", null);
            }

            tablesData.add(tableData);
        }
        return tablesData;
    }

    // TABLE STATUS: Get table status summary
    public static Map<String, Object> getTableStatus() {
        Map<String, Object> status = new HashMap<>();
        int totalTables = allTablesList.size();
        int occupiedTables = (int) allTablesList.stream()
                .filter(Table::isOccupied)
                .count();

        status.put("totalTables", totalTables);
        status.put("occupiedTables", occupiedTables);
        status.put("availableTables", totalTables - occupiedTables);

        return status;
    }

    // TABLE MANAGEMENT: Add table to available queues
    private static void addToAvailableQueues(Table table) {
        if (table == null || table.isOccupied())
            return;

        int tableSize = table.getTableSize();
        availableTablesBySize.computeIfAbsent(tableSize, k -> new LinkedList<>())
                .offer(table);
    }

    // TABLE MANAGEMENT: Remove table from available queues
    private static void removeFromAvailableQueues(Table table) {
        if (table == null)
            return;

        int tableSize = table.getTableSize();
        Queue<Table> queue = availableTablesBySize.get(tableSize);
        if (queue != null) {
            queue.remove(table);
        }
    }

    // TABLE MANAGEMENT: Get available table of specific size
    private static Table getAvailableTableOfSize(int tableSize) {
        Queue<Table> queue = availableTablesBySize.get(tableSize);
        if (queue != null && !queue.isEmpty()) {
            Table table = queue.poll();
            if (table != null && !table.isOccupied()) {
                return table;
            }
        }
        return null;
    }

    // TABLE CAPACITY: Check if table available for group
    public static boolean hasAvailableTableForGroup(int groupSize) {
        Integer[] possibleSizes = { 2, 4, 6, 8 };

        for (int size : possibleSizes) {
            if (size >= groupSize) {
                Queue<Table> queue = availableTablesBySize.get(size);
                if (queue != null && !queue.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    // TABLE STATISTICS: Get available table counts
    public static Map<Integer, Integer> getAvailableTableCounts() {
        Map<Integer, Integer> counts = new HashMap<>();
        for (Map.Entry<Integer, Queue<Table>> entry : availableTablesBySize.entrySet()) {
            counts.put(entry.getKey(), entry.getValue().size());
        }
        return counts;
    }

    // TABLE FINDING: Find next available table size
    public static Integer findNextAvailableSize(int groupSize) {
        Integer[] possibleSizes = { 2, 4, 6, 8 };

        for (int size : possibleSizes) {
            if (size >= groupSize) {
                Queue<Table> queue = availableTablesBySize.get(size);
                if (queue != null && !queue.isEmpty()) {
                    return size;
                }
            }
        }
        return null;
    }
}