import { Flex, Typography, type TabsProps } from 'antd'
import type { ReactElement } from 'react'

import { ReactComponent as CPLock } from '@/assets/icon/lock.svg'
import { ReactComponent as CPsso } from '@/assets/icon/sso.svg'
import { ReactComponent as CCIcon } from '@/assets/icon/customer-company.svg'
import { ReactComponent as CCInfo } from '@/assets/icon/info.svg'

import CardTab from '../components/CardTab'
import { useAppStore } from '@/stores/app.store'
import { StyledInfoTab, StyledTabs, StyledWrapper } from '../components/styled'
import Text from '@/components/Text'
import { useLocation } from 'react-router-dom'
import { useGetOrganizationById } from '@/hooks/company'
import SSOForm from '../components/SSOForm'

const CompanyPage = (): ReactElement => {
  const { mainColor } = useAppStore()
  const location = useLocation()
  const orgId = location.state?.record?.id
  const { isFetching, data } = useGetOrganizationById(orgId, {
    enabled: !!orgId
  })
  const loginMethod = data?.data?.metadata?.loginType
  console.log(data?.data?.metadata?.loginType)
  const items: TabsProps['items'] = [
    {
      key: '1',
      label: (
        <StyledInfoTab>
          <Flex vertical gap={10}>
            <Text.NormalMedium style={{ textAlign: 'start' }}>Please select login method</Text.NormalMedium>
            <Flex style={{ padding: '9px 12px 9px 12px', backgroundColor: '#F2F5FE', borderRadius: '4px' }} gap={10}>
              <div style={{ padding: '3px' }}>
                <CCInfo />
              </div>
              <Text.NormalMedium style={{ textAlign: 'start', whiteSpace: 'wrap' }}>
                Only one login method supported for each customer
              </Text.NormalMedium>
            </Flex>
          </Flex>
        </StyledInfoTab>
      ),
      disabled: true
    },
    {
      key: '2',
      label: (
        <CardTab
          title='Invite by email'
          description='User login by username and password'
          isEnabled={loginMethod === 'email'}
          icon={<CPLock />}
        />
      ),
      children: (
        <Flex vertical justify='start' align='center' gap={20}>
          table
        </Flex>
      )
    },
    {
      key: '3',
      label: (
        <CardTab
          title='SSO-'
          titleExtension='SAML 2.0'
          description='User login by configured SSO'
          isEnabled={loginMethod === 'sso'}
          icon={<CPsso />}
        />
      ),
      children: (
        <Flex vertical justify='start' align='flex-start' gap={20}>
          <Flex justify='flex-start' style={{ width: '100%' }} align='center'>
            <Typography.Title level={3} style={{ margin: 0 }}>
              Set up SSO
            </Typography.Title>
          </Flex>
          <SSOForm loginMethod={data?.data?.metadata?.loginType} />
        </Flex>
      )
    }
  ]

  if (isFetching) return <>Loading</>
  return (
    <Flex style={{ width: '70%', margin: '0 auto' }} vertical justify='start' align='center' gap={20}>
      <Flex justify='space-between' style={{ width: '100%' }} align='center'>
        <Flex gap={15} align='center'>
          <StyledWrapper $backgroundColor={mainColor}>
            <CCIcon />
          </StyledWrapper>
          <Typography.Title level={3} style={{ margin: 0 }}>
            {data?.data?.display_name ?? '--'}
          </Typography.Title>
        </Flex>
      </Flex>

      <StyledTabs items={items} defaultActiveKey='2' tabPosition='left' $mainColor={mainColor} />
    </Flex>
  )
}

export default CompanyPage
