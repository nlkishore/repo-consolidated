import React, { useState, useEffect } from 'react'
import Dashboard from './components/Dashboard'
import Login from './components/Login'
import './App.css'

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null)

  useEffect(() => {
    checkAuth()
  }, [])

  const checkAuth = async () => {
    try {
      const response = await fetch('/api/auth/check', {
        method: 'GET',
        credentials: 'include' // Include cookies for session
      })
      
      // Check if response is ok (status 200-299)
      if (!response.ok) {
        // 404 means endpoint doesn't exist (backend not deployed or wrong URL)
        // 401 means not authenticated (expected for first visit)
        if (response.status === 404) {
          console.warn('Auth endpoint not found. Backend may not be running or deployed.')
          setIsAuthenticated(false)
          return
        }
        // For other errors, try to parse error message
        try {
          const errorData = await response.json()
          console.warn('Auth check failed:', errorData.message || response.statusText)
        } catch {
          console.warn('Auth check failed:', response.status, response.statusText)
        }
        setIsAuthenticated(false)
        return
      }
      
      const data = await response.json()
      setIsAuthenticated(data.success || false)
    } catch (error) {
      // Network error or JSON parse error
      console.error('Error checking authentication:', error)
      setIsAuthenticated(false)
    }
  }

  const handleLoginSuccess = () => {
    setIsAuthenticated(true)
  }

  const handleLogout = async () => {
    try {
      await fetch('/api/auth/logout', {
        method: 'POST',
        credentials: 'include'
      })
      setIsAuthenticated(false)
    } catch (error) {
      console.error('Error during logout:', error)
      setIsAuthenticated(false)
    }
  }

  if (isAuthenticated === null) {
    // Still checking authentication
    return (
      <div className="App">
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
          <div>Checking authentication...</div>
        </div>
      </div>
    )
  }

  return (
    <div className="App">
      {isAuthenticated ? (
        <Dashboard onLogout={handleLogout} />
      ) : (
        <Login onLoginSuccess={handleLoginSuccess} />
      )}
    </div>
  )
}

export default App
