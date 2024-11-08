import { FC, useEffect } from 'react'
import { RouterProvider } from 'react-router-dom'
import { QueryClientProvider } from 'react-query'
import { router } from './routers'
import { ConfigProvider } from 'antd'
import { queryClient } from '@/utils/helpers/request-query'
import { useAppStore } from './stores/app.store'

const App: FC = () => {
  const { mainColor } = useAppStore()

  useEffect(() => {
    if (window) {
      window.portalToken = 'token'
    }
  }, [])

  return (
    <QueryClientProvider client={queryClient}>
      <ConfigProvider
        theme={{
          token: {
            borderRadius: 0,
            colorPrimary: mainColor
          }
        }}
      >
        <RouterProvider router={router} />
      </ConfigProvider>
    </QueryClientProvider>
  )
}

export default App
