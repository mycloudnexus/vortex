import Layout from '@/components/Layout'
import Dashboard from '@/pages/Dashboard'
import EdgeModuleContainer from '@/pages/ExampleMicroModule'
import { createBrowserRouter } from 'react-router-dom'

export const router = createBrowserRouter([
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
