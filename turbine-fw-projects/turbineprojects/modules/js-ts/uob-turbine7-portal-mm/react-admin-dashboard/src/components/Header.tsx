import React from 'react'
import './Header.css'

interface HeaderProps {
  userName?: string
  onLogout?: () => void
}

const Header: React.FC<HeaderProps> = ({ userName = 'Admin', onLogout }) => {
  const handleLogout = (e: React.MouseEvent<HTMLAnchorElement>) => {
    e.preventDefault()
    if (onLogout) {
      onLogout()
    }
  }

  return (
    <header className="dashboard-header">
      <div className="header-content">
        <div className="header-left">
          <h1 className="header-title">Admin Dashboard</h1>
        </div>
        <div className="header-right">
          <span className="welcome-text">Welcome, {userName}</span>
          <a href="#" onClick={handleLogout} className="logout-link">Logout</a>
        </div>
      </div>
    </header>
  )
}

export default Header
