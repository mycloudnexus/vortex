import { useQuery } from 'react-query'

import { getCompanyList } from '@/services'

export const useGetCompanyList = (config: any = {}) => {
  return useQuery(['getCompanyList', () => getCompanyList(), config])
}
