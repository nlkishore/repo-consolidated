/**
 * Admin Dashboard JavaScript
 * Modularized JavaScript for Admin Dashboard functionality
 */

// Floating Navigation Widget Management (Reference UI Style)
var floatingNavPositionPercent = 50; // Default position (50% from bottom)
var isFormSummaryVisible = true; // Track form summary visibility

// Toggle floating navigation menu
function toggleFloatingNav() {
    var menu = document.getElementById('floating-nav-menu');
    if (menu) {
        if (menu.style.display === 'none' || menu.style.display === '') {
            menu.style.display = 'block';
        } else {
            menu.style.display = 'none';
        }
    }
}

// Hide floating navigation menu
function hideFloatingNav() {
    var menu = document.getElementById('floating-nav-menu');
    if (menu) {
        menu.style.display = 'none';
    }
}

// Update floating nav widget position
function updateFloatingNavPosition(value) {
    var widget = document.getElementById('floating-nav-widget');
    var sliderValue = document.getElementById('slider-value');
    
    if (widget && sliderValue) {
        floatingNavPositionPercent = parseInt(value);
        
        // Calculate position based on slider (0-100%)
        // 0% = top of viewport, 100% = bottom of viewport
        var viewportHeight = window.innerHeight;
        var widgetHeight = widget.offsetHeight;
        var headerHeight = 60; // Header height
        
        // Available space for widget
        var maxTop = headerHeight + 20; // 20px padding from header
        var maxBottom = viewportHeight - widgetHeight - 20; // 20px padding from bottom
        
        // Calculate position: 0% = top, 100% = bottom
        var range = maxBottom - maxTop;
        var topPosition = maxTop + (range * (value / 100));
        
        // Keep within bounds
        topPosition = Math.max(maxTop, Math.min(topPosition, maxBottom));
        
        widget.style.top = topPosition + 'px';
        widget.style.bottom = 'auto';
        widget.style.right = '20px';
        
        sliderValue.textContent = value;
    }
}

// Toggle form summary visibility (Reference UI behavior)
function toggleFormSummary() {
    isFormSummaryVisible = !isFormSummaryVisible;
    var summaryText = document.getElementById('form-summary-text');
    var workArea = document.getElementById('workarea');
    
    if (summaryText) {
        summaryText.textContent = isFormSummaryVisible ? 'Hide Form Summary' : 'Show Form Summary';
    }
    
    if (workArea) {
        if (isFormSummaryVisible) {
            workArea.classList.remove('form-summary-hidden');
            // Show all sections
            var sections = workArea.querySelectorAll('.workarea-section');
            sections.forEach(function(section) {
                section.style.display = 'block';
            });
        } else {
            workArea.classList.add('form-summary-hidden');
            // Hide all sections except first
            var sections = workArea.querySelectorAll('.workarea-section');
            sections.forEach(function(section, index) {
                if (index === 0) {
                    section.style.display = 'block';
                } else {
                    section.style.display = 'none';
                }
            });
        }
    }
}

// Show all details
function showAllDetails() {
    var workArea = document.getElementById('workarea');
    if (workArea) {
        workArea.classList.remove('form-summary-hidden');
        var sections = workArea.querySelectorAll('.workarea-section');
        sections.forEach(function(section) {
            section.style.display = 'block';
        });
        isFormSummaryVisible = true;
        var summaryText = document.getElementById('form-summary-text');
        if (summaryText) {
            summaryText.textContent = 'Hide Form Summary';
        }
    }
}

// Update widget position on scroll/resize
function updateFloatingNavOnScroll() {
    var widget = document.getElementById('floating-nav-widget');
    if (widget) {
        updateFloatingNavPosition(floatingNavPositionPercent);
    }
}

// Scroll to top function
function scrollToTop() {
    var workArea = document.getElementById('workarea');
    if (workArea) {
        workArea.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    } else {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    }
}

// Show details function (can be customized)
function showDetails() {
    // Toggle visibility of detailed information
    // For now, just scroll to first section
    scrollToSection('user-details');
}

