import App from '@/App'
import { render } from '@testing-library/react'

test('App component', async () => {
  const { container } = render(<App />)
  expect(container).toBeInTheDocument()
})
