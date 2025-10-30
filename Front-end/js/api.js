const API_BASE = 'http://localhost:8080/api';

// ------------------------------------------
// API FETCH 
// ------------------------------------------
async function apiFetch(endpoint, options = {}) {
    try {
        const response = await fetch(`${API_BASE}${endpoint}`, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error(`API call failed: ${endpoint}`, error);
        throw error;
    }
}

// ----------------------------------------------------------------
// DASHBOARD STATS API - For dashboard statistics and counts
// ----------------------------------------------------------------
export async function getQueueCounts() {
    return await apiFetch('/patrons/queue-counts');
}

export async function getPendingOrderCounts() {
    return await apiFetch('/patrons/pending-counts');
}

export async function loadWaiterStats() {
    return await apiFetch('/waiters');
}

// ------------------------------------------
// TABLES API - For table status and management
// ------------------------------------------
export async function loadTables() {
    return await apiFetch('/tables');
}

export async function getTableStatus() {
    return await apiFetch('/tables/status');
}

// -----------------------------------------------------------
// PATRONS API - For customer management and queue handling
// ------------------------------------------------------------
export async function loadPatronsWithoutOrders() {
    return await apiFetch('/patrons/without-orders');
}

export async function loadDineInPatronsWithOrders() {
    return await apiFetch('/patrons/with-orders/dinein');
}

export async function createPatron(patronData) {
    return await apiFetch('/patrons/add', {
        method: 'POST',
        body: JSON.stringify(patronData)
    });
}

export async function getPatronById(id) {
    return await apiFetch(`/patrons/${id}`);
}

export async function getAllPatrons() {
    return await apiFetch('/patrons');
}

export async function checkDineInAvailability(groupSize) {
    return await apiFetch(`/patrons/check-dinein-availability?groupSize=${groupSize}`);
}

// ------------------------------------------------------------
// WAITERS API - For staff management and availability
// ------------------------------------------------------------
export async function loadWaiters() {
    return await apiFetch('/waiters/detailed');
}

export async function checkWaiterAvailability() {
    return await apiFetch('/waiters/availability/check-dinein');
}

// ------------------------------------------
// MENU API - For menu items and categories
// ------------------------------------------
export async function loadMenu() {
    return await apiFetch('/menu/items-with-descriptions');
}

export async function loadMenuByCategory(category) {
    return await apiFetch(`/menu/categories/${category}`);
}

// -----------------------------------------------------
// ORDERS API - For order creation and management
// -------------------------------------------------------
export async function loadPendingOrders() {
    return await apiFetch('/orders/pending');
}

export async function loadUnpaidOrders() {
    return await apiFetch('/orders/unpaid');
}

export async function getOrderById(id) {
    return await apiFetch(`/orders/${id}`);
}

export async function getOrderByPatronId(patronId) {
    return await apiFetch(`/orders/patron/${patronId}`);
}

export async function createOrder(orderData) {
    return await apiFetch('/orders/create', {
        method: 'POST',
        body: JSON.stringify(orderData)
    });
}

export async function addItemsToOrder(orderId, items) {
    return await apiFetch(`/orders/${orderId}/add-items`, {
        method: 'POST',
        body: JSON.stringify(items)
    });
}

export async function markOrderAsPaid(orderId) {
    return await apiFetch(`/orders/${orderId}/mark-paid`, {
        method: 'POST'
    });
}

// ------------------------------------------
// REPORTS API - For analytics and reporting
//------------------------------------------
export async function loadDailyReport() {
    return await apiFetch('/reports/daily');
}

export async function loadOverviewReport() {
    return await apiFetch('/reports/overview');
}

export async function loadStaffReport() {
    return await apiFetch('/reports/staff');
}

export async function loadMenuReport() {
    return await apiFetch('/reports/menu');
}

export async function loadFinanceReport() {
    return await apiFetch('/reports/finance');
}