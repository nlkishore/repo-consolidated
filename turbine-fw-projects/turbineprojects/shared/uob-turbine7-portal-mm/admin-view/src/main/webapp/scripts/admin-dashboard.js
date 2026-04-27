/**
 * Admin Dashboard JavaScript
 * Modularized JavaScript for Admin Dashboard functionality
 */

// Tooltip Window Management
const AdminTooltip = {
    window: null,
    workarea: null,
    isVisible: false,

    init() {
        this.window = document.getElementById('tooltip-window');
        this.workarea = document.getElementById('workarea');
    },

    toggle() {
        this.isVisible = !this.isVisible;
        if (this.window) {
            this.window.style.display = this.isVisible ? 'block' : 'none';
            if (this.isVisible) {
                this.updatePosition(document.getElementById('tooltip-slider').value);
            }
        }
    },

    updatePosition(value) {
        if (!this.isVisible || !this.window || !this.workarea) return;
        
        const sliderValue = document.getElementById('slider-value');
        if (sliderValue) {
            sliderValue.textContent = value;
        }
        
        const workareaRect = this.workarea.getBoundingClientRect();
        const tooltipRect = this.window.getBoundingClientRect();
        
        // Calculate position within visible workarea
        const maxTop = workareaRect.top;
        const maxBottom = workareaRect.bottom - tooltipRect.height;
        const range = maxBottom - maxTop;
        const topPosition = maxTop + (range * (value / 100));
        
        // Keep tooltip within workarea bounds
        const finalTop = Math.max(maxTop, Math.min(topPosition, maxBottom));
        
        this.window.style.top = finalTop + 'px';
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
    AdminTooltip.init();
    
    // Keyboard shortcut to toggle tooltip (Ctrl+T)
    document.addEventListener('keydown', function(e) {
        if (e.ctrlKey && e.key === 't') {
            e.preventDefault();
            AdminTooltip.toggle();
        }
    });
});
