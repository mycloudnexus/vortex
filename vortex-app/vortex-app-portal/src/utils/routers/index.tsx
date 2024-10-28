import Layout from '@/components/Layout'
import Dashboard from '@/pages/Dashboard'
import EdgeModuleContainer from '@/pages/ExampleMicroModule'
import { createBrowserRouter } from 'react-router-dom'
import Login from '@/components/Access/Login'

export const router = createBrowserRouter([
  {
    path: '/:organization/login',
    element: <Login />
  },
  {
    path: '',
    element: <Layout />,
    children: [
      {
        path: '',
        element: <Dashboard />
      },

      {
        path: 'example/*',
        element: <EdgeModuleContainer />
      }
    ]
  }
])
