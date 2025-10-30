import * as api from './api.js';
import * as ui from './ui.js';
import * as modals from './modals.js';
import * as orders from './orders.js';

// -----------------------------------------------------------------------------
//  Handles loading modal HTML and setting up functionalitities 
// -----------------------------------------------------------------------------
async function loadModals() {
    try {
        // Fetch the modal HTML from external file
        const response = await fetch('modals.html');
        if (!response.ok) throw new Error('Failed to fetch modals.html');
        const modalHtml = await response.text();
        
        // Find or create the modals container
        let modalsContainer = document.getElementById('modals-container');
        if (!modalsContainer) {
            modalsContainer = document.createElement('div');
            modalsContainer.id = 'modals-container';
            document.body.appendChild(modalsContainer);
        }
        
        // Insert the modal HTML into the container
        modalsContainer.innerHTML = modalHtml;
        setupModalFunctionality();
        
    } catch (error) {
        console.error('Failed to load modals:', error);
    }
}

function setupModalFunctionality() {
    setupModalEventListeners();
    setupCancelButtonListeners();
    setupTabNavigation();
    orders.setupOrderSelection();
    orders.setupMarkAsPaidButton();
}

// -----------------------------------------------------------------------------
//  Handles all click events for modals and buttons
// -----------------------------------------------------------------------------
function setupModalEventListeners() {
    document.addEventListener('click', (e) => {
        // --- Modal Open Buttons ---
        if (e.target.id === 'openTakeOrderModalBtn' || e.target.closest('#openTakeOrderModalBtn')) {
            e.preventDefault();
            modals.openTakeOrderModal();
        }
        if (e.target.id === 'openMarkAsPaidModalBtn' || e.target.closest('#openMarkAsPaidModalBtn')) {
            e.preventDefault();
            modals.openMarkAsPaidModal();
        }
        if (e.target.id === 'openViewReportsModalBtn' || e.target.closest('#openViewReportsModalBtn')) {
            e.preventDefault();
            modals.openViewReportsModal();
        }
        
        // --- Modal Close Buttons ---
        if (e.target.id === 'closeTakeOrderModal' || e.target.closest('#closeTakeOrderModal')) {
            e.preventDefault();
            modals.closeTakeOrderModal();
        }
        if (e.target.id === 'closeMarkAsPaidModal' || e.target.closest('#closeMarkAsPaidModal')) {
            e.preventDefault();
            modals.closeMarkAsPaidModal();
        }
        if (e.target.id === 'closeViewReportsModal' || e.target.closest('#closeViewReportsModal')) {
            e.preventDefault();
            modals.closeViewReportsModal();
        }
        if (e.target.id === 'closeReceiptModal' || e.target.closest('#closeReceiptModal')) {
            e.preventDefault();
            modals.closeReceiptModal();
        }
        
        // --- Action Buttons ---
        if (e.target.id === 'submitCustomerBtn' || e.target.closest('#submitCustomerBtn')) {
            e.preventDefault();
            modals.submitCustomer();
        }
        if (e.target.id === 'createOrderBtn' || e.target.closest('#createOrderBtn')) {
            e.preventDefault();
            orders.createOrder();
        }
        if (e.target.id === 'confirmPaymentBtn' || e.target.closest('#confirmPaymentBtn')) {
            e.preventDefault();
            orders.confirmPayment();
        }
        
        // --- Form Field Changes ---
        if (e.target.id === 'ordertype' || e.target.closest('#ordertype')) {
            modals.toggleGroupSizeVisibility();
        }
    });
    
    setupDirectButtonListeners();
}

// Setup direct button listeners as fallback
function setupDirectButtonListeners() {
    const takeOrderBtn = document.getElementById('openTakeOrderModalBtn');
    const markAsPaidBtn = document.getElementById('openMarkAsPaidModalBtn');
    const viewReportsBtn = document.getElementById('openViewReportsModalBtn');
    
    if (takeOrderBtn) takeOrderBtn.addEventListener('click', modals.openTakeOrderModal);
    if (markAsPaidBtn) markAsPaidBtn.addEventListener('click', modals.openMarkAsPaidModal);
    if (viewReportsBtn) viewReportsBtn.addEventListener('click', modals.openViewReportsModal);
}

// Handle cancel buttons in all modals
function setupCancelButtonListeners() {
    document.addEventListener('click', (e) => {
        if (e.target.classList.contains('cancel-btn')) {
            e.preventDefault();
            const modal = e.target.closest('.modal');
            if (modal) modal.style.display = 'none';
        }
    });
}

// Handle tab navigation in reports modal
function setupTabNavigation() {
    document.addEventListener('click', (e) => {
        if (e.target.classList.contains('tab-btn')) {
            const target = e.target.getAttribute("data-target");
            const contents = document.querySelectorAll(".tab-content");
            const tabs = document.querySelectorAll(".tab-btn");
            
            // Hide all tab contents and remove active class from all tabs
            contents.forEach(content => content.style.display = "none");
            tabs.forEach(t => t.classList.remove("active"));
            
            // Show the selected tab content and mark tab as active
            const targetContent = document.querySelector(`.${target}`);
            if (targetContent) {
                targetContent.style.display = "block";
                e.target.classList.add("active");
                loadTabData(target); // Load data for the selected tab
            }
        }
    });
}

