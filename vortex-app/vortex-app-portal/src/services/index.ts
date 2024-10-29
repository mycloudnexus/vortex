import request from '@/utils/helpers/request'
import { USER_INFO } from './api'

export const getUserDetail = (name: string) => {
  return request(`${USER_INFO}/${name}`, {})
}
