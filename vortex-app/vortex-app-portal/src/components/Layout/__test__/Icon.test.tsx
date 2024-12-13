import { render } from '@testing-library/react'

import { DefaultLogo, DefaultCompanyLogo } from '../Icon'

describe('Icon component page', () => {
  it('logo render', () => {
    const { container } = render(<DefaultLogo />)
    expect(container.firstElementChild?.tagName).toBe('svg')
  })
  it('DefaultCompanyLogo render', () => {
    const { container } = render(<DefaultCompanyLogo />)
    expect(container.firstElementChild?.tagName).toBe('svg')
  })
})
