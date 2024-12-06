import { create } from 'zustand'
import type { AppStore } from './type'
import { ENV } from '@/constant'

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
  mainColor: ENV.THEME_COLOR || '#FF7900',
  downstreamUser: null,
  setDownstreamUser: (downstreamUser) => set({ downstreamUser }),
  roleList: null,
  setRoleList: (roleList) => set({ roleList }),
  userType: 'reseller',
  setuserType: (userType) => set({ userType }),
  customerUser: null,
  setCustomerUser: (customerUser) => set({ customerUser }),
  customerCompanies: [],
  setCustomerCompanies: (customerCompanies) => set({ customerCompanies }),

}))
