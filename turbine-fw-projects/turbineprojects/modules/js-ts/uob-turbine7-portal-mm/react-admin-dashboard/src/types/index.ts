export interface User {
  id?: number | string
  name?: string
  firstName?: string
  lastName?: string
  email?: string
}

export interface Group {
  id?: number | string
  name?: string
}

export interface Role {
  id?: number | string
  name?: string
}

export interface Permission {
  id?: number | string
  name?: string
}
