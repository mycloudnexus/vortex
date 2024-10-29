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
