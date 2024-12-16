import request from '@/utils/helpers/request'
import { ORGANIZATIONS } from './api'
import {
  AddConnectionRequestBody,
  AddConnectionResponse,
  CreateOrganizationRequestBody,
  CreateOrganizationResponse,
  IOrganization,
  RequestResponse,
  UpdateOrganizationRequestBody
} from './types'

export const getCompanyList = async (): Promise<RequestResponse<IOrganization>> => {
  const response = await request(ORGANIZATIONS, {
    params: {
      size: -1
    }
  })
  return response?.data
}

export const createOrganization = async (req: CreateOrganizationRequestBody): Promise<CreateOrganizationResponse> => {
  const response = await request.post(ORGANIZATIONS, req)
  return response.data
}

export const updateOrganization = async (req: UpdateOrganizationRequestBody): Promise<CreateOrganizationResponse> => {
  const { id, request_body } = req
  const response = await request.patch(`${ORGANIZATIONS}/${id}`, request_body)
  return response.data
}

export const getOrganizationById = async (orgId: string): Promise<CreateOrganizationResponse> => {
  const response = await request(`${ORGANIZATIONS}/${orgId}`)
  return response.data
}

export const createConnection = async (
  orgId: string,
  req: AddConnectionRequestBody
): Promise<RequestResponse<AddConnectionResponse>> => {
  const response = await request.post(`${ORGANIZATIONS}/${orgId}/connection`, req)
  return response.data
}