// Scroll to section smoothly
function scrollToSection(sectionId) {
    var section = document.getElementById(sectionId);
    if (section) {
        var headerOffset = 80; // Account for header
        var elementPosition = section.getBoundingClientRect().top;
        var offsetPosition = elementPosition + window.pageYOffset - headerOffset;

        window.scrollTo({
            top: offsetPosition,
            behavior: 'smooth'
        });
        
        // Update active sidebar item
        updateActiveSidebarItem(sectionId);
    }
}

// Update active sidebar item
function updateActiveSidebarItem(sectionId) {
    var sidebarItems = document.querySelectorAll('.admin-sidebar nav li');
    sidebarItems.forEach(function(item) {
        item.classList.remove('active');
        var link = item.querySelector('a');
        if (link && link.getAttribute('href') === '#' + sectionId) {
            item.classList.add('active');
        }
    });
}

// Keep floating nav widget visible on scroll/resize
function keepFloatingNavVisible() {
    var widget = document.getElementById('floating-nav-widget');
    var workArea = document.getElementById('workarea');
    
    if (widget) {
        // Update position on window resize
        window.addEventListener('resize', function() {
            updateFloatingNavPosition(floatingNavPositionPercent);
        });
        
        // Close menu when clicking outside
        document.addEventListener('click', function(event) {
            var menu = document.getElementById('floating-nav-menu');
            var toggle = document.getElementById('floating-nav-toggle');
            if (menu && toggle && 
                !menu.contains(event.target) && 
                !toggle.contains(event.target)) {
                menu.style.display = 'none';
            }
        });
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Set initial floating nav position
    updateFloatingNavPosition(50);
    
    // Keep floating nav visible
    keepFloatingNavVisible();
    
    // Handle section visibility for active state
    var observer = new IntersectionObserver(function(entries) {
        entries.forEach(function(entry) {
            if (entry.isIntersecting) {
                updateActiveSidebarItem(entry.target.id);
            }
        });
    }, {
        root: document.getElementById('workarea'),
        threshold: 0.3
    });
    
    // Observe all sections
    var sections = document.querySelectorAll('.workarea-section');
    sections.forEach(function(section) {
        observer.observe(section);
    });
});

// Tooltip Window Management
const AdminTooltip = {
    window: null,
    workarea: null,
    isVisible: false,

    init() {
        this.window = document.getElementById('tooltip-window');
        this.workarea = document.getElementById('workarea');
        this.isVisible = true; // Always visible as overlay
        
        // Ensure floating nav widget is visible
        if (this.window) {
            this.window.style.display = 'block';
            this.window.style.visibility = 'visible';
            this.window.style.opacity = '1';
            this.window.style.position = 'fixed'; // Fixed for floating widget
            this.window.style.right = '20px';
            this.window.style.zIndex = '2000';
            
            // Set initial position
            const slider = document.getElementById('nav-slider');
            if (slider) {
                this.updatePosition(slider.value);
            } else {
                // Default position if slider not found
                this.window.style.bottom = '20px';
                this.window.style.right = '20px';
            }
        }
        
        // Ensure sidebar is visible
        const sidebar = document.querySelector('.admin-sidebar');
        if (sidebar) {
            sidebar.style.display = 'flex';
            sidebar.style.visibility = 'visible';
        }
    },

    toggle() {
        // Tooltip is always visible, but can be toggled if needed
        this.isVisible = !this.isVisible;
        if (this.window) {
            this.window.style.display = this.isVisible ? 'block' : 'none';
            if (this.isVisible) {
                this.updatePosition(document.getElementById('tooltip-slider').value);
            }
        }
    },

    updatePosition(value) {
        if (!this.window || !this.workarea) return;
        
        const sliderValue = document.getElementById('slider-value');
        if (sliderValue) {
            sliderValue.textContent = value;
        }
        
        // Since tooltip is now absolute (relative to work area), calculate position differently
        const workareaHeight = this.workarea.offsetHeight;
        const tooltipHeight = this.window.offsetHeight;
        const maxTop = 20; // 20px padding from top
        const maxBottom = workareaHeight - tooltipHeight - 20; // 20px padding from bottom
        const range = maxBottom - maxTop;
        
        // 0% = bottom, 100% = top (inverted for slider)
        const topPosition = maxBottom - (range * (value / 100));
        
        // Keep tooltip within workarea bounds
        const finalTop = Math.max(maxTop, Math.min(topPosition, maxBottom));
        
        this.window.style.top = finalTop + 'px';
        this.window.style.bottom = 'auto';
        this.window.style.right = '20px';
    }
};

