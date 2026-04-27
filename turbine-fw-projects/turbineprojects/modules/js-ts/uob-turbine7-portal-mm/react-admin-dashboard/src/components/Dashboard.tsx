import React, { useState, useEffect } from 'react'
import Header from './Header'
import Sidebar from './Sidebar'
import WorkArea from './WorkArea'
import FloatingDropdown from './FloatingDropdown'
import Footer from './Footer'
import { fetchUsers, fetchGroups, fetchRoles, fetchPermissions } from '../services/api'
import { User, Group, Role, Permission } from '../types'
import './Dashboard.css'

interface DashboardProps {
  onLogout?: () => void
}

const Dashboard: React.FC<DashboardProps> = ({ onLogout }) => {
  const [users, setUsers] = useState<User[]>([])
  const [groups, setGroups] = useState<Group[]>([])
  const [roles, setRoles] = useState<Role[]>([])
  const [permissions, setPermissions] = useState<Permission[]>([])
  const [loading, setLoading] = useState(true)
  const [activeSection, setActiveSection] = useState<string>('user-details')
  const [showFormSummary, setShowFormSummary] = useState(true)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      const [usersData, groupsData, rolesData, permissionsData] = await Promise.all([
        fetchUsers(),
        fetchGroups(),
        fetchRoles(),
        fetchPermissions()
      ])
      setUsers(usersData)
      setGroups(groupsData)
      setRoles(rolesData)
      setPermissions(permissionsData)
    } catch (error) {
      console.error('Error loading data:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleSectionChange = (sectionId: string) => {
    setActiveSection(sectionId)
    // Scroll will be handled by the component that calls this
  }

  const toggleFormSummary = () => {
    setShowFormSummary(!showFormSummary)
  }

  if (loading) {
    return (
      <div className="dashboard-loading">
        <div className="loading-spinner">Loading...</div>
      </div>
    )
  }

  return (
    <div className="dashboard-container">
      <Header onLogout={onLogout} />
      <div className="dashboard-body">
        <Sidebar 
          activeSection={activeSection} 
          onSectionChange={handleSectionChange}
        />
        <div className="dashboard-workarea" id="workarea">
          <FloatingDropdown 
            activeSection={activeSection}
            showFormSummary={showFormSummary}
            onSectionChange={handleSectionChange}
            onToggleFormSummary={toggleFormSummary}
          />
          <WorkArea
            users={users}
            groups={groups}
            roles={roles}
            permissions={permissions}
            activeSection={activeSection}
            showFormSummary={showFormSummary}
          />
        </div>
      </div>
      <Footer />
    </div>
  )
}

export default Dashboard
