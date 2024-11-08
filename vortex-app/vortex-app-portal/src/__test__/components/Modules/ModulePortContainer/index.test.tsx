import ModulePortContainer from '@/components/Modules/ModulePortContainer'
import { render } from '@/test/setupTest'

test('Modules/ModulePortContainer component', async () => {
  const { container } = render(<ModulePortContainer />)
  expect(container).toBeInTheDocument()
})
