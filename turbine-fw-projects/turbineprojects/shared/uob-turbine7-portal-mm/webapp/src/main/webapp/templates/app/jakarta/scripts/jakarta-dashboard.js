/**
 * Jakarta UI Dashboard JavaScript
 * Modern Jakarta-styled UI functionality
 * Matches reference UI behavior
 */

// Section visibility management
var currentSection = 'user-details';
var showFormSummary = true;

// Show specific section
function showSection(sectionId) {
    // Hide all sections
    var sections = document.querySelectorAll('.jakarta-section');
    sections.forEach(function(section) {
        section.style.display = 'none';
    });
    
    // Show selected section
    var targetSection = document.getElementById(sectionId);
    if (targetSection) {
        targetSection.style.display = 'block';
        currentSection = sectionId;
        
        // Update active nav item
        updateActiveNavItem(sectionId);
        
        // Scroll to top of work area
        var workarea = document.getElementById('workarea');
        if (workarea) {
            workarea.scrollTop = 0;
        }
    }
}

// Update active navigation item
function updateActiveNavItem(sectionId) {
    var navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(function(item) {
        item.classList.remove('active');
        var link = item.querySelector('.nav-link');
        if (link && link.getAttribute('href') === '#' + sectionId) {
            item.classList.add('active');
        }
    });
}

// Toggle dropdown menu
function toggleDropdown() {
    var menu = document.getElementById('dropdown-menu');
    if (menu) {
        if (menu.style.display === 'none' || menu.style.display === '') {
            menu.style.display = 'block';
        } else {
            menu.style.display = 'none';
        }
    }
}

// Hide dropdown menu
function hideDropdown() {
    var menu = document.getElementById('dropdown-menu');
    if (menu) {
        menu.style.display = 'none';
    }
}

// Toggle form summary visibility
function toggleFormSummary() {
    showFormSummary = !showFormSummary;
    var summaryText = document.getElementById('form-summary-text');
    var workArea = document.getElementById('workarea');
    
    if (summaryText) {
        summaryText.textContent = showFormSummary ? 'Hide Form Summary' : 'Show Form Summary';
    }
    
    if (workArea) {
        var sections = workArea.querySelectorAll('.jakarta-section');
        if (showFormSummary) {
            // Show all sections
            sections.forEach(function(section) {
                section.style.display = 'block';
            });
        } else {
            // Hide all except first
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

// Search functions
function searchUsers() {
    var searchTerm = document.getElementById('user-search')?.value.toLowerCase() || '';
    var rows = document.querySelectorAll('#user-grid-body tr');
    rows.forEach(function(row) {
        var text = row.textContent.toLowerCase();
        row.style.display = text.includes(searchTerm) ? '' : 'none';
    });
}

function searchGroups() {
    var searchTerm = document.getElementById('group-search')?.value.toLowerCase() || '';
    var rows = document.querySelectorAll('#group-grid-body tr');
    rows.forEach(function(row) {
        var text = row.textContent.toLowerCase();
        row.style.display = text.includes(searchTerm) ? '' : 'none';
    });
}

function searchRoles() {
    var searchTerm = document.getElementById('role-search')?.value.toLowerCase() || '';
    var rows = document.querySelectorAll('#role-grid-body tr');
    rows.forEach(function(row) {
        var text = row.textContent.toLowerCase();
        row.style.display = text.includes(searchTerm) ? '' : 'none';
    });
}

function searchPermissions() {
    var searchTerm = document.getElementById('permission-search')?.value.toLowerCase() || '';
    var rows = document.querySelectorAll('#permission-grid-body tr');
    rows.forEach(function(row) {
        var text = row.textContent.toLowerCase();
        row.style.display = text.includes(searchTerm) ? '' : 'none';
    });
}

// Scroll to top
function scrollToTop() {
    var workArea = document.getElementById('workarea');
    if (workArea) {
        workArea.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Show default section
    showSection('user-details');
    
    // Close dropdown when clicking outside
    document.addEventListener('click', function(event) {
        var dropdown = document.getElementById('dropdown-widget');
        var menu = document.getElementById('dropdown-menu');
        var toggle = document.getElementById('dropdown-toggle');
        
        if (dropdown && menu && toggle && 
            !dropdown.contains(event.target)) {
            menu.style.display = 'none';
        }
    });
    
    // Handle keyboard shortcuts
    document.addEventListener('keydown', function(e) {
        // ESC to close dropdown
        if (e.key === 'Escape') {
            hideDropdown();
        }
    });
    
    console.log('Jakarta Dashboard initialized');
});
