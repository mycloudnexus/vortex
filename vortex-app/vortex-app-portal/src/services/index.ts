import request from '@/utils/helpers/request'
import { ORGANIZATIONS } from './api'
import {
  CreateOrganizationRequestBody,
  CreateOrganizationResponse,
  IOrganization,
  RequestResponse,
  UpdateOrganizationRequestBody
} from './types'

export const getCompanyList = async (): Promise<RequestResponse<IOrganization>> => {
  try {
    const response = await request(ORGANIZATIONS, {
      params: {
        size: -1
      }
    })
    return response?.data
  } catch (error) {
    console.error(error)
    throw error
  }
}

export const createOrganization = async (req: CreateOrganizationRequestBody): Promise<CreateOrganizationResponse> => {
  try {
    const response = await request.post(ORGANIZATIONS, req)
    return response.data
  } catch (error) {
    console.error(error)
    throw error
  }
}

export const updateOrganization = async (req: UpdateOrganizationRequestBody): Promise<CreateOrganizationResponse> => {
  const { id, request_body } = req
  try {
    const response = await request.patch(`${ORGANIZATIONS}/${id}`, request_body)
    return response.data
  } catch (error) {
    console.error(error)
    throw error
  }
}

export const getOrganizationById = async (orgId: string): Promise<CreateOrganizationResponse> => {
  try {
    const response = await request(`${ORGANIZATIONS}/${orgId}`)
    return response.data
  } catch (error) {
    console.error(error)
    throw error
  }
}
