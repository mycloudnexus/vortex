import request from '@/utils/helpers/request'
import { USER_INFO, USER_ROLE, ORGANIZATIONS, AUTH_TOKEN } from './api'
import { CreateOrganizationRequestBody, CreateOrganizationResponse, IOrganization, RequestResponse } from './types'
import { ENV } from '@/constant'

export const getUserDetail = (name: string) => {
  return request(`${USER_INFO}/${name}`, {})
}

export const getUserAuthToken = () => {
  return request(`${ENV.DOWNSTREAM_API_PREFIX}${AUTH_TOKEN}`)
}

export const getUserRole = () => {
  return request(`${ENV.DOWNSTREAM_API_PREFIX}${USER_ROLE}`)
}

export const getCompanyList = async (): Promise<RequestResponse<IOrganization>> => {
  try {
    const response = await request(ORGANIZATIONS)
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
