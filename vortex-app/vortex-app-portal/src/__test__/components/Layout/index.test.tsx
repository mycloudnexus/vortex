import { useAuth0 } from '@auth0/auth0-react'
import Layout, { SliderCustom } from '@/components/Layout'
import NavMain from '@/components/Layout/NavMain'
import { fireEvent, render, screen } from '@/test/setupTest'

jest.mock('@auth0/auth0-react')
const fn = useAuth0 as any
test('Layout component', async () => {
  fn.mockReturnValue({ isAuthenticated: true, user: {}, getAccessTokenSilently: jest.fn() } as any)
  const { container } = render(<Layout />)
  expect(container).toBeInTheDocument()
  const dcPortsMenuBtn = screen.getByText('DC Ports')
  fireEvent.click(dcPortsMenuBtn)
  expect(screen.getByText('View all')).toBeInTheDocument()
  expect(screen.getByText('Add new')).toBeInTheDocument()
  const dashboardItem = screen.getAllByText('Dashboard')[0]
  fireEvent.click(dashboardItem)
  expect(dashboardItem.closest('li')?.classList.contains('ant-menu-item-selected'))
  fireEvent.click(screen.getByText('View all'))
  expect(screen.getByText('View all').closest('li')?.classList.contains('ant-menu-item-selected'))
  fireEvent.click(screen.getByText('Add new'))
  expect(screen.getByText('Add new').closest('li')?.classList.contains('ant-menu-item-selected'))
})
test('SliderCustom component', async () => {
  const { container } = render(<SliderCustom $mainColor='#ff0000' />)
  expect(container).toBeInTheDocument()
})
test('Layout/NavMain component', async () => {
  const { container } = render(<NavMain />)
  expect(container).toBeInTheDocument()
})
