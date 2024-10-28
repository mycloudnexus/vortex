import { create } from 'zustand'

type AppStore = {
  appLogo: string
  setAppLogo: (appLogo: string) => void
  currentCompany?: {
    id: string
    name: string
  }
  setCurrentCompany: (c: { id: string; name: string }) => void
  currentUser: Record<string, string>
  mainColor: string
}

export const useAppStore = create<AppStore>()((set) => ({
  appLogo: '',
  setAppLogo: (appLogo: string) => set({ appLogo }),
  currentCompany: {
    id: 'po',
    name: 'poping'
  },
  setCurrentCompany: (currentCompany) => set({ currentCompany }),
  currentUser: {
    name: 'Tim Tim'
  },
  mainColor: '#FF7900'
}))
