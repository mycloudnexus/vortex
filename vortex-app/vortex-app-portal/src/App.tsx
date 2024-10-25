import { FC } from 'react'
import { RouterProvider } from 'react-router-dom'
import { router } from './utils/routers'
import { ConfigProvider } from 'antd'
import { useAppStore } from './stores/app.store'

const App: FC = () => {
  const { mainColor } = useAppStore()
  return (
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
  )
}

export default App
