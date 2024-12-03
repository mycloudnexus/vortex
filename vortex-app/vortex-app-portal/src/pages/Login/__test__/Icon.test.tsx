import { render } from '@testing-library/react'

import { Logo } from '../Icon'

describe('Icon component page', () => {
  it('logo render', () => {
    const { container } = render(<Logo />)
    expect(container.firstElementChild?.tagName).toBe('svg')
  })
})
