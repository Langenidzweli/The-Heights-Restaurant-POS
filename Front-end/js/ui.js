import * as api from './api.js';

// -----------------------------------------------------
// Handles displaying all business reports and analytics
// ------------------------------------------------------
export function renderReports(dailyReport) {
    console.log('--- RENDER REPORTS START ---');
    console.log('Daily report structure:', Object.keys(dailyReport));
    renderOverviewReport(dailyReport.overview);
    renderStaffReport(dailyReport.staff);
    renderMenuReport(dailyReport.menu);
    renderFinanceReport(dailyReport.finance);
    console.log('--- RENDER REPORTS END ---');
}

export function renderOverviewReport(overview) {
    if (!overview) return;
    
    // Get all the overview stat elements
    const totalOrdersEl = document.querySelector('.total-orders_card h1');
    const totalRevenueEl = document.querySelector('.total-Revenue_card h1 span');
    const avgOrderValueEl = document.querySelector('.avg-Order-value_card h1 span');
    const dineInRateEl = document.querySelector('.dine-in-rate_card h1');
    
    // Update the overview statistics
    if (totalOrdersEl) totalOrdersEl.textContent = overview.totalOrders || 0;
    if (totalRevenueEl) totalRevenueEl.textContent = overview.totalRevenue?.toFixed(2) || '0.00';
    if (avgOrderValueEl) avgOrderValueEl.textContent = overview.averageOrderValue?.toFixed(2) || '0.00';
    if (dineInRateEl) dineInRateEl.textContent = `${overview.dineInRate?.toFixed(1) || '0'}%`;
    
    // Update order type breakdown section
    const orderTypeDetails = document.querySelector('.order-type-details');
    if (orderTypeDetails) {
        orderTypeDetails.innerHTML = `
            <h1>Order Type Breakdown</h1>
            <p class="order-type">Dine-In Orders <span>R ${overview.dineInRevenue?.toFixed(2) || '0.00'}</span></p>
            <p class="description">${overview.dineInOrders || 0} orders</p>
            <p class="order-type">Takeout Orders <span>R ${overview.takeoutRevenue?.toFixed(2) || '0.00'}</span></p>
            <p class="description">${overview.takeoutOrders || 0} orders</p>
        `;
    }
}

export function renderStaffReport(staff) {
    if (!staff || !staff.waiters) return;
    
    const tableBody = document.querySelector('.waiters-table-body');
    if (!tableBody) return;
    
    // Generate table rows for each waiter's performance
    tableBody.innerHTML = staff.waiters.map(waiter => `
        <tr>
            <td>
                ${waiter.waiterName || 'Unknown'}
                <span class="staff-id">${waiter.staffId || 'N/A'}</span>
            </td>
            <td>${waiter.dineInOrders || 0}</td>
            <td>${waiter.takeoutOrders || 0}</td>
            <td>R ${waiter.totalSales?.toFixed(2) || '0.00'}</td>
            <td>R ${waiter.commission?.toFixed(2) || '0.00'}</td>
            <td>
                <span class="waiter-availability ${waiter.status === 'Fully Booked' ? 'fully-booked' : 'available'}">
                    ${waiter.status || 'Unknown'}
                </span>
            </td>
        </tr>
    `).join('');
}

export function renderMenuReport(menu) {
    console.log('--- RENDER MENU REPORT START ---');
    console.log('Menu data received:', menu);
    
    if (!menu) {
        console.log('ERROR: No menu data provided');
        return;
    }
    
    if (!menu.items) {
        console.log('ERROR: menu.items is undefined');
        return;
    }
    
    console.log('Number of menu items to render:', menu.items.length);
    
    // Find the menu table body - this was causing issues before
    const menuTable = document.querySelector('.menu-breakdown table');
    console.log('Menu table found:', menuTable);
    
    let tableBody;
    if (menuTable) {
        tableBody = menuTable.querySelector('tbody');
        console.log('Table body found via table:', tableBody);
    } else {
        // Fallback: try to find any tbody in menu-breakdown
        tableBody = document.querySelector('.menu-breakdown tbody');
        console.log('Table body found via fallback:', tableBody);
    }
    
    if (!tableBody) {
        console.log('ERROR: Could not find table body in menu breakdown');
        console.log('Available elements in menu-breakdown:', document.querySelectorAll('.menu-breakdown *'));
        return;
    }
    
    // Make sure the table body is visible
    tableBody.style.display = 'table-row-group';
    console.log('Table body display set to:', tableBody.style.display);
    
    // Sort items by quantity sold (highest first) to show best-sellers first
    const sortedItems = [...menu.items].sort((a, b) => (b.quantitySold || 0) - (a.quantitySold || 0));
    console.log('Sorted items:', sortedItems);
    
    if (sortedItems.length === 0) {
        console.log('No items to display - showing empty message');
        tableBody.innerHTML = `
            <tr>
                <td colspan="5" class="no-data">No menu data available. Create and pay for some orders first.</td>
            </tr>
        `;
        return;
    }
    
    // Generate HTML for each menu item row
    const menuHTML = sortedItems.map(item => `
        <tr>
            <td>${item.itemName || 'Unknown'}</td>
            <td>${item.category || 'N/A'}</td>
            <td>${item.quantitySold || 0}</td>
            <td>R ${(item.revenue || 0).toFixed(2)}</td>
            <td>R ${(item.averagePrice || 0).toFixed(2)}</td>
        </tr>
    `).join('');
    
    console.log('Generated HTML:', menuHTML);
    
    // Render the menu items to the table
    tableBody.innerHTML = menuHTML;
    console.log('Menu report rendered successfully with', sortedItems.length, 'items');
    console.log('--- RENDER MENU REPORT END ---');
}

