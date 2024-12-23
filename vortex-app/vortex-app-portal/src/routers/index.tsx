import { createBrowserRouter, Outlet, RouteObject } from 'react-router-dom'
import ModulePortContainer from '@/pages/Modules/ModulePortContainer'
import Layout from '@/components/Layout'
import Dashboard from '@/pages/Dashboard'
import EdgeModuleContainer from '@/pages/ExampleMicroModule'
import Login from '@/pages/Login'
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
    path: '/login',
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
            path: '/settings/customers',
            element: <CustomerCompany />,
            breadCrumbName: 'Customers'
          },
          { path: '/settings/customers/:id', element: <CompanyPage /> }
        ]
      },
      {
        path: 'example/*',
        element: <EdgeModuleContainer />
      },
      {
        path: 'ports/*',
        element: <ModulePortContainer />
      }
    ]
  }
]
export const router = createBrowserRouter(routes)
