import { createOrganization, getCompanyList, getOrganizationById, updateOrganization } from '@/services'
import {
  CreateOrganizationRequestBody,
  CreateOrganizationResponse,
  IOrganization,
  RequestResponse,
  UpdateOrganizationRequestBody
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

export const useUpdateOrganization = () => {
  return useMutation<CreateOrganizationResponse, Error, UpdateOrganizationRequestBody>(
    (req: UpdateOrganizationRequestBody) => updateOrganization(req)
  )
}

export const useGetOrganizationById = (
  orgId: string,
  config: UseQueryOptions<CreateOrganizationResponse, Error> = {}
): UseQueryResult<CreateOrganizationResponse, Error> => {
  return useQuery<CreateOrganizationResponse, Error>(
    ['getCompanyById', orgId],
    () => getOrganizationById(orgId),
    config
  )
}
