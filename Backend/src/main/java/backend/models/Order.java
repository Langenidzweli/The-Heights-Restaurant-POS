package backend.models;

import java.util.*;

public class Order {
    private static int orderIdCounter = 1;
    private static int itemIdCounter = 1;
    public static Order firstOrder = null;
    public static Order lastOrder = null;
    public Order nextOrder;

    private static Map<Integer, Order> allOrders = new HashMap<>();
    private static List<Order> ordersList = new ArrayList<>();

    private int orderId;
    private int patronId;
    private String waiterId;
    private int tableNumber;
    private List<OrderItem> items;
    private double total;
    private boolean paid;

    // All available food and drink
    public static final List<MenuItem> MENU = Arrays.asList(
            new MenuItem("Classic Burger", 85.0, "Main Meal", "Beef patty with lettuce, tomato, and our special sauce"),
            new MenuItem("Grilled Steak", 185.0, "Main Meal", "200g premium steak grilled to perfection"),
            new MenuItem("Chicken Pasta", 125.0, "Main Meal", "Creamy pasta with grilled chicken and mushrooms"),
            new MenuItem("Fish & Chips", 95.0, "Main Meal", "Fresh hake with crispy chips and tartare sauce"),
            new MenuItem("Vegetarian Pizza", 115.0, "Main Meal", "Wood-fired pizza with fresh vegetables"),
            new MenuItem("Still Water", 20.0, "Drinks", "500ml bottle"),
            new MenuItem("Sparkling Water", 25.0, "Drinks", "500ml bottle"),
            new MenuItem("Coca Cola", 35.0, "Drinks", "330ml can"),
            new MenuItem("Coffee", 30.0, "Drinks", "Premium roast coffee"),
            new MenuItem("Cappuccino", 40.0, "Drinks", "Espresso with steamed milk"),
            new MenuItem("French Fries", 35.0, "Sides", "Crispy golden fries"),
            new MenuItem("Garden Salad", 45.0, "Sides", "Fresh mixed greens with vinaigrette"),
            new MenuItem("Onion Rings", 40.0, "Sides", "Crispy beer-battered onion rings"),
            new MenuItem("Garlic Bread", 25.0, "Sides", "Toasted bread with garlic butter"),
            new MenuItem("Chocolate Cake", 55.0, "Desserts", "Rich chocolate cake with cream"),
            new MenuItem("Ice Cream", 35.0, "Desserts", "Vanilla, chocolate, or strawberry"),
            new MenuItem("Cheesecake", 85.0, "Desserts", "Kimberly style cheesecake"));

