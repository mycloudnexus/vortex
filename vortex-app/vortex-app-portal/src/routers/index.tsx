import Layout from '@/components/Layout'
import Dashboard from '@/pages/Dashboard'
import EdgeModuleContainer from '@/pages/ExampleMicroModule'
import { createBrowserRouter, Outlet, RouteObject } from 'react-router-dom'
import Login from '@/components/Access/Login'
import Users from '@/pages/Settings/Users'
import CompanyPage from '@/pages/Settings/CompanyPage'
import CustomerCompany from '@/pages/Settings/CustomerCompany'

type CustomRoute = {
  breadCrumbName?: string
  children?: IRouteObject[]
}
export type IRouteObject = RouteObject & CustomRoute

export const routes: IRouteObject[] = [
  {
    path: '/:organization/login',
    element: <Login />
  },
  {
    path: '',
    element: <Layout />,
    children: [
      {
        path: '/',
        element: <Dashboard />,
        breadCrumbName: 'Dashboard'
      },
      {
        path: '/settings',
        element: <Outlet />,
        breadCrumbName: 'Settings',
        children: [
          {
            index: true,
            path: '/settings/users',
            element: <Users />,
            breadCrumbName: 'Users'
          },
          {
            path: '/settings/customer-company',
            element: <CustomerCompany />,
            breadCrumbName: 'Customer Company'
          },
          { path: '/settings/customer-company/:id', element: <CompanyPage /> }
        ]
      },
      {
        path: 'example/*',
        element: <EdgeModuleContainer />
      }
    ]
  }
]
export const router = createBrowserRouter(routes)
