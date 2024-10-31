import { useQuery } from 'react-query'

import { getUserDetail, getUserAuthToken, getUserRole } from '@/services'

export const useGetUserDetail = (params: string, config: any = {}) => {
  return useQuery(['getUserDetail', params], () => getUserDetail(params), config)
}

export const useGetUserAuthDetail = (config: any = {}) => {
  return useQuery(['getUserAuthToken'], () => getUserAuthToken(), config)
}

export const useGetUserRole = (config: any = {}) => {
  return useQuery(['getUserRole'], () => getUserRole(), config)
}
