package backend.models;

import java.util.*;

public class Patron {
    private static int idCount = 1;
    private static Map<Integer, Patron> allPatrons = new HashMap<>();

    public static Patron firstDineIn = null;
    public static Patron lastDineIn = null;
    public static Patron firstTakeout = null;
    public static Patron lastTakeout = null;

    private int id;
    private String name;
    private int groupSize;
    private int serviceType;
    private int tableId;
    private Waiter waiter;
    private Order order;
    public Patron next;

    public Patron(String name, int groupSize, int serviceType) {
        this.id = idCount++;
        this.name = name;
        this.groupSize = groupSize;
        this.serviceType = serviceType;
        this.tableId = 0;
        this.waiter = null;
        this.order = null;
        this.next = null;
        allPatrons.put(this.id, this);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public int getServiceType() {
        return serviceType;
    }

    public int getTableId() {
        return tableId;
    }

    public Waiter getWaiter() {
        return waiter;
    }

    public Order getOrder() {
        return order;
    }

    public void setWaiter(Waiter w) {
        this.waiter = w;
    }

    public void setTableId(int id) {
        this.tableId = id;
    }

    public void setOrder(Order o) {
        this.order = o;
    }

    // PATRON CREATION AND QUEUE MANAGEMENT
    public static Map<String, Object> createNewPatron(int serviceType, Integer groupSize) {

        if (serviceType == 1) {
            if (!Waiter.hasAvailableWaitersForDineIn()) {
                throw new IllegalArgumentException("No available waiters for dine-in. Please order takeout.");
            }

            int finalGroupSize = groupSize != null && groupSize > 0 ? groupSize : 1; // Dine-in
            if (!TableManager.hasAvailableTableForGroup(finalGroupSize)) {
                throw new IllegalArgumentException(
                        "No available tables for group of " + finalGroupSize + ". Please order takeout.");
            }
        }

        int finalGroupSize = serviceType == 0 ? 1 : (groupSize != null && groupSize > 0 ? groupSize : 1);
        Patron patron = new Patron("Customer", finalGroupSize, serviceType);
        patron.joinQueue();

        Map<String, Object> response = new HashMap<>();
        response.put("id", patron.getId());
        response.put("groupSize", finalGroupSize);
        response.put("serviceType", serviceType);
        response.put("message", "Customer added successfully");
        return response;
    }

    public void joinQueue() {
        if (serviceType == 0) {
            addToTakeout();
            handleTakeoutQueue();
        } else {
            addToDineIn();
            handleDineInQueue();
        }
    }

    private void addToTakeout() {
        if (firstTakeout == null) {
            firstTakeout = this;

        } else {
            lastTakeout.next = this;
        }
        lastTakeout = this;
    }

    private void addToDineIn() {
        if (firstDineIn == null) {
            firstDineIn = this;
        } else {
            lastDineIn.next = this;
        }
        lastDineIn = this;
    }

    // QUEUE PROCESSING METHODS
    private static void handleTakeoutQueue() {
        Patron current = firstTakeout;
        while (current != null) {
            if (current.getWaiter() == null) {
                Waiter w = Waiter.findFreeWaiterForTakeout();
                if (w != null) {
                    current.setWaiter(w);
                }
            }
            current = current.next;
        }
    }

    public static void handleDineInQueue() {
        Patron current = firstDineIn;
        while (current != null) {
            if (current.getTableId() == 0) {
                if (current.getWaiter() == null) {
                    Waiter w = Waiter.findFreeWaiterForDineIn();
                    if (w != null) {
                        current.setWaiter(w);
                    }
                }
                TableManager.Table t = TableManager.findTable(current.getGroupSize());
                if (t != null) {
                    t.assignToPatron(current);
                    current.setTableId(t.getNumber());
                }
            }
            current = current.next;
        }
    }

    // PATRON SEARCH AND LISTS
    public static Patron findById(int id) {
        return allPatrons.get(id);
    }

    public static List<Patron> getAll() {
        List<Patron> list = new ArrayList<>();
        addFromQueue(firstDineIn, list);
        addFromQueue(firstTakeout, list);
        return list;
    }

    private static void addFromQueue(Patron start, List<Patron> list) {
        Patron current = start;
        while (current != null) {
            list.add(current);
            current = current.next;
        }
    }

    public static List<Patron> getWithoutOrders() {
        List<Patron> list = new ArrayList<>();
        addWithoutOrders(firstDineIn, list);
        addWithoutOrders(firstTakeout, list);
        return list;
    }

    private static void addWithoutOrders(Patron start, List<Patron> list) {
        Patron current = start;
        while (current != null) {
            if (current.getOrder() == null) {
                list.add(current);
            }
            current = current.next;
        }
    }

    public static List<Patron> getDineInWithOrders() {
        List<Patron> list = new ArrayList<>();
        Patron current = firstDineIn;
        while (current != null) {
            if (current.getOrder() != null && current.getServiceType() == 1) {
                list.add(current);
            }
            current = current.next;
        }
        return list;
    }

    // COUNTING AND STATISTICS METHODS
    public static Map<String, Integer> getQueueCounts() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("dineInQueue", countQueue(firstDineIn));
        counts.put("takeoutQueue", countQueue(firstTakeout));
        return counts;
    }

    private static int countQueue(Patron start) {
        int count = 0;
        Patron current = start;
        while (current != null) {
            if (current.getOrder() == null) {
                count++;
            }
            current = current.next;
        }
        return count;
    }

    public static Map<String, Integer> getPendingOrderCounts() {
        Map<String, Integer> counts = new HashMap<>();
        int dineInPending = 0;
        int takeoutPending = 0;

        for (Patron patron : getAll()) {
            if (patron.getOrder() != null && !patron.getOrder().isPaid()) {
                if (patron.getServiceType() == 1) {
                    dineInPending++;
                } else if (patron.getServiceType() == 0) {
                    takeoutPending++;
                }
            }
        }

        counts.put("dineInPending", dineInPending);
        counts.put("takeoutPending", takeoutPending);
        return counts;
    }

    // AVAILABILITY CHECKING
    public static Map<String, Object> checkDineInAvailability(int groupSize) {
        Map<String, Object> result = new HashMap<>();

        boolean waitersAvailable = Waiter.hasAvailableWaitersForDineIn();
        boolean tablesAvailable = TableManager.hasAvailableTableForGroup(groupSize);
        boolean canAccept = waitersAvailable && tablesAvailable;

        result.put("canAccept", canAccept);
        result.put("waitersAvailable", waitersAvailable);
        result.put("tablesAvailable", tablesAvailable);

        if (!canAccept) {
            String message = "Restaurant fully booked - ";
            if (!waitersAvailable && !tablesAvailable) {
                message += "no waiters and no tables for group of " + groupSize;
            } else if (!waitersAvailable) {
                message += "no waiters for dine-in";
            } else {
                message += "no tables for group of " + groupSize;
            }
            message += ". Please order takeout.";
            result.put("message", message);
        } else {
            result.put("message", "Restaurant can accept your booking");
        }

        return result;
    }

}