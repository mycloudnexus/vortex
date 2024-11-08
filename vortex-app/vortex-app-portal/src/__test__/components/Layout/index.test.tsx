import Layout, { SliderCustom } from '@/components/Layout'
import NavMain from '@/components/Layout/NavMain'
import { render } from '@/test/setupTest'

test('Layout component', async () => {
  const { container } = render(<Layout />)
  expect(container).toBeInTheDocument()
})
test('SliderCustom component', async () => {
  const { container } = render(<SliderCustom $mainColor='#ff0000' />)
  expect(container).toBeInTheDocument()
})
test('Layout/NavMain component', async () => {
  const { container } = render(<NavMain />)
  expect(container).toBeInTheDocument()
})
