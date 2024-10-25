import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import { AuthProvider } from './components/AuthProvider'
// import Authenticate from './components/AuthProvider/Authenticate'

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <AuthProvider>
      <App />
      {/* <Authenticate></Authenticate> */}
    </AuthProvider>
  </React.StrictMode>
)
