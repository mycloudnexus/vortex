import type { ReactElement } from 'react'
import type { TableProps } from 'antd'

import { Fragment, useState } from 'react'

import { ReactComponent as CCIcon } from '@/assets/icon/customer-company.svg'
import { ReactComponent as CCEmpty } from '@/assets/icon/customer-company-empty.svg'
import { ReactComponent as CCStatus } from '@/assets/icon/status.svg'
import { ReactComponent as CCClose } from '@/assets/icon/close-circle.svg'
import { ReactComponent as CCWarning } from '@/assets/icon/warning-circle.svg'
import { useAppStore } from '@/stores/app.store'
import Text from '@/components/Text'

import { Button, Flex, Typography, Space, notification } from 'antd'
import { useNavigate } from 'react-router-dom'
import { Company, useCompanyStore } from '@/stores/company.store'
import { useForm } from 'antd/es/form/Form'
import CustomerCompanyModal from '../components/CustomerModal'
import { StyledButton, StyledModal, StyledTable, StyledWrapper } from '../components/styled'
import Tooltip from '../components/Tooltip'

const createColumns = (
  handleOpenModify: (record: Company) => void,
  handleOpenActivate: (key: string) => void,
  handleOpenDeactivate: (key: string) => void
): TableProps<Company>['columns'] => [
  {
    title: 'Name',
    dataIndex: 'title',
    key: 'title'
  },
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id'
  },
  {
    title: 'Short name & URL',
    dataIndex: 'shortName',
    key: 'shortName',
    render: (_, { shortName }) => (
      <Flex gap={5} align='center'>
        {shortName}
        <Tooltip shortName={shortName} color='#FFF' placement='topLeft' />
      </Flex>
    )
  },
  {
    title: 'Status',
    key: 'status',
    dataIndex: 'status',
    render: (_, { status }) => (
      <>
        {status === 'active' ? (
          <Flex gap={2}>
            <CCStatus style={{ fill: '#00B284' }} />
            <Typography.Text>Active</Typography.Text>
          </Flex>
        ) : (
          <Flex gap={2}>
            <CCStatus style={{ fill: '#668B97' }} />
            <Typography.Text>Inactive</Typography.Text>
          </Flex>
        )}
      </>
    )
  },
  {
    title: 'Action',
    key: 'action',
    render: (_, record) => {
      const handleModify = (e: React.MouseEvent<HTMLElement, MouseEvent>, record: Company): void => {
        e.stopPropagation()
        handleOpenModify(record)
      }
      const handleDeactivate = (e: React.MouseEvent<HTMLElement, MouseEvent>, key: string): void => {
        e.stopPropagation()
        handleOpenDeactivate(key)
      }
      const handleActivate = (e: React.MouseEvent<HTMLElement, MouseEvent>, key: string): void => {
        e.stopPropagation()
        handleOpenActivate(key)
      }
      return (
        <Space size='small'>
          {record.status === 'active' ? (
            <Fragment>
              <Button
                onClick={(e) => handleModify(e, record)}
                variant='link'
                type='link'
                color='danger'
                style={{ padding: '4px 0' }}
              >
                Modify
              </Button>
              <Button
                onClick={(e) => handleDeactivate(e, record.key)}
                variant='link'
                type='link'
                color='danger'
                style={{ padding: '4px 0' }}
              >
                Deactivate
              </Button>
            </Fragment>
          ) : (
            <Fragment>
              <Button
                onClick={(e) => handleActivate(e, record.key)}
                variant='link'
                type='link'
                color='danger'
                style={{ padding: '4px 0' }}
              >
                Activate
              </Button>
            </Fragment>
          )}
        </Space>
      )
    }
  }
]

