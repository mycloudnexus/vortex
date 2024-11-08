import axios, { isCancel } from 'axios'
import _ from 'lodash'
import { ENV } from '@/constant'

const request = axios.create({
  timeout: 50000,
  baseURL: ENV.API_BASE_URL
})
request.interceptors.request.use((config: any) => {
  const token = window.portalToken
  config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = _.get(error, 'response.status')
    const message = _.get(error, 'response.data.error.message')
    const principalId = _.get(error, 'response.data.error.details.principalId')
    const pbacErrorEmptyPrincipal =
      status === 401 && message === accessDenied && _.isPlainObject(principalId) && _.isEmpty(principalId)
    const sessionExpired = status === 401 && invalidToken.includes(message)
    if (pbacErrorEmptyPrincipal || sessionExpired) {
      //TODO To prevent it jump login all the time
      // window.location.href = `${getOrg()}/login`
    }

    /**
     * Send contextual data to Sentry
     */
    if (error.response?.data) {
      let errorData = error.response.data

      try {
        errorData = JSON.stringify(error.response.data)
      } catch (err) {
        console.log('--tes-err', err)
      }

      console.log('--tes-errorDatat', errorData)
    }

    return Promise.reject(error)
  }
)

export const isCancelCaught = (thrown: object) => isCancel(thrown)

const invalidToken = ['The user is not logged in', 'The session token has expired', 'The session token been deleted']

const accessDenied = 'Access denied'

export const fetchData = (path: string, config?: any) => request.get(path, config).then((value) => value.data)

export const fetchCollection = (path: string) => fetchData(path).then((d: { results: any[] }) => d.results)

export const post = <ResponseBody = any>(path: string, data: any, config?: any) =>
  request.post<ResponseBody>(path, data, config)

export const get = <ResponseBody = any>(path: string, config?: any) => request.get<ResponseBody>(path, config)

export const put = <ResponseBody = any>(path: string, data: any, config?: any) =>
  request.put<ResponseBody>(path, data, config)

export const patch = <ResponseBody = any>(path: string, data: any, config?: any) =>
  request.patch<ResponseBody>(path, data, config)

export const deleteData = <ResponseBody = any>(path: string, config?: any) => request.delete<ResponseBody>(path, config)

export default request
