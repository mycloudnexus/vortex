import MockAdapter from 'axios-mock-adapter'
import request from '@/utils/helpers/request'
import { storeOrg, getOrg } from '@/utils/helpers/token'

describe('Axios interceptor', () => {
  const url = 'https://www.test.com'
  Object.defineProperty(window, 'location', {
    value: {
      origin: url
    },
    writable: true
  })
  let mock: any

  beforeEach(() => {
    mock = new MockAdapter(request)
  })

  afterEach(() => {
    mock.restore()
  })

  it('should add authorization header to request', async () => {
    const testToken = 'hello'
    const testCompany = 'currentCompany33'
    window.localStorage.setItem('token', testToken)
    window.localStorage.setItem('currentCompany', testCompany)
    mock.onGet('/users').reply(200, [])

    await request.get('/users')

    expect(mock.history.get[0].headers['Authorization']).toEqual(`Bearer ${testToken}`)
    expect(mock.history.get[0].headers['x-vortex-customer-org-id']).toEqual(testCompany)
  })
  it('should not have  header to request', async () => {
    window.localStorage.removeItem('token')
    window.localStorage.removeItem('currentCompany')
    mock.onGet('/users').reply(200, [])
    await request.get('/users')

    expect(mock.history.get[0].headers['Authorization']).toBeFalsy()
    expect(mock.history.get[0].headers['x-vortex-customer-org-id']).toBeFalsy()
  })

  it('should handle response errors', async () => {
    mock.onGet('/users').reply(500, 'Internal Server Error')

    try {
      await request.get('/users')
    } catch (error: any) {
      expect(error.response.status).toEqual(500)
      expect(error.response.data).toEqual('Internal Server Error')
    }
  })

  it('should redirect to login when response code is 401', async () => {
    storeOrg('org')
    mock.onGet('/users').reply(401, { error: { message: 'The session token been deleted' } })
    try {
      await request.get('/users')
    } catch (error: any) {
      expect(error.response.status).toEqual(401)
      expect(window.location.href).toContain(`/${getOrg()}/login`)
    }
  })
  it('it should redirect to login when response code is 401 (reseller)', async () => {
    window.localStorage.removeItem('org')
    mock.onGet('/users').reply(401, { error: { message: 'The user is not logged in' } })
    try {
      await request.get('/users')
    } catch (error: any) {
      expect(error.response.status).toEqual(401)
      expect(window.location.href).toContain(`/login`)
    }
  })
})
