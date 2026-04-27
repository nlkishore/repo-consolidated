import React, { useState } from 'react'
import { Permission } from '../../types'
import SearchBox from '../SearchBox'
import DataGrid from '../DataGrid'
import './Section.css'

interface PermissionDetailsSectionProps {
  data: Permission[]
}

const PermissionDetailsSection: React.FC<PermissionDetailsSectionProps> = ({ data }) => {
  const [searchTerm, setSearchTerm] = useState('')
  const [filteredData, setFilteredData] = useState<Permission[]>(data)

  React.useEffect(() => {
    if (searchTerm === '') {
      setFilteredData(data)
    } else {
      const filtered = data.filter((permission) =>
        permission.name?.toLowerCase().includes(searchTerm.toLowerCase())
      )
      setFilteredData(filtered)
    }
  }, [searchTerm, data])

  const columns = [
    { key: 'id', label: 'Permission ID' },
    { key: 'name', label: 'Permission Name' },
    { key: 'actions', label: 'Actions' }
  ]

  const renderCell = (permission: Permission, column: string) => {
    switch (column) {
      case 'actions':
        return (
          <a href={`/app/permission,FluxPermissionForm.vm?permissionId=${permission.id}`} className="action-link">
            Edit
          </a>
        )
      default:
        return permission[column as keyof Permission] || ''
    }
  }

  return (
    <div className="section-container">
      <div className="section-header">
        <h2 className="section-title">Permission Details</h2>
      </div>
      <div className="section-content">
        <SearchBox
          placeholder="Search permissions by name..."
          value={searchTerm}
          onChange={setSearchTerm}
        />
        <DataGrid
          columns={columns}
          data={filteredData}
          renderCell={renderCell}
          emptyMessage="No permissions found"
        />
      </div>
    </div>
  )
}

export default PermissionDetailsSection
