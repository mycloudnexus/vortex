import { ENV } from '@/constant'

export const USER_INFO = '/api/user'
export const AUTH_TOKEN = '/api/auth/token'
export const USER_ROLE = '/v2/admin/roles'
export const DOWNSTREAM_AUTH_TOKEN = `${ENV.DOWNSTREAM_API_PREFIX}${AUTH_TOKEN}`
export const DOWNSTREAM_USER_ROLE = `${ENV.DOWNSTREAM_API_PREFIX}${USER_ROLE}`

//Customer Company Endpoints
export const ORGANIZATIONS = '/mgmt/organizations'