const CustomerCompany = (): ReactElement => {
  const { mainColor } = useAppStore()
  const { companies, addCompany, updateCompanyRecord, updateCompanyStatus } = useCompanyStore()
  const [form] = useForm()
  const [api, contextHolder] = notification.useNotification({ top: 80 })
  const [isUpdateModalOpen, setIsUpdateModalOpen] = useState<boolean>(false)
  const navigate = useNavigate()
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false)
  const [isWarning, setIsWarning] = useState<boolean>(false)
  const [isConfigLogin, setIsConfigLogin] = useState<boolean>(false)
  const [isDeactivate, setIsDeactivate] = useState<boolean>(false)
  const [updateValue, setUpdateValue] = useState<Company>({
    key: '',
    id: '',
    title: '',
    shortName: '',
    status: 'active'
  })
  const [key, setKey] = useState<string>('')

  const handleSuccessDeactivate = (): void => {
    api.success({
      message: 'Customer company name deactivated',
      placement: 'top',
      showProgress: true,
      pauseOnHover: true,
      closeIcon: false,
      duration: 2
    })
  }

  const handleCloseDeactivate = (): void => setIsDeactivate(false)
  const handleOpenDeactivate = (): void => setIsDeactivate(true)
  const handleDeactivateSubmit = (): void => {
    try {
      updateCompanyStatus(key, 'inactive')
      handleCloseDeactivate()
      handleSuccessDeactivate()
    } catch (error) {
      console.log(error)
    }
  }

  const handleActivate = (key: string): void => {
    setKey(key)
  }
  const handleDeactivate = (key: string): void => {
    setKey(key)
    handleOpenDeactivate()
  }

  const openUpdateModal = (record: Company): void => {
    form.setFieldsValue(record)
    setUpdateValue(record)
    setIsUpdateModalOpen(true)
  }
  const closeUpdateModal = (): void => {
    form.resetFields()
    setIsUpdateModalOpen(false)
  }
  const handleUpdate = async (): Promise<void> => {
    try {
      const values = await form.validateFields()
      updateCompanyRecord({
        ...updateValue,
        title: values.title
      })
      closeUpdateModal()
    } catch (error) {
      console.log('Form validation failed:', error)
    }
  }

  const handleSubmitConfigLogin = (): void => {
    setIsConfigLogin(false)
    navigate(`/settings/customer-company/${updateValue.title}`, { state: { record: updateValue } })
  }
  const handleOpenModal = (record: Company): void => {
    setUpdateValue(record)
    setIsConfigLogin(true)
  }
  const handleCloseConfigModal = (): void => setIsConfigLogin(false)

  const handleOpenWarning = (): void => setIsWarning(true)
  const handleCloseWarning = (): void => setIsWarning(false)

  const showModal = () => {
    setIsModalOpen(true)
  }

  const handleOk = async (): Promise<void> => {
    try {
      const values = await form.validateFields()
      const valuesToAdd: Company = {
        id: 'IDEO2333',
        key: (companies.length + 1).toString(),
        title: values.title,
        shortName: values.shortName,
        status: 'active'
      }
      addCompany(valuesToAdd)
      handleCancel()
      handleOpenModal(valuesToAdd)
    } catch (error) {
      console.log('Form validation failed:', error)
    }
  }

  const handleCancel = () => {
    form.resetFields()
    setIsModalOpen(false)
  }

  const handleClick = (): void => {
    if (companies.length > 200) {
      return handleOpenWarning()
    }
    showModal()
  }
  const handleOnRowClick = (record: Company): void => {
    navigate(`/settings/customer-company/${record.title}`, { state: { record: record } })
  }
  return (
    <Flex style={{ width: '70%', margin: '0 auto' }} vertical justify='start' align='center' gap={20}>
      <Flex justify='space-between' style={{ width: '100%' }} align='center'>
        <Flex gap={15} align='center'>
          <StyledWrapper $backgroundColor={mainColor}>
            <CCIcon />
          </StyledWrapper>
          <Typography.Title level={2} style={{ margin: 0 }}>
            Customer Company
          </Typography.Title>
        </Flex>
        <StyledButton variant='solid' $backgroundColor={mainColor} onClick={handleClick} data-testid='add-button'>
          Add customer company
        </StyledButton>
      </Flex>

      <StyledTable
        columns={createColumns(
          (record) => openUpdateModal(record),
          (key) => handleActivate(key),
          (key) => handleDeactivate(key)
        )}
        dataSource={companies}
        pagination={false}
        onRow={(record) => {
          return {
            onClick: () => handleOnRowClick(record)
          }
        }}
        style={{ width: '100%' }}
        locale={{
          emptyText: (
            <Flex align='center' justify='center' vertical>
              <CCEmpty />
              <Text.NormalLarge color='#000'>No customer company</Text.NormalLarge>
            </Flex>
          )
        }}
      />

      <CustomerCompanyModal
        title='Add customer company'
        name='add_customer_company'
        companies={companies}
        form={form}
        handleCancel={handleCancel}
        handleOk={handleOk}
        initialValues={{ name: '', shortName: '' }}
        isModalOpen={isModalOpen}
      />

      <CustomerCompanyModal
        title='Modify Customer company'
        name='modify_customer_company'
        companies={companies}
        form={form}
        handleCancel={closeUpdateModal}
        handleOk={handleUpdate}
        initialValues={{ name: '', shortName: '' }}
        isModalOpen={isUpdateModalOpen}
        type='update'
      />

      <StyledModal
        centered
        title={
          <Flex align='center' gap={5}>
            <CCClose />
            <Text.NormalMedium style={{ fontSize: '16px', fontWeight: 'bold' }}>
              Cannot create more customer company
            </Text.NormalMedium>
          </Flex>
        }
        open={isWarning}
        okText='Got it'
        onOk={handleCloseWarning}
        closable={false}
        cancelButtonProps={{ style: { display: 'none' } }}
        $containerWidth='25rem'
      >
        <Flex gap={10} vertical style={{ marginLeft: '30px' }}>
          <Text.NormalMedium>Maximum 200 customer companies reached.</Text.NormalMedium>
          <Text.NormalMedium>Any problem, please contact with Console Connect support team</Text.NormalMedium>
        </Flex>
      </StyledModal>

      <StyledModal
        centered
        title={
          <Flex align='center' gap={5}>
            <Text.NormalMedium style={{ fontSize: '16px', fontWeight: 'bold' }}>
              Continue to configure for user login?
            </Text.NormalMedium>
          </Flex>
        }
        open={isConfigLogin}
        okText='Yes, continue'
        onOk={handleSubmitConfigLogin}
        onCancel={handleCloseConfigModal}
        closable={false}
        cancelText='Not now'
        $containerWidth='25rem'
      >
        <Flex gap={10} vertical>
          <Text.NormalMedium>
            Users from this customer company cannot login Vortex yet. <br /> You can select user login method for the
            customer company and invite user or configure SSO accordingly
          </Text.NormalMedium>
        </Flex>
      </StyledModal>

      <StyledModal
        centered
        title={
          <Flex align='center' gap={5}>
            <CCWarning />
            <Text.NormalMedium style={{ fontSize: '16px', fontWeight: 'bold' }}>
              Are you sure to deactivate this customer company?
            </Text.NormalMedium>
          </Flex>
        }
        open={isDeactivate}
        okText='Yes, continue'
        onOk={handleDeactivateSubmit}
        onCancel={handleCloseDeactivate}
        closable={false}
        cancelText='Cancel'
        $containerWidth='30rem'
        okButtonProps={{ style: { backgroundColor: '#FF4D4F' } }}
      >
        <Flex gap={10} vertical style={{ marginLeft: '30px' }}>
          <Text.NormalMedium>
            Please ensure there are no any active services, otherwise the customer will still be charged. <br />
            After deactivation, order history will be kept, but you cannot order new service for this customer. <br />
            The customer users cannot login to Vortex either. <br />
            Continue?
          </Text.NormalMedium>
        </Flex>
      </StyledModal>
      {contextHolder}
    </Flex>
  )
}

export default CustomerCompany
