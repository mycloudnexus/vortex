import { render } from '@testing-library/react'
import { ReactNode } from 'react'

import NavMain from '../NavMain'

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  Link: ({ children }: { children: ReactNode }) => <div>{children}</div>
}))

describe('NavMain component ', () => {
  it('NavMain render', () => {
    const { getByText } = render(<NavMain />)
    const elem = getByText(/Console Connect Private Label/i)
    expect(elem).toBeInTheDocument()
  })
})
