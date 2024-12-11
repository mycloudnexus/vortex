import { ENV } from '@/constant'
const { DOWNSTREAM_API_PREFIX } = ENV
// user
export const DOWNSTREAM_USER_DERAIL_WITH_NAME = '/api/user'
export const DOWNSTREAM_USER_INFO = `${DOWNSTREAM_API_PREFIX}/api/auth/token`
export const DOWNSTREAM_USER_ROLE = `${DOWNSTREAM_API_PREFIX}/v2/admin/roles`
export const VORTEX_USER_ROLE = `/auth/token`

//Customer Company Endpoints
export const ORGANIZATIONS = '/mgmt/organizations'