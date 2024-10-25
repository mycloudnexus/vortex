import { create } from 'zustand'

export type AuthUser = {
  email: string
  email_verified: boolean
  family_name: string
  given_name: string
  name: string
  nickname: string
  org_id: string
  picture: string
  sub: string
  updated_at: string
}

type AppStore = {
  appLogo: string
  setAppLogo: (appLogo: string) => void
  currentCompany?: {
    id: string
    name: string
  }
  setCurrentCompany: (c: { id: string; name: string }) => void
  currentUser: AuthUser | null
  setCurrentUser: (c: AuthUser | null) => void
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
  setCurrentUser: (currentUser) => set({ currentUser }),
  currentUser: null,
  mainColor: '#FF7900'
}))