export function renderFinanceReport(finance) {
    if (!finance) return;
    
    // Get finance card elements
    const revenueEl = document.querySelector('.Revenue-card h1 span');
    const salaryEl = document.querySelector('.staff-salary-card h1 span');
    const incomeEl = document.querySelector('.total-income-card h1 span');
    
    // Update financial statistics
    if (revenueEl) revenueEl.textContent = finance.totalRevenue?.toFixed(2) || '0.00';
    if (salaryEl) salaryEl.textContent = finance.staffSalary?.toFixed(2) || '0.00';
    if (incomeEl) incomeEl.textContent = finance.netIncome?.toFixed(2) || '0.00';
}

// ------------------------------------------
//  Handles displaying restaurant table status
// --------------------------------------------
export function renderTables(tables) {
    const tableStatusDiv = document.querySelector('.table-status');
    if (!tableStatusDiv) return;
    
    tableStatusDiv.innerHTML = '';
    
    if (!tables || tables.length === 0) {
        tableStatusDiv.innerHTML = '<p class="no-tables">No tables available</p>';
        return;
    }
    
    // Create a card for each table showing its status
    tables.forEach(table => {
        const tableCard = document.createElement('div');
        tableCard.className = `table-card ${table.occupied ? 'occupied' : ''}`;
        
        tableCard.innerHTML = `
            <h3>Table ${table.tableNumber}</h3>
            <p>${table.tableSize} seats</p>
            <p><span class="availability">${table.occupied ? '' : ''}</span></p>
            ${table.occupied && table.patron ? 
                `<p class="occupied-by">Occupied by ID:${table.patron.id}</p>` : ''}
        `;
        
        tableStatusDiv.appendChild(tableCard);
    });
}

// --------------------------------------------------
//Handles displaying the restaurant menu by category
// ---------------------------------------------------
export function renderMenu(menuItems) {
    const menuCards = document.querySelector('.menu-cards');
    if (!menuCards) return;
    
    // Group menu items by category for organized display
    const itemsByCategory = {};
    menuItems.forEach(item => {
        if (!itemsByCategory[item.category]) {
            itemsByCategory[item.category] = [];
        }
        itemsByCategory[item.category].push(item);
    });
    
    let menuHTML = '';
    for (const [category, items] of Object.entries(itemsByCategory)) {
        const categoryClass = category.toLowerCase().replace(/\s+/g, '-');
        menuHTML += `
            <div class="${categoryClass}-card">
                <h2>${category}</h2>
                ${items.map(item => `
                    <div class="meal-item" data-name="${item.name}" data-price="${item.price}">
                        <h3 class="meal-heading">${item.name}</h3>
                        <p class="meal-details">${item.description}</p>
                        <p class="meal-amount">R ${item.price} 
                            <img class="plus-mg" src="../Img/add-svgrepo-com.svg" alt="add">
                        </p>
                    </div>
                `).join('')}
            </div>
        `;
    }
    
    menuCards.innerHTML = menuHTML;
}

// -------------------------------------------------------------------------
// ORDER ITEM DISPLAY & CONTROLS - showing order items with edit controls
// -------------------------------------------------------------------------
export function clearOrderItems() {
    const orderList = document.querySelector(".order-list");
    const overallTotalBlock = document.querySelector(".overall-total-block");
    
    if (!orderList || !overallTotalBlock) return;
    
    orderList.innerHTML = '<p class="empty-msg">No items added yet</p>';
    overallTotalBlock.style.display = "none";
}

