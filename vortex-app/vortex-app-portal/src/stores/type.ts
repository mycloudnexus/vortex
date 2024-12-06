import type { ICompany } from '@/services/types'
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
  user_id: string

}
export type AuthOrg = {
  roles: string[],
  id: string,
  name: string,
  display_name: string,
  metadata: {
    loginType: string,
    status: string,
    type: string
  }

}
export type CustomerUser = {
  email: string
  mgmt: boolean
  name: string
  orgId: string
  roles: string[]
  userId: string,
  userInfo: AuthUser & { organization: AuthOrg }
}

export type VortexUserType = 'reseller' | 'customer'

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
  downstreamUser: User | null
  setDownstreamUser: (c: User | null) => void
  roleList: Role | null
  setRoleList: (c: Role | null) => void
  userType: VortexUserType | null
  setuserType: (c: VortexUserType) => void
  customerUser: CustomerUser | null,
  setCustomerUser: (c: CustomerUser) => void,
  customerCompanies: ICompany[],
  setCustomerCompanies: (c: ICompany[]) => void,


}