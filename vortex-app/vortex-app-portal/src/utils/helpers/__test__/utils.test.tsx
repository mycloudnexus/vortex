import { CreateOrganizationResponse, ICompany } from '@/services/types'
import { updateData } from '../utils'

describe('utils test', () => {
  it('should return the correct value', () => {
    const fn = updateData
    const mockedData: CreateOrganizationResponse = {
      code: 200,
      data: {
        name: '1',
        id: '1',
        display_name: 'test',
        metadata: {
          loginType: '',
          status: '',
          type: ''
        },
        branding: { colors: { page_background: '', primary: '' }, logo_url: '' }
      },
      message: 'OK'
    }
    const response: ICompany[] = [
      {
        name: '1',
        id: '1',
        display_name: '',
        metadata: {
          loginType: '',
          status: '',
          type: ''
        },
        branding: { colors: { page_background: '', primary: '' }, logo_url: '' }
      }
    ]

    expect(fn(response, mockedData)).toEqual([
      {
        name: '1',
        id: '1',
        display_name: 'test',
        metadata: {
          loginType: '',
          status: '',
          type: ''
        },
        branding: { colors: { page_background: '', primary: '' }, logo_url: '' }
      }
    ])
  })
})
