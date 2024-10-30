import Layout from '@/components/Layout'
import Dashboard from '@/pages/Dashboard'
import EdgeModuleContainer from '@/pages/ExampleMicroModule'
import NotFound from '@/pages/NotFound'
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
      },
      {
        path: '/pricing',
        element: <EdgeModuleContainer />
      },
      {
        path: '*',
        element: <NotFound />
      }
    ]
  }
])
