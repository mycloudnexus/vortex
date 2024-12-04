import request from '@/utils/helpers/request'
import {
  CreateOrganizationRequestBody,
  CreateOrganizationResponse,
  IOrganization,
  RequestResponse,
  UpdateOrganizationRequestBody
} from './types'

import { DOWNSTREAM_USER_INFO, DOWNSTREAM_DOWNSTREAM_USER_INFO, DOWNSTREAM_USER_ROLE, ORGANIZATIONS } from './api'

export const getUserDetail = (name: string) => {
  return request(`${DOWNSTREAM_USER_INFO}/${name}`, {})
}

export const getUserAuthToken = () => {
  return request(DOWNSTREAM_DOWNSTREAM_USER_INFO)
}

export const getUserRole = () => {
  return request(DOWNSTREAM_USER_ROLE)
}

export const getCompanyList = async (): Promise<RequestResponse<IOrganization>> => {
  try {
    const response = await request(ORGANIZATIONS, {
      params: {
        size: 200
      }
    })
    return response.data
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
