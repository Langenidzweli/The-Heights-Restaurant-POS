import * as api from './api.js';
import * as ui from './ui.js';
import * as orders from './orders.js';

// ----------------------------------------------------
//  Handles opening/closing modals and click outside
// ----------------------------------------------------
export function toggleModal(modalId, show) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = show ? "block" : "none";
    }
}

export function setupModalCloseListeners() {
    window.addEventListener("click", (event) => {
        const modals = [
            "takeOrderModal", "markAsPaidModal", "viewReportsModal", "receiptModal"
        ];
        
        // Close modal if user clicks outside of it
        modals.forEach(modalId => {
            const modal = document.getElementById(modalId);
            if (modal && event.target === modal) {
                toggleModal(modalId, false);
            }
        });
    });
}

// ------------------------------------------------
//  Handles customer creation and form logic
// ------------------------------------------------
export function toggleGroupSizeVisibility() {
    const orderType = document.getElementById("ordertype")?.value;
    const groupSizeContainer = document.getElementById("groupSizeContainer");
    
    // Only show group size for dine-in orders (type 1)
    if (groupSizeContainer && orderType) {
        groupSizeContainer.style.display = orderType === "1" ? "block" : "none";
    }
}

export async function submitCustomer() {
    const orderTypeSelect = document.getElementById("ordertype");
    const groupSizeSelect = document.getElementById("groupSize");
    
    if (!orderTypeSelect) return;
    
    const orderTypeValue = orderTypeSelect.value;
    if (!orderTypeValue) {
        alert("Please select an order type!");
        return;
    }
    
    let groupSize = 1;
    if (orderTypeValue === "1") {
        // Validate group size for dine-in orders
        if (!groupSizeSelect || !groupSizeSelect.value || parseInt(groupSizeSelect.value) < 1) {
            alert("Please enter a valid group size!");
            return;
        }
        groupSize = parseInt(groupSizeSelect.value);
        
        // Check if restaurant has space for this group
        try {
            const availability = await api.checkDineInAvailability(groupSize);
            if (!availability.canAccept) {
                alert(availability.message);
                return;
            }
        } catch (error) {
            console.error('Error checking availability:', error);
            alert('Error checking restaurant availability. Please try again.');
            return;
        }
    }
    
    try {
        // Create new customer in the system
        const data = await api.createPatron({ 
            orderType: parseInt(orderTypeValue),
            groupSize: groupSize
        });
        
        alert(`Customer created successfully! ID: ${data.id}`);
        
        // Reset the form after successful creation
        orderTypeSelect.value = "";
        if (groupSizeSelect) groupSizeSelect.value = "";
        const groupSizeContainer = document.getElementById("groupSizeContainer");
        if (groupSizeContainer) groupSizeContainer.style.display = "none";
        
        // Refresh table status to show new customer assignment
        const tables = await api.loadTables();
        ui.renderTables(tables);
        
    } catch (error) {
        console.error('Error creating customer:', error);
        // Show user-friendly error messages
        if (error.message && error.message.includes('restaurant fully booked')) {
            alert(error.message);
        } else {
            alert("Error creating customer: " + (error.message || 'Unknown error'));
        }
    }
}

// --------------------------------------
//  Handles customer selection UI
// -----------------------------------------
let isCustomerDropdownOpen = false;

