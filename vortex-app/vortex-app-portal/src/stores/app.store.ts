import { create } from 'zustand'
import type { User } from './type'

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
  currentAuth0User: AuthUser | null
  setCurrentAuth0User: (c: AuthUser | null) => void
  mainColor: string
  user: User | null
  setUser: (c: User | null) => void
}

export const useAppStore = create<AppStore>()((set) => ({
  appLogo: '',
  setAppLogo: (appLogo: string) => set({ appLogo }),
  currentCompany: {
    id: 'po',
    name: 'poping'
  },
  setCurrentCompany: (currentCompany) => set({ currentCompany }),
  setCurrentAuth0User: (currentAuth0User) => set({ currentAuth0User }),
  currentAuth0User: null,
  mainColor: '#FF7900',
  user: null,
  setUser: (user) => set({ user })
}))
