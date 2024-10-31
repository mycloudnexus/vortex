import { create } from 'zustand'
import type { AppStore } from './type'

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
  setUser: (user) => set({ user }),
  roleList: null,
  setRoleList: (roleList) => set({ roleList })
}))