export function setupCustomerDropdown() {
    const customDropdown = document.querySelector('.custom-select-dropdown');
    if (!customDropdown) return;
    
    const dropdownTrigger = customDropdown.querySelector('.dropdown-trigger');
    const selectedValue = customDropdown.querySelector('.selected-value');
    const dropdownOptions = customDropdown.querySelector('.dropdown-options');
    
    if (!dropdownTrigger || !selectedValue || !dropdownOptions) return;
    
    // Clear any existing event listeners by cloning and replacing
    const newTrigger = dropdownTrigger.cloneNode(true);
    dropdownTrigger.parentNode.replaceChild(newTrigger, dropdownTrigger);
    
    // Get the updated reference after replacement
    const updatedTrigger = customDropdown.querySelector('.dropdown-trigger');
    
    updatedTrigger.addEventListener('click', (e) => {
        e.stopPropagation();
        isCustomerDropdownOpen = !isCustomerDropdownOpen;
        customDropdown.classList.toggle('open');
        
        if (isCustomerDropdownOpen) {
            // Hide the action dropdowns when opening main dropdown
            hideCustomerActionDropdowns();
        }
    });
    
    // Handle radio button selections for new/update customer
    dropdownOptions.addEventListener('click', (e) => {
        const radio = e.target.closest('input[type="radio"]');
        if (radio) {
            const optionText = radio.parentElement.querySelector('.option-text').textContent;
            selectedValue.textContent = optionText;
            
            // Show the appropriate dropdown based on selection
            handleCustomerActionSelection(radio.value);
            
            // Keep main dropdown open for better UX
            customDropdown.classList.add('open');
            isCustomerDropdownOpen = true;
        }
    });
    
    // Close dropdown when clicking outside
    document.addEventListener('click', (e) => {
        if (!customDropdown.contains(e.target)) {
            customDropdown.classList.remove('open');
            isCustomerDropdownOpen = false;
            hideCustomerActionDropdowns();
        }
    });
}

function hideCustomerActionDropdowns() {
    const newCustomerContainer = document.getElementById('newCustomerDropdownContainer');
    const updateOrderContainer = document.getElementById('updateOrderDropdownContainer');
    if (newCustomerContainer) newCustomerContainer.style.display = 'none';
    if (updateOrderContainer) updateOrderContainer.style.display = 'none';
}

function handleCustomerActionSelection(action) {
    console.log('Customer action selected:', action);
    
    // Reset form and UI when switching between actions
    resetFormFields();
    ui.clearOrderItems();
    orders.resetCurrentOrder();
    
    // Show the appropriate dropdown based on selection
    if (action === "new") {
        document.getElementById('updateOrderDropdownContainer').style.display = 'none';
        showNewCustomerDropdown();
    } else if (action === "update") {
        document.getElementById('newCustomerDropdownContainer').style.display = 'none';
        showUpdateOrderDropdown();
    }
}

function resetFormFields() {
    const assignedWaiter = document.getElementById("assignedWaiter");
    const assignedTable = document.getElementById("assignedTable");
    const assignedTableLabel = document.querySelector('label[for="assignedTable"]');
    
    if (assignedWaiter) {
        assignedWaiter.value = "Select customer to get assigned waiter";
    }
    
    if (assignedTable && assignedTableLabel) {
        assignedTable.value = "";
        assignedTable.style.display = "none";
        assignedTableLabel.style.display = "none";
    }
    
    const createOrderBtn = document.getElementById("createOrderBtn");
    if (createOrderBtn) {
        createOrderBtn.textContent = "Create Order";
        createOrderBtn.dataset.orderId = "";
        createOrderBtn.dataset.patronId = "";
    }
}

async function showNewCustomerDropdown() {
    try {
        const patrons = await api.loadPatronsWithoutOrders();
        const dropdownContainer = document.getElementById('newCustomerDropdownContainer');
        const dropdown = document.getElementById('newCustomerDropdown');
        
        if (!dropdownContainer || !dropdown) return;
        
        dropdown.innerHTML = '<option value="">-- Select customer --</option>';
        
        if (!patrons || patrons.length === 0) {
            dropdown.innerHTML = '<option value="" disabled>No customers without orders found</option>';
        } else {
            // show dropdown with customers who don't have orders yet
            patrons.forEach(patron => {
                const orderType = patron.orderType === 1 ? "Dine-in" : "Takeout";
                const option = document.createElement('option');
                option.value = patron.id;
                option.textContent = `ID: ${patron.id} - ${orderType} - Group: ${patron.groupSize}`;
                option.dataset.patron = JSON.stringify(patron);
                dropdown.appendChild(option);
            });
            
            dropdown.onchange = function() {
                const selectedOption = this.options[this.selectedIndex];
                if (selectedOption.value) {
                    const patron = JSON.parse(selectedOption.dataset.patron);
                    completeCustomerSelection(patron, 'new');
                }
            };
        }
        
        dropdownContainer.style.display = 'block';
        
    } catch (error) {
        console.error('Failed to load customers without orders:', error);
        const dropdown = document.getElementById('newCustomerDropdown');
        if (dropdown) {
            dropdown.innerHTML = '<option value="" disabled>Error loading customers</option>';
        }
    }
}

