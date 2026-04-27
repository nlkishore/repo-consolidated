import React, { useState, useEffect, useRef } from 'react'
import './FloatingDropdown.css'

interface FloatingDropdownProps {
  activeSection: string
  showFormSummary: boolean
  onSectionChange: (sectionId: string) => void
  onToggleFormSummary: () => void
}

const FloatingDropdown: React.FC<FloatingDropdownProps> = ({
  activeSection,
  showFormSummary,
  onSectionChange,
  onToggleFormSummary
}) => {
  const [isOpen, setIsOpen] = useState(false)
  const dropdownRef = useRef<HTMLDivElement>(null)

  const menuItems = [
    { id: 'user-details', label: 'User Details' },
    { id: 'group-details', label: 'Group Details' },
    { id: 'roles-details', label: 'Roles Details' },
    { id: 'permission-details', label: 'Permission Details' }
  ]

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false)
      }
    }

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside)
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
    }
  }, [isOpen])

  const scrollToSection = (sectionId: string) => {
    const sectionElement = document.getElementById(sectionId)
    const workArea = document.getElementById('workarea')
    
    if (sectionElement && workArea) {
      // Get the position of the section relative to the work area
      const sectionRect = sectionElement.getBoundingClientRect()
      const workAreaRect = workArea.getBoundingClientRect()
      const scrollPosition = workArea.scrollTop + (sectionRect.top - workAreaRect.top) - 20 // 20px padding
      
      workArea.scrollTo({
        top: Math.max(0, scrollPosition),
        behavior: 'smooth'
      })
    }
  }

  const handleMenuItemClick = (sectionId: string) => {
    onSectionChange(sectionId)
    // Keep dropdown open - don't close it
    
    // Scroll to the selected section after DOM update
    setTimeout(() => {
      scrollToSection(sectionId)
    }, 150)
  }

  const scrollToTop = () => {
    const workArea = document.getElementById('workarea')
    if (workArea) {
      workArea.scrollTo({ 
        top: 0, 
        behavior: 'smooth' 
      })
    }
    // Keep dropdown open - don't close it
  }

  const handleToggleFormSummary = () => {
    onToggleFormSummary()
    // Close dropdown only when Hide Form Summary is clicked
    setIsOpen(false)
    
    // If hiding form summary, scroll to active section
    if (showFormSummary) {
      setTimeout(() => {
        scrollToSection(activeSection)
      }, 150)
    } else {
      // If showing form summary, scroll to top
      setTimeout(() => {
        scrollToTop()
      }, 150)
    }
  }

  return (
    <div className="floating-dropdown-widget" ref={dropdownRef}>
      <button
        className="floating-dropdown-toggle"
        onClick={() => setIsOpen(!isOpen)}
        aria-label="Toggle navigation menu"
      >
        <span className="dropdown-icon">☰</span>
        <span className="dropdown-label">Navigation</span>
        <span className={`dropdown-arrow ${isOpen ? 'open' : ''}`}>▼</span>
      </button>
      {isOpen && (
        <div className="floating-dropdown-menu">
          <ul className="dropdown-list">
            <li>
              <a
                href="#"
                onClick={(e) => {
                  e.preventDefault()
                  handleToggleFormSummary()
                }}
                className={showFormSummary ? 'active-toggle' : ''}
              >
                {showFormSummary ? 'Hide Form Summary' : 'Show Form Summary'} | Top
              </a>
            </li>
            <li className="dropdown-divider"></li>
            {menuItems.map((item) => (
              <li key={item.id}>
                <a
                  href={`#${item.id}`}
                  onClick={(e) => {
                    e.preventDefault()
                    handleMenuItemClick(item.id)
                  }}
                  className={activeSection === item.id ? 'active-item' : ''}
                >
                  {item.label}
                </a>
              </li>
            ))}
            <li className="dropdown-divider"></li>
            <li>
              <a
                href="#"
                onClick={(e) => {
                  e.preventDefault()
                  scrollToTop()
                }}
              >
                Top
              </a>
            </li>
          </ul>
        </div>
      )}
    </div>
  )
}

export default FloatingDropdown
