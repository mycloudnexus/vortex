import { lazyLoadFederatedModule } from '@/utils/moduleLoader'
import { Spin } from 'antd'
import { Suspense } from 'react'

const ModuleDashboard = lazyLoadFederatedModule('vortex_module_template', undefined)

const ModuleDashboardContainer = (props: any): JSX.Element => {
  return <Suspense fallback={<Spin />}>{ModuleDashboard && <ModuleDashboard baseUrl='example' {...props} />}</Suspense>
}

export default ModuleDashboardContainer
