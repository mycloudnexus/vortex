export interface PolicyDefinition {
  Statement: [
    {
      Action: string[]
      Resource: string[]
      Effect: 'Allow' | 'Deny'
      Sid?: string
    }
  ]
  Version?: string
}
export interface Policy {
  id?: string
  name: string
  description: string
  definition: PolicyDefinition
}
export interface Role {
  id?: string
  name: string
  description: string
  policyIds: string[]
  policies?: Policy[]
}
export interface Company {
  id: string
  username: string
  name: string
  addresses: any[]
}
export interface User {
  id: string
  name: string
  username: string
  email: string
  companies: Company[]
  following: string[]
  token?: string
  accessRoles?: Role[]
}


export type AuthUser = {
  email: string
  email_verified: boolean
  family_name: string
  given_name: string
  name: string
  nickname: string
  org_id: string
  picture: string
  sub: string
  updated_at: string
}

export type AppStore = {
  appLogo: string
  setAppLogo: (appLogo: string) => void
  currentCompany?: {
    id: string
    name: string
  }
  setCurrentCompany: (c: { id: string; name: string }) => void
  currentAuth0User: AuthUser | null
  setCurrentAuth0User: (c: AuthUser | null) => void
  mainColor: string
  user: User | null
  setUser: (c: User | null) => void
  roleList: Role | null
  setRoleList: (c: Role | null) => void
}

