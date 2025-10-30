import * as api from './api.js';
import * as ui from './ui.js';
import * as modals from './modals.js';

// ----------------------------------------
//  Tracks current order items and IDs
// ----------------------------------------
let existingOrderItems = [];  // Items already in the order from backend
let newOrderItems = [];       // New items being added in current session
let currentOrderId = null;    // Current order ID if updating existing order
let currentCustomerId = null; // Current customer ID

export function resetCurrentOrder() {
    existingOrderItems = [];
    newOrderItems = [];
    currentOrderId = null;
    currentCustomerId = null;
}

export function getCurrentOrderState() {
    return {
        existingItems: [...existingOrderItems],
        newItems: [...newOrderItems],
        orderId: currentOrderId,
        customerId: currentCustomerId
    };
}

// -----------------------------------------
// Handles adding items from menu to order
// -----------------------------------------
export function setupMenuEventListeners() {
    // Use event delegation for dynamic menu items (better performance)
    document.addEventListener('click', (e) => {
        if (e.target.classList.contains('plus-mg') || e.target.closest('.plus-mg')) {
            const mealItem = e.target.closest('.meal-item');
            if (mealItem) {
                const name = mealItem.dataset.name;
                const price = parseFloat(mealItem.dataset.price);
                addMenuItemToOrder(name, price);
            }
        }
    });
}

function addMenuItemToOrder(name, price) {
    // Check if item already exists in new items to avoid duplicates
    const existingNewItem = newOrderItems.find(item => item.name === name);
    
    if (existingNewItem) {
        // If item exists, just increase the quantity
        existingNewItem.quantity += 1;
    } else {
        // Add new item to the order
        newOrderItems.push({
            name: name,
            price: price,
            quantity: 1
        });
    }
    
    updateOrderDisplay();
}

function updateOrderDisplay() {
    // Combine existing and new items for display purposes only
    const allItems = [...existingOrderItems, ...newOrderItems];
    
    // TEMPORARY frontend calculation for UI responsiveness only
    // Backend will calculate the real total when order is saved
    const tempTotal = allItems.reduce((total, item) => total + (item.price * item.quantity), 0);
    
    // Render combined items to the UI
    ui.renderCombinedOrderItems(existingOrderItems, newOrderItems, tempTotal);
    setupItemControls();
}

function setupItemControls() {
    // Only setup controls for new items (existing items are read-only from backend)
    document.querySelectorAll('.order-item.new-item').forEach(item => {
        const name = item.dataset.name;
        const increaseBtn = item.querySelector('.increase');
        const decreaseBtn = item.querySelector('.decrease');
        const deleteBtn = item.querySelector('.delete');
        const counter = item.querySelector('.counter');
        
        // Remove existing listeners and reattach (avoid duplicate listeners)
        const newIncrease = increaseBtn.cloneNode(true);
        const newDecrease = decreaseBtn.cloneNode(true);
        const newDelete = deleteBtn.cloneNode(true);
        
        increaseBtn.parentNode.replaceChild(newIncrease, increaseBtn);
        decreaseBtn.parentNode.replaceChild(newDecrease, decreaseBtn);
        deleteBtn.parentNode.replaceChild(newDelete, deleteBtn);
        
        // Add new listeners for quantity controls
        newIncrease.addEventListener('click', () => {
            const orderItem = newOrderItems.find(i => i.name === name);
            if (orderItem) {
                orderItem.quantity++;
                counter.textContent = orderItem.quantity;
                updateOrderDisplay();
            }
        });
        
        newDecrease.addEventListener('click', () => {
            const orderItem = newOrderItems.find(i => i.name === name);
            if (orderItem && orderItem.quantity > 1) {
                orderItem.quantity--;
                counter.textContent = orderItem.quantity;
                updateOrderDisplay();
            }
        });
        
        newDelete.addEventListener('click', () => {
            // Remove item from new items array
            newOrderItems = newOrderItems.filter(i => i.name !== name);
            updateOrderDisplay();
            
            // Show empty message if no items left
            if (existingOrderItems.length === 0 && newOrderItems.length === 0) {
                const orderList = document.querySelector(".order-list");
                if (orderList) {
                    orderList.innerHTML = '<p class="empty-msg">No items added yet</p>';
                }
            }
        });
    });
}

// ---------------------------------------------------
// BACKEND AUTHORITATIVE (Backend calculates totals)
// ---------------------------------------------------
export async function createOrder() {
    const createOrderBtn = document.getElementById('createOrderBtn');
    const patronId = createOrderBtn?.dataset.patronId;
    const orderId = createOrderBtn?.dataset.orderId;
    
    // Validation checks
    if (!patronId) {
        alert('Please select a customer first!');
        return;
    }
    
    if (newOrderItems.length === 0 && orderId) {
        alert('Please add at least one new item to update the order!');
        return;
    }
    
    if (newOrderItems.length === 0 && !orderId) {
        alert('Please add at least one item to the order!');
        return;
    }
    
    try {
        let result;
        
        if (orderId) {
            // Update existing order - BACKEND CALCULATES TOTALS
            console.log('Updating existing order:', orderId, 'with items:', newOrderItems);
            result = await api.addItemsToOrder(orderId, newOrderItems);
            alert(`Order #${orderId} updated successfully!`);
        } else {
            // Create new order - BACKEND CALCULATES TOTALS
            console.log('Creating new order for patron:', patronId, 'with items:', newOrderItems);
            result = await api.createOrder({
                patronId: parseInt(patronId),
                items: newOrderItems
            });
            alert(`Order created successfully! Order ID: ${result.orderId}`);
        }
        
        // Close modal and refresh all data
        modals.closeTakeOrderModal();
        await refreshDataAfterOrder();
        resetCurrentOrder();
        
    } catch (error) {
        console.error('Error creating/updating order:', error);
        alert('Failed to create/update order: ' + error.message);
    }
}