export function renderCombinedOrderItems(existingItems, newItems, tempTotal) {
    const orderList = document.querySelector(".order-list");
    const overallTotalBlock = document.querySelector(".overall-total-block");
    const overallTotalEl = document.getElementById("overall-total");
    
    if (!orderList || !overallTotalBlock || !overallTotalEl) return;
    
    orderList.innerHTML = '';
    
    // Show empty message if no items
    if (existingItems.length === 0 && newItems.length === 0) {
        orderList.innerHTML = '<p class="empty-msg">No items added yet</p>';
        overallTotalBlock.style.display = "none";
        return;
    }
    
    // Render existing items (read-only - from backend)
    existingItems.forEach(item => {
        const orderItem = document.createElement("div");
        orderItem.classList.add("order-item", "existing-item");
        orderItem.innerHTML = `
            <div class="item-left">
                <h6>${item.name}</h6>
                <p>R ${item.price.toFixed(2)} each</p>
            </div>
            <div class="item-right">
                <span class="counter">${item.quantity}</span>
            </div>
        `;
        orderList.appendChild(orderItem);
    });
    
    // Render new items (with edit controls - being added now)
    newItems.forEach(item => {
        const orderItem = document.createElement("div");
        orderItem.classList.add("order-item", "new-item");
        orderItem.dataset.name = item.name;
        orderItem.dataset.price = item.price;
        orderItem.innerHTML = `
            <div class="item-left">
                <h6>${item.name}</h6>
                <p>R ${item.price.toFixed(2)} each</p>
            </div>
            <div class="item-right">
                <img class="decrease" src="../img/minus-svgrepo-com.svg" alt="decrease">
                <span class="counter">${item.quantity}</span>
                <img class="increase" src="../img/add-svgrepo-com.svg" alt="increase">
                <img class="delete" src="../img/bin-essential-trash-svgrepo-com.svg" alt="delete">
            </div>
        `;
        orderList.appendChild(orderItem);
    });
    
    // Show the total amount (temporary frontend calculation)
    overallTotalEl.textContent = tempTotal.toFixed(2);
    overallTotalBlock.style.display = "block";
}

export function renderExistingOrderItems(items, totalAmount) {
    const orderList = document.querySelector(".order-list");
    const overallTotalBlock = document.querySelector(".overall-total-block");
    const overallTotalEl = document.getElementById("overall-total");
    
    if (!orderList || !overallTotalBlock || !overallTotalEl) return;
    
    orderList.innerHTML = '';
    
    if (items.length === 0) {
        orderList.innerHTML = '<p class="empty-msg">No items added yet</p>';
        overallTotalBlock.style.display = "none";
        return;
    }
    
    // Render only existing items (no edit controls)
    items.forEach(item => {
        const orderItem = document.createElement("div");
        orderItem.classList.add("order-item", "existing-item");
        orderItem.innerHTML = `
            <div class="item-left">
                <h6>${item.name}</h6>
                <p>R ${item.price.toFixed(2)} each</p>
            </div>
            <div class="item-right">
                <span class="counter">${item.quantity}</span>
            </div>
        `;
        orderList.appendChild(orderItem);
    });
    
    overallTotalEl.textContent = totalAmount.toFixed(2);
    overallTotalBlock.style.display = "block";
}

// ----------------------------------------------------------
// PENDING ORDERS DISPLAY - Shows orders waiting for payment
// ----------------------------------------------------------
export function renderPendingOrders(orders) {
    const pendingList = document.getElementById('pending-orders-list');
    const pendingCount = document.getElementById('pending-count');
    const completionText = document.getElementById('completion');
    
    if (!pendingList || !pendingCount || !completionText) return;
    
    pendingCount.textContent = orders.length;
    
    if (orders.length === 0) {
        completionText.textContent = 'All orders are completed';
        pendingList.innerHTML = `
            <div class="empty-state">
                <img src="../img/checkmark-done-svgrepo-com.svg" alt="No orders">
                <p>No pending orders</p>
            </div>
        `;
        return;
    }
    
    completionText.textContent = 'Orders waiting for payment processing';
    
    // Generate HTML for each pending order
    pendingList.innerHTML = orders.map(order => `
        <div class="pending-order-item">
            <div class="order-header">
                <span class="order-id">Order #${order.orderId}</span>
                <span class="order-amount"> R${order.totalAmount.toFixed(2)}</span>
            </div>
            
            <div class="order-details">
                <div class="detail-item">
                    <img src="../img/user-svgrepo-com.svg" alt="Customer">
                    <span>Customer ID: ${order.patronId}</span>
                </div>
                
                <div class="detail-item">
                    <img src="../img/waiter-svgrepo-com.svg" alt="Waiter">
                    <span>${order.waiterName}</span>
                </div>
                
                <div class="detail-item">
                    <img src="../img/fork-and-knife-svgrepo-com.svg" alt="Type">
                    <span>${order.orderType}</span>
                </div>
                
                <div class="detail-item">
                    <img src="../img/shopping-bag-svgrepo-com.svg" alt="Items">
                    <span>${order.totalItems || order.items?.reduce((acc, item) => acc + item.quantity, 0) || 0} items</span>
                </div>
            </div>
            
            <div class="order-meta">
                <span>ID: ${order.patronId}</span>
                ${order.tableNumber ? `<span>Table ${order.tableNumber}</span>` : ''}
            </div>
        </div>
    `).join('');
}

