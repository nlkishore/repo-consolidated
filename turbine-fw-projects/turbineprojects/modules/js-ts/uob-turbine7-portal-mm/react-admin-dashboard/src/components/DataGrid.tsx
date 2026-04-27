import React from 'react'
import './DataGrid.css'

interface Column {
  key: string
  label: string
}

interface DataGridProps<T> {
  columns: Column[]
  data: T[]
  renderCell: (item: T, column: string) => React.ReactNode
  emptyMessage?: string
}

function DataGrid<T extends { id?: number | string }>({
  columns,
  data,
  renderCell,
  emptyMessage = 'No data found'
}: DataGridProps<T>) {
  return (
    <div className="data-grid-container">
      <table className="data-grid">
        <thead>
          <tr>
            {columns.map((column) => (
              <th key={column.key}>{column.label}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="empty-message">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            data.map((item, index) => (
              <tr key={item.id || index}>
                {columns.map((column) => (
                  <td key={column.key}>{renderCell(item, column.key)}</td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  )
}

export default DataGrid