async function refreshDataAfterOrder() {
    try {
        // Refresh both pending orders and table status after order creation
        const [pendingOrders, tables] = await Promise.all([
            api.loadPendingOrders(),
            api.loadTables()
        ]);
        
        ui.renderPendingOrders(pendingOrders);
        ui.renderTables(tables);
        
    } catch (error) {
        console.error('Error refreshing data after order:', error);
    }
}

// ---------------------------------------------------------
// EXISTING ORDER MANAGEMENT (Backend provides real data)
// -------------------------------------------------------
export async function loadExistingOrder(patronId) {
    try {
        // Get  order data from backend 
        const orderData = await api.getOrderByPatronId(patronId);
        
        if (orderData && orderData.items) {
            // Store backend data in our state
            currentOrderId = orderData.orderId;
            currentCustomerId = patronId;
            existingOrderItems = [...orderData.items]; // Store existing items from backend
            newOrderItems = []; // Reset new items for current session
            
            // Update UI button state
            const createOrderBtn = document.getElementById("createOrderBtn");
            if (createOrderBtn) {
                createOrderBtn.textContent = "Update Order";
                createOrderBtn.dataset.orderId = orderData.orderId;
                createOrderBtn.dataset.patronId = patronId;
            }
            
            // Display combined items (existing from backend + empty new items)
            updateOrderDisplay();
            
            console.log('Existing order loaded from backend:', orderData);
        }
    } catch (error) {
        console.error('Error loading existing order from backend:', error);
        throw error;
    }
}

// ---------------------------------------------------
// PAYMENT PROCESSING  (Backend handles all payment)
// --------------------------------------------------
export function setupOrderSelection() {
    const selectElement = document.getElementById('SelectOrderToMarkAsPaid');
    const displayCard = document.querySelector('.display-unpaid-card');
    
    if (!selectElement || !displayCard) return;
    
    selectElement.addEventListener('change', async function() {
        const selectedOrderId = this.value;
        
        if (!selectedOrderId) {
            displayCard.innerHTML = '<p>Select an order to view details</p>';
            return;
        }
        
        try {
            // Get authoritative order data from backend for display
            const orderData = await api.getOrderById(selectedOrderId);
            ui.renderOrderDetails(displayCard, orderData);
        } catch (error) {
            console.error('Error fetching order details from backend:', error);
            displayCard.innerHTML = '<p>Error loading order details</p>';
        }
    });
}

export function setupMarkAsPaidButton() {
    const markBtn = document.querySelector('#markAsPaidModal .mark-btn');
    
    if (!markBtn) return;
    
    // Remove existing listener and reattach (clean slate)
    const newMarkBtn = markBtn.cloneNode(true);
    markBtn.parentNode.replaceChild(newMarkBtn, markBtn);
    
    newMarkBtn.addEventListener('click', async () => {
        const selectedOrderId = document.getElementById('SelectOrderToMarkAsPaid')?.value;
        
        if (!selectedOrderId) {
            alert('Please select an order first');
            return;
        }
        
        try {
            // Get authoritative order data from backend for receipt generation
            const orderData = await api.getOrderById(selectedOrderId);
            const confirmBtn = document.getElementById('confirmPaymentBtn');
            if (confirmBtn) {
                confirmBtn.dataset.orderId = selectedOrderId;
            }
            modals.openReceiptModal(orderData);
        } catch (error) {
            console.error('Error fetching order details from backend:', error);
            alert('Failed to load order details');
        }
    });
}

export async function confirmPayment() {
    const confirmBtn = document.getElementById('confirmPaymentBtn');
    const orderId = confirmBtn?.dataset.orderId;
    
    if (!orderId) {
        alert('No order selected for payment');
        return;
    }
    
    try {
        // Backend handles all payment logic and calculations
        const result = await api.markOrderAsPaid(orderId);
        alert('Payment confirmed successfully!');
        
        // Close payment modals
        modals.closeReceiptModal();
        modals.closeMarkAsPaidModal();
        
        // Refresh data from backend after payment
        const orders = await api.loadPendingOrders();
        ui.renderPendingOrders(orders);
        
        const tables = await api.loadTables();
        ui.renderTables(tables);
        
    } catch (error) {
        console.error('Payment error:', error);
        alert('Failed to process payment: ' + error.message);
    }
}