import Layout from '@/components/Layout'
import Dashboard from '@/pages/Dashboard'
import EdgeModuleContainer from '@/pages/ExampleMicroModule'
import { createBrowserRouter, RouteObject } from 'react-router-dom'
import Login from '@/pages/Login'

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
        path: 'example/*',
        element: <EdgeModuleContainer />
      }
    ]
  }
]
export const router = createBrowserRouter(routes)