async function showUpdateOrderDropdown() {
    try {
        const patrons = await api.loadDineInPatronsWithOrders();
        const dropdownContainer = document.getElementById('updateOrderDropdownContainer');
        const dropdown = document.getElementById('updateOrderDropdown');
        
        if (!dropdownContainer || !dropdown) return;
        
        dropdown.innerHTML = '<option value="">-- Select customer --</option>';
        
        if (!patrons || patrons.length === 0) {
            dropdown.innerHTML = '<option value="" disabled>No dine-in customers with orders found</option>';
        } else {
            // show dropdown with customers who have existing orders
            patrons.forEach(patron => {
                const option = document.createElement('option');
                option.value = patron.id;
                option.textContent = `ID: ${patron.id} - Table: ${patron.tableNumber || 'None'} - Waiter: ${patron.waiterName || 'None'}`;
                option.dataset.patron = JSON.stringify(patron);
                dropdown.appendChild(option);
            });
            
            dropdown.onchange = async function() {
                const selectedOption = this.options[this.selectedIndex];
                if (selectedOption.value) {
                    const patron = JSON.parse(selectedOption.dataset.patron);
                    await completeCustomerSelection(patron, 'update');
                }
            };
        }
        
        dropdownContainer.style.display = 'block';
        
    } catch (error) {
        console.error('Failed to load customers with existing orders:', error);
        const dropdown = document.getElementById('updateOrderDropdown');
        if (dropdown) {
            dropdown.innerHTML = '<option value="" disabled>Error loading customers</option>';
        }
    }
}

async function completeCustomerSelection(patron, actionType) {
    console.log('Completing customer selection:', patron, actionType);
    
    // Update UI to show selected customer
    const selectedValue = document.querySelector('.selected-value');
    if (selectedValue) {
        selectedValue.textContent = `${actionType === 'new' ? 'New Order' : 'Update Order'} - Customer ID: ${patron.id}`;
    }
    
    // Update assigned resources display
    updateAssignedResources(patron);
    
    // Set patron ID on create order button
    const createOrderBtn = document.getElementById("createOrderBtn");
    if (createOrderBtn) {
        createOrderBtn.dataset.patronId = patron.id;
    }
    
    // Close all dropdowns after selection
    const customDropdown = document.querySelector('.custom-select-dropdown');
    if (customDropdown) {
        customDropdown.classList.remove('open');
        isCustomerDropdownOpen = false;
    }
    hideCustomerActionDropdowns();
    
    // For update actions, load existing order items
    if (actionType === 'update') {
        try {
            await orders.loadExistingOrder(patron.id);
        } catch (error) {
            console.error('Failed to load existing order:', error);
            ui.clearOrderItems();
        }
    } else {
        // For new orders, start with empty order
        ui.clearOrderItems();
        orders.resetCurrentOrder();
    }
}

function updateAssignedResources(patron) {
    const assignedWaiter = document.getElementById("assignedWaiter");
    const assignedTable = document.getElementById("assignedTable");
    const assignedTableLabel = document.querySelector('label[for="assignedTable"]');
    
    if (assignedWaiter) {
        assignedWaiter.value = patron.waiterName || 'Waiter will be assigned';
    }
    
    if (assignedTable && assignedTableLabel) {
        if (patron.tableNumber && patron.tableNumber !== 0) {
            assignedTable.value = `Table ${patron.tableNumber}`;
            assignedTable.style.display = 'block';
            assignedTableLabel.style.display = 'block';
        } else {
            assignedTable.value = '';
            assignedTable.style.display = 'none';
            assignedTableLabel.style.display = 'none';
        }
    }
}