// -----------------------------------------------------------------------------
// loads data from backend and updates UI
// -----------------------------------------------------------------------------
async function loadTabData(target) {
    try {
        let reportData;
        switch(target) {
            case 'overview-breakdown':
                reportData = await api.loadOverviewReport();
                ui.renderOverviewReport(reportData);
                break;
            case 'staff-breakdown':
                reportData = await api.loadStaffReport();
                ui.renderStaffReport(reportData);
                break;
            case 'menu-breakdown':
                reportData = await api.loadMenuReport();
                ui.renderMenuReport(reportData);
                break;
            case 'finance-breakdown':
                reportData = await api.loadFinanceReport();
                ui.renderFinanceReport(reportData);
                break;
        }
    } catch (error) {
        console.error(`Error loading ${target} report:`, error);
    }
}

// Main data loading function - loads all initial app data
async function loadAllData() {
    try {
        // Load menu items first since they're needed for order creation
        const menu = await api.loadMenu();
        ui.renderMenu(menu);
        orders.setupMenuEventListeners(); // Setup click handlers for menu items
    } catch (error) {
        console.error('Failed to load menu:', error);
        ui.showMenuError('Failed to load menu');
    }
    
    try {
        // Load multiple data sources in parallel for better performance
        const [tables, pendingOrders, pendingCounts, dailyReport, waiters] = await Promise.all([
            api.loadTables().catch(err => []),
            api.loadPendingOrders().catch(err => []),
            api.getPendingOrderCounts().catch(err => ({dineInPending: 0, takeoutPending: 0})),
            api.loadDailyReport().catch(err => ({})),
            api.loadWaiters().catch(err => [])
        ]);
        
        // Update UI with loaded data
        ui.renderTables(tables);
        ui.renderPendingOrders(pendingOrders);
        updateDashboardStats(dailyReport, pendingCounts, pendingOrders, waiters);
        
    } catch (error) {
        console.error('Failed to load some data:', error);
    }
}

// Update the dashboard statistics with fresh data
function updateDashboardStats(dailyReport, pendingCounts, pendingOrders, waiters) {
    // Update queue counts from backend
    const dineInPendingEl = document.getElementById('num_of_dine-in_queue');
    const takeoutPendingEl = document.getElementById('num_of_takeout_queue');
    if (dineInPendingEl) dineInPendingEl.textContent = pendingCounts.dineInPending || 0;
    if (takeoutPendingEl) takeoutPendingEl.textContent = pendingCounts.takeoutPending || 0;
    
    // Update order statistics
    const totalOrdersEl = document.getElementById('num_of_orders');
    const dineInOrdersEl = document.getElementById('num_of_dine_in');
    const takeoutOrdersEl = document.getElementById('num_of_takeout');
    if (totalOrdersEl) totalOrdersEl.textContent = dailyReport?.overview?.totalOrders || 0;
    if (dineInOrdersEl) dineInOrdersEl.textContent = dailyReport?.overview?.dineInOrders || 0;
    if (takeoutOrdersEl) takeoutOrdersEl.textContent = dailyReport?.overview?.takeoutOrders || 0;
    
    // Update pending orders count
    const pendingCount = document.getElementById('pending-count');
    if (pendingCount) pendingCount.textContent = pendingOrders?.length || 0;
    
    // Update active staff count
    const staffCountEl = document.getElementById('num_of_staff');
    if (staffCountEl) {
        staffCountEl.textContent = waiters?.length || 0;
    }
}

// -----------------------------------------------------------
// APPLICATION INITIALIZATION - Main app startup code
// ------------------------------------------------------------
document.addEventListener("DOMContentLoaded", async () => {
    console.log('--- Initializing application ---');
    
    // Show loading message for menu
    const menuContainer = document.getElementById('menu-container');
    if (menuContainer) {
        menuContainer.innerHTML = '<div class="loading">Loading menu, please wait...</div>';
    }
    
    // Load modals and setup functionality
    await loadModals();
    modals.setupModalCloseListeners();
    await loadAllData();
    
    console.log('--- Application initialized successfully ---');
    
    // Auto-refresh dashboard data every 3 seconds to keep the current updated stats 
    setInterval(async () => {
        try {
            const [pendingOrders, pendingCounts, dailyReport, waiters] = await Promise.all([
                api.loadPendingOrders().catch(err => []),
                api.getPendingOrderCounts().catch(err => ({dineInPending: 0, takeoutPending: 0})),
                api.loadDailyReport().catch(err => ({})),
                api.loadWaiters().catch(err => [])
            ]);
            
            ui.renderPendingOrders(pendingOrders);
            updateDashboardStats(dailyReport, pendingCounts, pendingOrders, waiters);
        } catch (error) {
            console.error('Failed to refresh dashboard data:', error);
        }
    }, 3000);

    // Auto-refresh table status every 5 seconds
    setInterval(async () => {
        try {
            const tables = await api.loadTables();
            ui.renderTables(tables);
        } catch (error) {
            console.error('Failed to refresh tables:', error);
        }
    }, 5000);
});