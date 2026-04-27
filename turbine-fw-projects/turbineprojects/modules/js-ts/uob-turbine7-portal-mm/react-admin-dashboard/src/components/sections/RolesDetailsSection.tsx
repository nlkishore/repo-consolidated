import React, { useState } from 'react'
import { Role } from '../../types'
import SearchBox from '../SearchBox'
import DataGrid from '../DataGrid'
import './Section.css'

interface RolesDetailsSectionProps {
  data: Role[]
}

const RolesDetailsSection: React.FC<RolesDetailsSectionProps> = ({ data }) => {
  const [searchTerm, setSearchTerm] = useState('')
  const [filteredData, setFilteredData] = useState<Role[]>(data)

  React.useEffect(() => {
    if (searchTerm === '') {
      setFilteredData(data)
    } else {
      const filtered = data.filter((role) =>
        role.name?.toLowerCase().includes(searchTerm.toLowerCase())
      )
      setFilteredData(filtered)
    }
  }, [searchTerm, data])

  const columns = [
    { key: 'id', label: 'Role ID' },
    { key: 'name', label: 'Role Name' },
    { key: 'actions', label: 'Actions' }
  ]

  const renderCell = (role: Role, column: string) => {
    switch (column) {
      case 'actions':
        return (
          <a href={`/app/role,FluxRoleForm.vm?roleId=${role.id}`} className="action-link">
            Edit
          </a>
        )
      default:
        return role[column as keyof Role] || ''
    }
  }

  return (
    <div className="section-container">
      <div className="section-header">
        <h2 className="section-title">Roles Details</h2>
      </div>
      <div className="section-content">
        <SearchBox
          placeholder="Search roles by name..."
          value={searchTerm}
          onChange={setSearchTerm}
        />
        <DataGrid
          columns={columns}
          data={filteredData}
          renderCell={renderCell}
          emptyMessage="No roles found"
        />
      </div>
    </div>
  )
}

export default RolesDetailsSection