    // MENU MANAGEMENT METHODS
    public static List<Map<String, Object>> getAllMenuItems() {
        List<Map<String, Object>> menuData = new ArrayList<>();
        for (MenuItem item : MENU) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("name", item.getName());
            itemMap.put("price", item.getPrice());
            itemMap.put("category", item.getCategory());
            menuData.add(itemMap);
        }
        return menuData;
    }

    public static List<Map<String, Object>> getMenuItemsWithDescriptions() {
        List<Map<String, Object>> menuData = new ArrayList<>();
        for (MenuItem item : MENU) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("name", item.getName());
            itemMap.put("price", item.getPrice());
            itemMap.put("category", item.getCategory());
            itemMap.put("description", item.getDescription());
            menuData.add(itemMap);
        }
        return menuData;
    }

    public static List<Map<String, Object>> getMenuItemsByCategory(String category) {
        List<Map<String, Object>> menuData = new ArrayList<>();
        for (MenuItem item : MENU) {
            if (item.getCategory().equalsIgnoreCase(category)) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("name", item.getName());
                itemMap.put("price", item.getPrice());
                itemMap.put("category", item.getCategory());
                itemMap.put("description", item.getDescription());
                menuData.add(itemMap);
            }
        }
        return menuData;
    }

    // CONSTRUCTOR
    public Order(int patronId, String waiterId, int tableNumber) {
        this.orderId = orderIdCounter++;
        this.patronId = patronId;
        this.waiterId = waiterId;
        this.tableNumber = tableNumber;
        this.items = new ArrayList<>();
        this.total = 0.0;
        this.paid = false;
        this.nextOrder = null;

        // Add to linked list of orders
        if (firstOrder == null) {
            firstOrder = this;
        } else {
            lastOrder.nextOrder = this;
        }
        lastOrder = this;

        allOrders.put(this.orderId, this);
        ordersList.add(this);
    }

    // GETTER METHODS
    public int getOrderId() {
        return orderId;
    }

    public int getPatronId() {
        return patronId;
    }

    public String getWaiterStaffId() {
        return waiterId;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public double getTotalAmount() {
        return total;
    }

    public boolean isPaid() {
        return paid;
    }

    // SETTER METHODS
    public void setPaid(boolean paid) {
        this.paid = paid;
        if (paid && waiterId != null) {
            Waiter waiter = findWaiterById(waiterId);
            if (waiter != null) {
                Patron patron = Patron.findById(this.patronId);
                boolean isDineIn = patron != null && patron.getServiceType() == 1;
                waiter.addOrderItem(total, isDineIn);
            }
        }
    }

    // ORDER CREATION AND MANAGEMENT
    public static Map<String, Object> createOrder(int patronId, List<OrderItemRequest> itemRequests) {
        Patron patron = Patron.findById(patronId);
        if (patron == null) {
            throw new IllegalArgumentException("Patron not found");
        }

        String waiterId = patron.getWaiter() != null ? patron.getWaiter().getStaffId() : null;
        Order order = new Order(patronId, waiterId, patron.getTableId());

        // Add all requested items to the order
        for (OrderItemRequest itemReq : itemRequests) {
            order.addItem(itemReq.getName(), itemReq.getQuantity());
        }

        patron.setOrder(order);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getOrderId());
        result.put("totalAmount", order.getTotalAmount());
        result.put("message", "Order created successfully");
        return result;
    }

    public static Map<String, Object> addItemsToOrder(int orderId, List<OrderItemRequest> newItems) {
        Order order = findOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }

        if (order.isPaid()) {
            throw new IllegalArgumentException("Cannot add items to a paid order");
        }

        for (OrderItemRequest item : newItems) {
            order.addItem(item.getName(), item.getQuantity());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getOrderId());
        result.put("totalAmount", order.getTotalAmount());
        result.put("message", "Items added to order successfully");
        return result;
    }

    // PAYMENT PROCESSING
    public static Map<String, Object> markOrderAsPaid(int orderId) {
        Order order = findOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }

        if (order.isPaid()) {
            throw new IllegalArgumentException("Order is already paid");
        }

        Patron patron = Patron.findById(order.getPatronId());

        // Release waiter when order is paid
        if (order.waiterId != null && patron != null) {
            boolean isDineIn = patron.getServiceType() == 1;
            Waiter.releaseWaiterAfterPayment(order.waiterId, isDineIn);
        }

        order.setPaid(true);

        // Release table for dine-in customers
        if (patron != null) {
            if (patron.getServiceType() == 1 && patron.getTableId() != 0) {
                TableManager.Table table = TableManager.getTableByNumber(patron.getTableId());
                if (table != null) {
                    table.releaseTable();
                }
            }
            removePatronFromQueues(patron.getId());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Order marked as paid successfully");
        result.put("orderId", orderId);
        result.put("orderData", getOrderReceiptDetails(order, patron));
        return result;
    }

    // ORDER RETRIEVAL METHODS
    public static Map<String, Object> getOrderDetails(int orderId) {
        Order order = findOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }

        Patron patron = Patron.findById(order.getPatronId());
        return buildOrderResponse(order, patron);
    }

    public static Map<String, Object> getOrderByPatronId(int patronId) {
        Patron patron = Patron.findById(patronId);
        if (patron == null) {
            throw new IllegalArgumentException("Patron not found");
        }

        Order order = patron.getOrder();
        if (order == null) {
            throw new IllegalArgumentException("No order found for patron");
        }

        return buildOrderResponse(order, patron);
    }

    public static List<Map<String, Object>> getUnpaidOrders() {
        List<Map<String, Object>> unpaidList = new ArrayList<>();
        for (Order order : ordersList) {
            if (!order.isPaid()) {
                Patron patron = Patron.findById(order.getPatronId());
                unpaidList.add(buildOrderResponse(order, patron));
            }
        }
        return unpaidList;
    }

    public static List<Map<String, Object>> getPendingOrders() {
        List<Map<String, Object>> pendingList = new ArrayList<>();
        for (Order order : ordersList) {
            if (!order.isPaid()) {
                Patron patron = Patron.findById(order.getPatronId());
                Map<String, Object> orderData = buildOrderResponse(order, patron);

                int totalItems = order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
                orderData.put("totalItems", totalItems);

                pendingList.add(orderData);
            }
        }
        return pendingList;
    }

    // HELPER METHODS
    private void addItem(String name, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        MenuItem menuItem = findMenuItem(name);
        if (menuItem == null) {
            throw new IllegalArgumentException("Menu item not found: " + name);
        }

        // Check if item already exists in order
        for (OrderItem item : items) {
            if (item.getName().equalsIgnoreCase(name)) {
                item.setQuantity(item.getQuantity() + quantity);
                calculateTotal();
                return;
            }
        }

        // Add new item to order
        items.add(new OrderItem(itemIdCounter++, name, menuItem.getPrice(), quantity, menuItem.getCategory()));
        calculateTotal();
    }

    private void calculateTotal() {
        total = 0;
        for (OrderItem item : items) {
            total += item.getPrice() * item.getQuantity();
        }
        total = Math.round(total * 100.0) / 100.0;
    }

    private static MenuItem findMenuItem(String name) {
        for (MenuItem item : MENU) {
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

    private static Waiter findWaiterById(String staffId) {
        if (staffId == null)
            return null;
        Waiter current = Waiter.head;
        if (current != null) {
            do {
                if (current.getStaffId().equals(staffId)) {
                    return current;
                }
                current = current.nextWaiter;
            } while (current != Waiter.head);
        }
        return null;
    }

    private static void removePatronFromQueues(int patronId) {
        removeFromQueue(patronId, Patron.firstDineIn, Patron.lastDineIn, true);
        removeFromQueue(patronId, Patron.firstTakeout, Patron.lastTakeout, false);
    }

    private static void removeFromQueue(int patronId, Patron head, Patron tail, boolean isDineIn) {
        Patron current = head;
        Patron prev = null;

        while (current != null) {
            if (current.getId() == patronId) {
                if (prev == null) {
                    if (isDineIn)
                        Patron.firstDineIn = current.next;
                    else
                        Patron.firstTakeout = current.next;
                } else {
                    prev.next = current.next;
                }

                if (current == tail) {
                    if (isDineIn)
                        Patron.lastDineIn = prev;
                    else
                        Patron.lastTakeout = prev;
                }
                break;
            }
            prev = current;
            current = current.next;
        }
    }

    // RESPONSE BUILDING METHODS
    private static Map<String, Object> buildOrderResponse(Order order, Patron patron) {
        List<Map<String, Object>> itemsData = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("name", item.getName());
            itemMap.put("price", item.getPrice());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("category", item.getCategory());
            itemsData.add(itemMap);
        }

        String orderType = patron != null ? (patron.getServiceType() == 1 ? "Dine-in" : "Takeout") : "Unknown";
        String waiterName = patron != null && patron.getWaiter() != null ? patron.getWaiter().getName() : "Unassigned";

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getOrderId());
        response.put("patronId", order.getPatronId());
        response.put("totalAmount", order.getTotalAmount());
        response.put("items", itemsData);
        response.put("waiterName", waiterName);
        response.put("orderType", orderType);
        response.put("tableNumber", order.getTableNumber());
        response.put("isPaid", order.isPaid());
        return response;
    }

    private static Map<String, Object> getOrderReceiptDetails(Order order, Patron patron) {
        Waiter waiter = patron != null ? patron.getWaiter() : null;
        String orderType = patron != null ? (patron.getServiceType() == 1 ? "Dine-in" : "Takeout") : "Unknown";

        Map<String, Object> receipt = new HashMap<>();
        receipt.put("orderId", order.getOrderId());
        receipt.put("totalAmount", order.getTotalAmount());
        receipt.put("items", order.getItems());
        receipt.put("waiterName", waiter != null ? waiter.getName() : "Unassigned");
        receipt.put("orderType", orderType);
        receipt.put("tableNumber", patron != null ? patron.getTableId() : 0);
        receipt.put("timestamp", new Date());
        return receipt;
    }

    public static Order findOrderById(int orderId) {
        return allOrders.get(orderId);
    }

    public static List<Order> getAllOrders() {
        return new ArrayList<>(ordersList);
    }

    // INNER CLASSES
    public static class MenuItem {
        private String name;
        private double price;
        private String category;
        private String description;

        public MenuItem(String name, double price, String category, String description) {
            this.name = name;
            this.price = price;
            this.category = category;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public String getCategory() {
            return category;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class OrderItem {
        private int itemId;
        private String name;
        private double price;
        private int quantity;
        private String category;

        public OrderItem(int itemId, String name, double price, int quantity, String category) {
            this.itemId = itemId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.category = category;
        }

        public int getItemId() {
            return itemId;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        public String getCategory() {
            return category;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    public static class OrderItemRequest {
        private String name;
        private int quantity;

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}