import React from 'react'
import UserDetailsSection from './sections/UserDetailsSection'
import GroupDetailsSection from './sections/GroupDetailsSection'
import RolesDetailsSection from './sections/RolesDetailsSection'
import PermissionDetailsSection from './sections/PermissionDetailsSection'
import { User, Group, Role, Permission } from '../types'
import './WorkArea.css'

interface WorkAreaProps {
  users: User[]
  groups: Group[]
  roles: Role[]
  permissions: Permission[]
  activeSection: string
  showFormSummary: boolean
}

const WorkArea: React.FC<WorkAreaProps> = ({
  users,
  groups,
  roles,
  permissions,
  activeSection,
  showFormSummary
}) => {
  const sections = [
    { id: 'user-details', component: UserDetailsSection, data: users },
    { id: 'group-details', component: GroupDetailsSection, data: groups },
    { id: 'roles-details', component: RolesDetailsSection, data: roles },
    { id: 'permission-details', component: PermissionDetailsSection, data: permissions }
  ]

  return (
    <div className="workarea-container">
      {showFormSummary ? (
        sections.map((section) => {
          const Component = section.component
          return (
            <div
              key={section.id}
              id={section.id}
              className={`workarea-section ${activeSection === section.id ? 'active' : ''}`}
            >
              <Component data={section.data} />
            </div>
          )
        })
      ) : (
        sections
          .filter((section) => section.id === activeSection)
          .map((section) => {
            const Component = section.component
            return (
              <div
                key={section.id}
                id={section.id}
                className="workarea-section active"
              >
                <Component data={section.data} />
              </div>
            )
          })
      )}
    </div>
  )
}

export default WorkArea
