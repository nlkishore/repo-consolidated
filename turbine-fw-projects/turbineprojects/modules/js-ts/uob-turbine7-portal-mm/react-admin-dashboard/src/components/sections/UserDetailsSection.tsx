import React, { useState } from 'react'
import { User } from '../../types'
import SearchBox from '../SearchBox'
import DataGrid from '../DataGrid'
import './Section.css'

interface UserDetailsSectionProps {
  data: User[]
}

const UserDetailsSection: React.FC<UserDetailsSectionProps> = ({ data }) => {
  const [searchTerm, setSearchTerm] = useState('')
  const [filteredData, setFilteredData] = useState<User[]>(data)

  React.useEffect(() => {
    if (searchTerm === '') {
      setFilteredData(data)
    } else {
      const filtered = data.filter((user) =>
        user.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.lastName?.toLowerCase().includes(searchTerm.toLowerCase())
      )
      setFilteredData(filtered)
    }
  }, [searchTerm, data])

  const columns = [
    { key: 'id', label: 'User ID' },
    { key: 'name', label: 'Login Name' },
    { key: 'firstName', label: 'First Name' },
    { key: 'lastName', label: 'Last Name' },
    { key: 'email', label: 'Email' },
    { key: 'actions', label: 'Actions' }
  ]

  const renderCell = (user: User, column: string) => {
    switch (column) {
      case 'actions':
        return (
          <a href={`/app/user,FluxUserForm.vm?username=${user.name}`} className="action-link">
            Edit
          </a>
        )
      default:
        return user[column as keyof User] || ''
    }
  }

  return (
    <div className="section-container">
      <div className="section-header">
        <h2 className="section-title">User Details</h2>
      </div>
      <div className="section-content">
        <SearchBox
          placeholder="Search users by name, email, or login..."
          value={searchTerm}
          onChange={setSearchTerm}
        />
        <DataGrid
          columns={columns}
          data={filteredData}
          renderCell={renderCell}
          emptyMessage="No users found"
        />
      </div>
    </div>
  )
}

export default UserDetailsSection
