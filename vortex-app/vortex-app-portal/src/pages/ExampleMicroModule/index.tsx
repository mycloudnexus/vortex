import { useAppStore } from '@/stores/app.store'
import { lazyLoadFederatedModule } from '@/utils/moduleLoader'
import { Spin } from 'antd'
import { Suspense } from 'react'

const ModulePort = lazyLoadFederatedModule('portal_client_port_module')
console.log('-ModulePort', ModulePort)

const ModulePortContainer = (props: any): JSX.Element => {
  const { roleList, mainColor } = useAppStore()
  return (
    <Suspense fallback={<Spin />}>
      {ModulePort && <ModulePort baseUrl='ports' user={{}} roleList={roleList} mainColor={mainColor} {...props} />}
    </Suspense>
  )
}

export default ModulePortContainer
