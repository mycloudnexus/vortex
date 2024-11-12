import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import BreadCrumb from '..'

jest.mock('@/stores/app.store', () => ({
  useAppStore: () => ({
    mainColor: 'blue'
  })
}))

jest.mock('@/routers', () => ({
  routes: [
    {
      path: '/home',
      breadCrumbName: 'Home',
      children: [
        {
          path: '/home/about',
          breadCrumbName: 'About'
        },
        {
          path: '/home/profile',
          breadCrumbName: 'Profile',
          children: [
            {
              path: '/home/profile/details',
              breadCrumbName: 'Details'
            }
          ]
        }
      ]
    },
    {
      path: '/contact',
      breadCrumbName: ''
    },
    {
      path: '/',
      breadCrumbName: 'Root'
    }
  ]
}))

describe('BreadCrumb Component', () => {
  it('should render the breadcrumb correctly for a given path', () => {
    const { getByText, baseElement } = render(
      <MemoryRouter initialEntries={['/home/about']}>
        <BreadCrumb />
      </MemoryRouter>
    )
    expect(getByText('Home')).toBeInTheDocument()
    expect(getByText('About')).toBeInTheDocument()
    expect(baseElement).toMatchSnapshot()
  })

  it('should render breadcrumb for root path', () => {
    const { baseElement, getByText } = render(
      <MemoryRouter initialEntries={['/']}>
        <BreadCrumb />
      </MemoryRouter>
    )
    expect(getByText('Root')).toBeInTheDocument()
    expect(baseElement).toMatchSnapshot()
  })

  it('should render multiple breadcrumb levels', () => {
    const { getByText, baseElement } = render(
      <MemoryRouter initialEntries={['/home/profile/details']}>
        <BreadCrumb />
      </MemoryRouter>
    )
    expect(getByText(/Home/i)).toBeInTheDocument()
    expect(getByText(/Profile/i)).toBeInTheDocument()
    expect(getByText(/Details/i)).toBeInTheDocument()
    expect(baseElement).toMatchSnapshot()
  })

  it('BreadCrumb Component should handle missing breadcrumb name gracefully', () => {
    const { queryByText, baseElement } = render(
      <MemoryRouter initialEntries={['/contact']}>
        <BreadCrumb />
      </MemoryRouter>
    )

    expect(queryByText('Contact')).not.toBeInTheDocument()
    expect(baseElement).toMatchSnapshot()
  })

  it('should render nothing for a path with no matching routes', () => {
    const { queryByText, baseElement } = render(
      <MemoryRouter initialEntries={['/nonexistent']}>
        <BreadCrumb />
      </MemoryRouter>
    )
    expect(queryByText('Home')).toBeNull()
    expect(queryByText('About')).toBeNull()
    expect(baseElement).toMatchSnapshot()
  })
})
