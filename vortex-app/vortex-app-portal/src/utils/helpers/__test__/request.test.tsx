import MockAdapter from 'axios-mock-adapter'
import request from '@/utils/helpers/request'
import { storeOrg, getOrg } from '@/utils/helpers/token'

describe('Axios interceptor', () => {
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
    const url = 'https://www.test.com'
    Object.defineProperty(window, 'location', {
      value: {
        origin: url
      },
      writable: true
    })
    mock.onGet('/users').reply(401, { error: { message: 'The session token been deleted' } })
    try {
      await request.get('/users')
    } catch (error: any) {
      expect(error.response.status).toEqual(401)
      console.log('---getOrg()getOrg()', getOrg())
      expect(window.location.href).toContain(`/${getOrg()}/login`)
    }
  })
})
