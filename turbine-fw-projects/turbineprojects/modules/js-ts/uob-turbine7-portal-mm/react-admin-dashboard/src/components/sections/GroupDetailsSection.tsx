import React, { useState } from 'react'
import { Group } from '../../types'
import SearchBox from '../SearchBox'
import DataGrid from '../DataGrid'
import './Section.css'

interface GroupDetailsSectionProps {
  data: Group[]
}

const GroupDetailsSection: React.FC<GroupDetailsSectionProps> = ({ data }) => {
  const [searchTerm, setSearchTerm] = useState('')
  const [filteredData, setFilteredData] = useState<Group[]>(data)

  React.useEffect(() => {
    if (searchTerm === '') {
      setFilteredData(data)
    } else {
      const filtered = data.filter((group) =>
        group.name?.toLowerCase().includes(searchTerm.toLowerCase())
      )
      setFilteredData(filtered)
    }
  }, [searchTerm, data])

  const columns = [
    { key: 'id', label: 'Group ID' },
    { key: 'name', label: 'Group Name' },
    { key: 'actions', label: 'Actions' }
  ]

  const renderCell = (group: Group, column: string) => {
    switch (column) {
      case 'actions':
        return (
          <a href={`/app/group,FluxGroupForm.vm?groupId=${group.id}`} className="action-link">
            Edit
          </a>
        )
      default:
        return group[column as keyof Group] || ''
    }
  }

  return (
    <div className="section-container">
      <div className="section-header">
        <h2 className="section-title">Group Details</h2>
      </div>
      <div className="section-content">
        <SearchBox
          placeholder="Search groups by name..."
          value={searchTerm}
          onChange={setSearchTerm}
        />
        <DataGrid
          columns={columns}
          data={filteredData}
          renderCell={renderCell}
          emptyMessage="No groups found"
        />
      </div>
    </div>
  )
}

export default GroupDetailsSection
