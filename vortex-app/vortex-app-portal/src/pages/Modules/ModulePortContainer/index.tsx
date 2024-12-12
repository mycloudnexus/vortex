import { useAppStore } from '@/stores/app.store'
import { lazyLoadFederatedModule } from '@/utils/moduleLoader'
import { Spin } from 'antd'
import { Suspense } from 'react'

const ModulePort = lazyLoadFederatedModule('portal_client_port_module')

const ModulePortContainer = (): JSX.Element => {
  const { downstreamUser, roleList, mainColor } = useAppStore()
  return (
    <Suspense fallback={<Spin />}>
      {ModulePort && <ModulePort baseUrl='ports' user={downstreamUser} roleList={roleList} mainColor={mainColor} />}
    </Suspense>
  )
}

export default ModulePortContainer