// Navigation Management
const AdminNavigation = {
    scrollToSection(sectionId) {
        const section = document.getElementById(sectionId);
        if (section) {
            section.scrollIntoView({ behavior: 'smooth', block: 'start' });
            
            // Update active nav item
            document.querySelectorAll('.admin-sidebar nav li').forEach(li => {
                li.classList.remove('active');
            });
            const activeLink = document.querySelector(`.admin-sidebar nav li a[href="#${sectionId}"]`);
            if (activeLink) {
                activeLink.closest('li')?.classList.add('active');
            }
        }
    }
};

// Search Management
const AdminSearch = {
    searchUsers() {
        const searchTerm = document.getElementById('user-search')?.value || '';
        console.log('Searching users:', searchTerm);
        AdminDataLoader.loadUsers(searchTerm);
    },

    searchGroups() {
        const searchTerm = document.getElementById('group-search')?.value || '';
        console.log('Searching groups:', searchTerm);
        AdminDataLoader.loadGroups(searchTerm);
    },

    searchRoles() {
        const searchTerm = document.getElementById('role-search')?.value || '';
        console.log('Searching roles:', searchTerm);
        AdminDataLoader.loadRoles(searchTerm);
    },

    searchPermissions() {
        const searchTerm = document.getElementById('permission-search')?.value || '';
        console.log('Searching permissions:', searchTerm);
        AdminDataLoader.loadPermissions(searchTerm);
    }
};

// Data Loader (AJAX)
const AdminDataLoader = {
    async loadUsers(searchTerm) {
        const tbody = document.getElementById('user-grid-body');
        if (!tbody) return;
        
        tbody.innerHTML = '<tr><td colspan="6">Loading...</td></tr>';
        
        try {
            // TODO: Replace with actual API endpoint
            const response = await fetch(`/api/admin/users?search=${encodeURIComponent(searchTerm)}`);
            const data = await response.json();
            
            if (data && data.length > 0) {
                tbody.innerHTML = data.map(user => `
                    <tr>
                        <td>${user.id || ''}</td>
                        <td>${user.loginName || ''}</td>
                        <td>${user.firstName || ''}</td>
                        <td>${user.lastName || ''}</td>
                        <td>${user.email || ''}</td>
                        <td>
                            <a href="/app/user,FluxUserForm.vm?username=${user.loginName}">Edit</a>
                        </td>
                    </tr>
                `).join('');
            } else {
                tbody.innerHTML = '<tr><td colspan="6">No users found</td></tr>';
            }
        } catch (error) {
            console.error('Error loading users:', error);
            tbody.innerHTML = '<tr><td colspan="6">Error loading users</td></tr>';
        }
    },

    async loadGroups(searchTerm) {
        const tbody = document.getElementById('group-grid-body');
        if (!tbody) return;
        
        tbody.innerHTML = '<tr><td colspan="3">Loading...</td></tr>';
        
        try {
            // TODO: Replace with actual API endpoint
            const response = await fetch(`/api/admin/groups?search=${encodeURIComponent(searchTerm)}`);
            const data = await response.json();
            
            if (data && data.length > 0) {
                tbody.innerHTML = data.map(group => `
                    <tr>
                        <td>${group.id || ''}</td>
                        <td>${group.name || ''}</td>
                        <td>
                            <a href="/app/group,FluxGroupForm.vm?groupId=${group.id}">Edit</a>
                        </td>
                    </tr>
                `).join('');
            } else {
                tbody.innerHTML = '<tr><td colspan="3">No groups found</td></tr>';
            }
        } catch (error) {
            console.error('Error loading groups:', error);
            tbody.innerHTML = '<tr><td colspan="3">Error loading groups</td></tr>';
        }
    },

    async loadRoles(searchTerm) {
        const tbody = document.getElementById('role-grid-body');
        if (!tbody) return;
        
        tbody.innerHTML = '<tr><td colspan="3">Loading...</td></tr>';
        
        try {
            // TODO: Replace with actual API endpoint
            const response = await fetch(`/api/admin/roles?search=${encodeURIComponent(searchTerm)}`);
            const data = await response.json();
            
            if (data && data.length > 0) {
                tbody.innerHTML = data.map(role => `
                    <tr>
                        <td>${role.id || ''}</td>
                        <td>${role.name || ''}</td>
                        <td>
                            <a href="/app/role,FluxRoleForm.vm?roleId=${role.id}">Edit</a>
                        </td>
                    </tr>
                `).join('');
            } else {
                tbody.innerHTML = '<tr><td colspan="3">No roles found</td></tr>';
            }
        } catch (error) {
            console.error('Error loading roles:', error);
            tbody.innerHTML = '<tr><td colspan="3">Error loading roles</td></tr>';
        }
    },

    async loadPermissions(searchTerm) {
        const tbody = document.getElementById('permission-grid-body');
        if (!tbody) return;
        
        tbody.innerHTML = '<tr><td colspan="3">Loading...</td></tr>';
        
        try {
            // TODO: Replace with actual API endpoint
            const response = await fetch(`/api/admin/permissions?search=${encodeURIComponent(searchTerm)}`);
            const data = await response.json();
            
            if (data && data.length > 0) {
                tbody.innerHTML = data.map(permission => `
                    <tr>
                        <td>${permission.id || ''}</td>
                        <td>${permission.name || ''}</td>
                        <td>
                            <a href="/app/permission,FluxPermissionForm.vm?permissionId=${permission.id}">Edit</a>
                        </td>
                    </tr>
                `).join('');
            } else {
                tbody.innerHTML = '<tr><td colspan="3">No permissions found</td></tr>';
            }
        } catch (error) {
            console.error('Error loading permissions:', error);
            tbody.innerHTML = '<tr><td colspan="3">Error loading permissions</td></tr>';
        }
    }
};

