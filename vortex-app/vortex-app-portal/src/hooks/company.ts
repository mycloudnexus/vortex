import { createOrganization, getCompanyList } from '@/services'
import {
  CreateOrganizationRequestBody,
  CreateOrganizationResponse,
  IOrganization,
  RequestResponse
} from '@/services/types'
import { useMutation, useQuery, UseQueryOptions, UseQueryResult } from 'react-query'

export const useGetCompanyList = (
  config: UseQueryOptions<RequestResponse<IOrganization>, Error> = {}
): UseQueryResult<RequestResponse<IOrganization>, Error> => {
  return useQuery<RequestResponse<IOrganization>, Error>(['getCompanyList', config], () => {
    return getCompanyList()
  }, {
    enabled: !!!window.localStorage.getItem('org'),
    ...config
  })
}

export const useAddOrganization = () => {
  return useMutation<CreateOrganizationResponse, Error, CreateOrganizationRequestBody>(
    (req: CreateOrganizationRequestBody) => createOrganization(req)
  )
}
