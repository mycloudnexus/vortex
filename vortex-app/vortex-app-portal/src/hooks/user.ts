import { useQuery } from 'react-query'

import { getUserDetail } from '@/services'

export const useGetUserDetail = (params: string, config: any = {}) => {
  return useQuery(['getUserDetail', params], () => getUserDetail(params), {
    ...config
  })
}