// Global functions for inline event handlers
function scrollToSection(sectionId) {
    AdminNavigation.scrollToSection(sectionId);
}

function searchUsers() {
    AdminSearch.searchUsers();
}

function searchGroups() {
    AdminSearch.searchGroups();
}

function searchRoles() {
    AdminSearch.searchRoles();
}

function searchPermissions() {
    AdminSearch.searchPermissions();
}

function updateTooltipPosition(value) {
    AdminTooltip.updatePosition(value);
}

function toggleTooltip() {
    AdminTooltip.toggle();
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Force sidebar visibility
    var sidebar = document.querySelector('.admin-sidebar');
    if (sidebar) {
        sidebar.style.display = 'flex';
        sidebar.style.visibility = 'visible';
        sidebar.style.opacity = '1';
        sidebar.style.width = '250px';
        sidebar.style.minWidth = '250px';
        sidebar.style.maxWidth = '250px';
        console.log('Sidebar initialized and forced visible');
    } else {
        console.error('Sidebar element not found!');
    }
    
    // Force floating nav widget visibility
    var floatingNav = document.getElementById('floating-nav-widget');
    if (floatingNav) {
        floatingNav.style.display = 'block';
        floatingNav.style.visibility = 'visible';
        floatingNav.style.opacity = '1';
        floatingNav.style.position = 'fixed'; // Fixed for floating widget
        floatingNav.style.right = '20px';
        floatingNav.style.zIndex = '2000';
        // Position will be set by updateFloatingNavPosition
        updateFloatingNavPosition(50); // Set initial position
        console.log('Floating navigation widget initialized');
    } else {
        console.error('Floating navigation widget element not found!');
    }
    
    AdminTooltip.init();
    
    // Keyboard shortcut to toggle tooltip (Ctrl+T)
    document.addEventListener('keydown', function(e) {
        if (e.ctrlKey && e.key === 't') {
            e.preventDefault();
            AdminTooltip.toggle();
        }
    });
    
    console.log('Admin Dashboard JavaScript initialized');
});
