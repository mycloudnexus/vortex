import { useAppStore } from '@/stores/app.store'
const useInitialData = () => {
  const { roleList, user, currentAuth0User } = useAppStore()

  return { roleList, user, currentAuth0User }
}

export default useInitialData
