import request from '@/utils/helpers/request'
import { USER_INFO, AUTH_TOKEN, USER_ROLE, GET_COMPANY_LIST, CREATE_COMPANY } from './api'
import { CreateOrganizationRequestBody, CreateOrganizationResponse, IOrganization, RequestResponse } from './types'

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
    const response = await request(GET_COMPANY_LIST)
    return response.data
  } catch (error) {
    console.error(error)
    throw error
  }
}

export const createOrganization = async (req: CreateOrganizationRequestBody): Promise<CreateOrganizationResponse> => {
  try {
    const response = await request.post(CREATE_COMPANY, req)
    return response.data
  } catch (error) {
    console.error(error)
    throw error
  }
}
