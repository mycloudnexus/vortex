import request from '@/utils/helpers/request'
import { DOWNSTREAM_USER_INFO, DOWNSTREAM_DOWNSTREAM_USER_INFO, DOWNSTREAM_USER_ROLE, VORTEX_USER_ROLE } from './api'

export const getUserDetail = (name: string) => {
  return request(`${DOWNSTREAM_USER_INFO}/${name}`, {})
}

export const getUserAuthToken = () => {
  return request(DOWNSTREAM_DOWNSTREAM_USER_INFO)
}

export const getUserRole = () => {
  return request(DOWNSTREAM_USER_ROLE)
}

export const getVortexUser = () => {
  return request(VORTEX_USER_ROLE)
}
