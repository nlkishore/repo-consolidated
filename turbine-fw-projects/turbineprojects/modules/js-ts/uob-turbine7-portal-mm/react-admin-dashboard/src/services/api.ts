import axios from 'axios'
import { User, Group, Role, Permission } from '../types'

// API Base URL - matches Spring REST API endpoints
// In development, Vite proxy handles /api -> /uob-t7-portal-mm-tomcat/api
// In production, use full context path
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/uob-t7-portal-mm-tomcat/api'

// DTO interfaces matching backend DTOs
interface UserDTO {
  userId?: number
  loginName?: string
  firstName?: string
  lastName?: string
  email?: string
  confirmed?: boolean
  lastLogin?: string
  created?: string
  modified?: string
}

interface GroupDTO {
  groupId?: number
  groupName?: string
}

interface RoleDTO {
  roleId?: number
  roleName?: string
}

interface PermissionDTO {
  permissionId?: number
  permissionName?: string
}

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true // Include cookies for session management
})

// Map DTO to React types
const mapUserDTO = (dto: UserDTO): User => ({
  id: dto.userId,
  name: dto.loginName,
  firstName: dto.firstName,
  lastName: dto.lastName,
  email: dto.email
})

const mapGroupDTO = (dto: GroupDTO): Group => ({
  id: dto.groupId,
  name: dto.groupName
})

const mapRoleDTO = (dto: RoleDTO): Role => ({
  id: dto.roleId,
  name: dto.roleName
})

const mapPermissionDTO = (dto: PermissionDTO): Permission => ({
  id: dto.permissionId,
  name: dto.permissionName
})

// Mock data for development fallback
const mockUsers: User[] = [
  { id: 1, name: 'admin', firstName: 'Admin', lastName: 'User', email: 'admin@uob.com' },
  { id: 2, name: 'manager1', firstName: 'John', lastName: 'Manager', email: 'manager1@uob.com' },
  { id: 3, name: 'manager2', firstName: 'Jane', lastName: 'Manager', email: 'manager2@uob.com' }
]

const mockGroups: Group[] = [
  { id: 1, name: 'ADMINISTRATORS' },
  { id: 2, name: 'MANAGERS' },
  { id: 3, name: 'USERS' }
]

const mockRoles: Role[] = [
  { id: 1, name: 'turbineadmin' },
  { id: 2, name: 'admin' },
  { id: 3, name: 'manager' },
  { id: 4, name: 'user' }
]

const mockPermissions: Permission[] = [
  { id: 1, name: 'ADMIN' },
  { id: 2, name: 'MANAGER' },
  { id: 3, name: 'USER' }
]

/**
 * Fetch all users from REST API
 * GET /api/users
 */
export const fetchUsers = async (): Promise<User[]> => {
  try {
    const response = await api.get<UserDTO[]>('/users')
    return response.data.map(mapUserDTO)
  } catch (error) {
    console.error('Error fetching users from API:', error)
    console.warn('Falling back to mock data')
    // Return mock data for development/fallback
    return Promise.resolve(mockUsers)
  }
}

/**
 * Fetch all groups from REST API
 * GET /api/groups
 */
export const fetchGroups = async (): Promise<Group[]> => {
  try {
    const response = await api.get<GroupDTO[]>('/groups')
    return response.data.map(mapGroupDTO)
  } catch (error) {
    console.error('Error fetching groups from API:', error)
    console.warn('Falling back to mock data')
    return Promise.resolve(mockGroups)
  }
}

/**
 * Fetch all roles from REST API
 * GET /api/roles
 */
export const fetchRoles = async (): Promise<Role[]> => {
  try {
    const response = await api.get<RoleDTO[]>('/roles')
    return response.data.map(mapRoleDTO)
  } catch (error) {
    console.error('Error fetching roles from API:', error)
    console.warn('Falling back to mock data')
    return Promise.resolve(mockRoles)
  }
}

/**
 * Fetch all permissions from REST API
 * GET /api/permissions
 */
export const fetchPermissions = async (): Promise<Permission[]> => {
  try {
    const response = await api.get<PermissionDTO[]>('/permissions')
    return response.data.map(mapPermissionDTO)
  } catch (error) {
    console.error('Error fetching permissions from API:', error)
    console.warn('Falling back to mock data')
    return Promise.resolve(mockPermissions)
  }
}

/**
 * Search users by term
 * GET /api/users?search={term}
 */
export const searchUsers = async (searchTerm: string): Promise<User[]> => {
  try {
    const response = await api.get<UserDTO[]>('/users', {
      params: { search: searchTerm }
    })
    return response.data.map(mapUserDTO)
  } catch (error) {
    console.error('Error searching users:', error)
    // Fallback to client-side filtering
    const allUsers = await fetchUsers()
    return allUsers.filter(user =>
      user.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.lastName?.toLowerCase().includes(searchTerm.toLowerCase())
    )
  }
}
