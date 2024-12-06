import { render } from '@testing-library/react'
import { StyledButton } from '../styled'

describe('Styled Components', () => {
  it('should render the button', () => {
    const { getByText } = render(
      <StyledButton $backgroundColor='white' $width='50%'>
        Button Test
      </StyledButton>
    )
    expect(getByText(/Button Test/i)).toBeInTheDocument()
  })
})
