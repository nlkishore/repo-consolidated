import React from 'react'
import './Sidebar.css'

interface SidebarProps {
  activeSection: string
  onSectionChange: (sectionId: string) => void
}

const Sidebar: React.FC<SidebarProps> = ({ activeSection, onSectionChange }) => {
  const menuItems = [
    { id: 'user-details', icon: '👤', label: 'User Details' },
    { id: 'group-details', icon: '👥', label: 'Group Details' },
    { id: 'roles-details', icon: '🔑', label: 'Roles Details' },
    { id: 'permission-details', icon: '🔐', label: 'Permission Details' }
  ]

  return (
    <aside className="dashboard-sidebar">
      <nav className="sidebar-nav">
        <ul className="sidebar-menu">
          {menuItems.map((item) => (
            <li
              key={item.id}
              className={`sidebar-menu-item ${activeSection === item.id ? 'active' : ''}`}
            >
              <a
                href={`#${item.id}`}
                className="sidebar-link"
                onClick={(e) => {
                  e.preventDefault()
                  onSectionChange(item.id)
                  
                  // Scroll to section
                  setTimeout(() => {
                    const sectionElement = document.getElementById(item.id)
                    const workArea = document.getElementById('workarea')
                    
                    if (sectionElement && workArea) {
                      const sectionTop = sectionElement.offsetTop
                      const workAreaTop = workArea.offsetTop
                      const scrollPosition = sectionTop - workAreaTop - 20 // 20px padding
                      
                      workArea.scrollTo({
                        top: scrollPosition,
                        behavior: 'smooth'
                      })
                    }
                  }, 100)
                }}
              >
                <span className="menu-icon">{item.icon}</span>
                <span className="menu-text">{item.label}</span>
              </a>
            </li>
          ))}
        </ul>
      </nav>
    </aside>
  )
}

export default Sidebar
