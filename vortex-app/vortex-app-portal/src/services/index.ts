import request from '@/utils/helpers/request'
import { USER_INFO, AUTH_TOKEN, USER_ROLE, ORGANIZATIONS } from './api'
import {
  CreateOrganizationRequestBody,
  CreateOrganizationResponse,
  IOrganization,
  RequestResponse,
  UpdateOrganizationRequestBody
} from './types'

export const getUserDetail = (name: string) => {
  return request(`${USER_INFO}/${name}`, {})
}

export const getUserAuthToken = () => {
  return request(AUTH_TOKEN)
}

export const getUserRole = () => {
  return request(USER_ROLE)
}

export const getCompanyList = async (): Promise<RequestResponse<IOrganization>> => {
  try {
    const response = await request(ORGANIZATIONS, {
      params: {
        size: -1
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
