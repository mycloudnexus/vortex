import { lazyLoadFederatedModule } from '@/utils/moduleLoader'
import { Spin } from 'antd'
import { Suspense } from 'react'

const ModuleDashboard = lazyLoadFederatedModule('vortex_pricing', undefined)

const ModulePricingContainer = (props: any): JSX.Element => {
  return <Suspense fallback={<Spin />}>{ModuleDashboard && <ModuleDashboard baseUrl='/pricing' {...props} />}</Suspense>
}

export default ModulePricingContainer