// -------------------------------------------
//  Handles the main order creation modal
// ------------------------------------------
export async function openTakeOrderModal() {
    const modal = document.getElementById('takeOrderModal');
    if (!modal) {
        alert('Take order modal not found. Please refresh the page.');
        return;
    }
    
    resetTakeOrderModal();
    setupCustomerDropdown();
    toggleModal("takeOrderModal", true);
}

export function resetTakeOrderModal() {
    // Reset all form fields to initial state
    resetFormFields();
    
    // Clear order items from UI
    ui.clearOrderItems();
    orders.resetCurrentOrder();
    
    // Reset dropdown displays
    const selectedValue = document.querySelector('.selected-value');
    if (selectedValue) selectedValue.textContent = '-- Select customer action --';
    
    hideCustomerActionDropdowns();
    
    // Reset dropdown contents
    const newCustomerDropdown = document.getElementById('newCustomerDropdown');
    const updateOrderDropdown = document.getElementById('updateOrderDropdown');
    if (newCustomerDropdown) newCustomerDropdown.innerHTML = '<option value="">-- Select customer --</option>';
    if (updateOrderDropdown) updateOrderDropdown.innerHTML = '<option value="">-- Select customer --</option>';
    
    const customDropdown = document.querySelector('.custom-select-dropdown');
    if (customDropdown) {
        customDropdown.classList.remove('open');
        isCustomerDropdownOpen = false;
    }
}

export function closeTakeOrderModal() {
    toggleModal("takeOrderModal", false);
}

// -----------------------------------------------------------
// Handles payment processing
// --------------------------------------------------------------
export function openMarkAsPaidModal() {
    toggleModal("markAsPaidModal", true);
    loadUnpaidOrdersForDropdown();
}

export function closeMarkAsPaidModal() {
    toggleModal("markAsPaidModal", false);
}

export async function loadUnpaidOrdersForDropdown() {
    const selectElement = document.getElementById('SelectOrderToMarkAsPaid');
    const displayCard = document.querySelector('.display-unpaid-card');
    
    if (!selectElement || !displayCard) return;
    
    try {
        const unpaidOrders = await api.loadUnpaidOrders();
        selectElement.innerHTML = '<option value="">-- Choose unpaid order --</option>';
        
        if (!unpaidOrders || unpaidOrders.length === 0) {
            displayCard.innerHTML = '<p>All orders are paid</p>';
            return;
        }
        
        // show dropdown with unpaid orders
        unpaidOrders.forEach(order => {
            const option = document.createElement('option');
            option.value = order.orderId;
            option.textContent = `Order #${order.orderId} - R ${order.totalAmount?.toFixed(2) || '0.00'} - Customer ID: ${order.patronId}`;
            selectElement.appendChild(option);
        });
        
        displayCard.innerHTML = `<p>${unpaidOrders.length} unpaid order(s) found</p>`;
        
    } catch (error) {
        console.error('Error loading unpaid orders:', error);
        displayCard.innerHTML = '<p>Error loading orders</p>';
    }
}

// --------------------------------------
// Handles receipt generation and display
// ---------------------------------------
export function openReceiptModal(orderData) {
    const modal = document.getElementById('receiptModal');
    if (!modal) return;
    ui.renderReceipt(orderData);
    toggleModal("receiptModal", true);
}

export function closeReceiptModal() {
    toggleModal("receiptModal", false);
}

// ----------------------------------------
//  Handles business reports and analytics
// -----------------------------------------
export async function openViewReportsModal() {
    toggleModal("viewReportsModal", true);
    await loadReportsData();
}

export function closeViewReportsModal() {
    toggleModal("viewReportsModal", false);
}

export async function loadReportsData() {
    try {
        const dailyReport = await api.loadDailyReport();
        ui.renderReports(dailyReport);
    } catch (error) {
        console.error('Failed to load reports data:', error);
        ui.showReportsError('Failed to load reports data');
    }
}