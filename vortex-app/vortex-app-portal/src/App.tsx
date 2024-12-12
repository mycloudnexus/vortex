import { FC, useEffect } from 'react'
import { RouterProvider } from 'react-router-dom'
import { QueryClientProvider } from 'react-query'
import { router } from './routers'
import { ConfigProvider } from 'antd'
import { queryClient } from '@/utils/helpers/request-query'
import { useAppStore } from './stores/app.store'
import { AuthProvider } from './components/Access'
import { ENV } from '@/constant'
import logo from '@/assets/logo.png'

const App: FC = () => {
  const { mainColor } = useAppStore()
  useEffect(() => {
    const link: any = document.querySelector("link[rel*='icon']") || document.createElement('link')
    link.type = 'image/x-icon'
    link.rel = 'shortcut icon'
    link.href = ENV.COMPANY_LOGO_URL ?? logo
    document.getElementsByTagName('head')[0].appendChild(link)
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
        <AuthProvider>
          <RouterProvider router={router} />
        </AuthProvider>
      </ConfigProvider>
    </QueryClientProvider>
  )
}

export default App