// ---------------------------------------------
// Shows detailed order information for payment
// ---------------------------------------------
export function renderOrderDetails(container, orderData) {
    if (!container) return;
    
    container.innerHTML = `
        <div class="order-details-preview">
            <h4>Order #${orderData.orderId}</h4>
            <div class="detail-row">
                <span>Customer ID:</span>
                <span>${orderData.patronId}</span>
            </div>
            <div class="detail-row">
                <span>Total:</span>
                <span class="amount">R ${orderData.totalAmount.toFixed(2)}</span>
            </div>
            <div class="detail-row">
                <span>Items:</span>
                <span>${orderData.items ? orderData.items.reduce((acc, item) => acc + item.quantity, 0) : 0} items</span>
            </div>
            ${orderData.tableNumber ? `
            <div class="detail-row">
                <span>Table:</span>
                <span>${orderData.tableNumber}</span>
            </div>` : ''}
            ${orderData.waiterName ? `
            <div class="detail-row">
                <span>Waiter:</span>
                <span>${orderData.waiterName}</span>
            </div>` : ''}
        </div>
    `;
}

// ------------------------------------------------------
//  Generates and shows receipt for payment confirmation
// -------------------------------------------------------
export function renderReceipt(orderData) {
    const receiptOrderId = document.getElementById('receipt-order-id');
    const receiptDate = document.getElementById('receipt-date');
    const receiptType = document.getElementById('receipt-type');
    const receiptCustomer = document.getElementById('receipt-customer');
    const receiptWaiter = document.getElementById('receipt-waiter');
    const receiptTotal = document.getElementById('receipt-total');
    const itemsContainer = document.getElementById('receipt-items');
    
    if (!receiptOrderId || !receiptDate || !receiptType || !receiptCustomer || !receiptWaiter || !receiptTotal || !itemsContainer) return;
    
    // Fill in receipt header information
    receiptOrderId.textContent = orderData.orderId;
    receiptDate.textContent = new Date().toLocaleString('en-ZA', {
        day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
    });
    receiptType.textContent = orderData.orderType || '-';
    receiptCustomer.textContent = `Customer ID: ${orderData.patronId}`;
    receiptWaiter.textContent = orderData.waiterName || 'Unassigned';
    receiptTotal.textContent = `R ${orderData.totalAmount.toFixed(2)}`;
    
    // Generate receipt items list
    itemsContainer.innerHTML = orderData.items.map(item => `
        <div class="receipt-item">
            <div class="receipt-item-info">
                <div class="receipt-item-name">${item.quantity}x ${item.name}</div>
            </div>
            <div class="receipt-item-price">R ${(item.price * item.quantity).toFixed(2)}</div>
        </div>
    `).join('');
}

// --------------------------------------------------
// ACTIVE STAFF COUNT - Gets staff count from backend
// --------------------------------------------------
export async function updateActiveStaffCount() {
    try {
        const waiters = await api.loadWaiters();
        const staffCountEl = document.getElementById('num_of_staff');
        if (staffCountEl) {
            staffCountEl.textContent = waiters.length || 0;
        }
    } catch (error) {
        console.error('Failed to load active staff count:', error);
        const staffCountEl = document.getElementById('num_of_staff');
        if (staffCountEl) {
            staffCountEl.textContent = '0';
        }
    }
}

// ------------------------------------------------------
//  Shows user-friendly error messages
// ----------------------------------------------------
export function showReportsError(message) {
    const tabContents = document.querySelectorAll('.tab-content');
    tabContents.forEach(content => {
        content.innerHTML = `<div class="error-state"><p>${message}</p></div>`;
    });
}

export function showMenuError(message) {
    const menuCards = document.querySelector('.menu-cards');
    if (menuCards) {
        menuCards.innerHTML = `<div class="error-state"><p>${message}</p></div>`;
    }
}

export function showTableError(message) {
    const tableStatusDiv = document.querySelector('.table-status');
    if (tableStatusDiv) {
        tableStatusDiv.innerHTML = `<div class="error-state"><p>${message}</p></div>`;
    }
}