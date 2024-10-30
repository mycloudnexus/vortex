import request from '@/utils/helpers/request'
import { USER_INFO, AUTH_TOKEN, USER_ROLE } from './api'

export const getUserDetail = (name: string) => {
  return request(`${USER_INFO}/${name}`, {})
}

export const getUserAuthToken = () => {
  return request(AUTH_TOKEN)
}

export const getUserRole = () => {
  return request(USER_ROLE)
}
