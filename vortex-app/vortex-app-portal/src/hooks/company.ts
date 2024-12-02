import { createOrganization, getCompanyList, updateOrganization } from '@/services'
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
  return useQuery<RequestResponse<IOrganization>, Error>(['getCompanyList'], () => getCompanyList(), config)
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
