import type { ReactElement } from 'react'
import type { ICompany, CreateOrganizationRequestBody } from '@/services/types'

import { Fragment, useState } from 'react'

import { useAppStore } from '@/stores/app.store'
import { useNavigate } from 'react-router-dom'

import { ReactComponent as CCIcon } from '@/assets/icon/customer-company.svg'
import { ReactComponent as CCClose } from '@/assets/icon/close-circle.svg'
import { ReactComponent as CCStatus } from '@/assets/icon/status.svg'
import { ReactComponent as CCEmpty } from '@/assets/icon/customer-company-empty.svg'
import { ReactComponent as CCWarning } from '@/assets/icon/warning-circle.svg'
import Text from '@/components/Text'

import { Button, Flex, Form, message, Space, TableProps, Typography } from 'antd'

import { StyledModal, StyledTable, StyledWrapper } from '../components/styled'
import CustomerCompanyModal from '../components/CustomerModal'
import Tooltip from '../components/Tooltip'
import { useAddOrganization, useGetCompanyList, useUpdateOrganization } from '@/hooks/company'
import { useQueryClient } from 'react-query'

const createColumns = (
  handleOpenModify: (record: ICompany) => void,
  handleOpenActivate: (key: string) => void,
  handleOpenDeactivate: (key: string) => void
): TableProps<ICompany>['columns'] => [
  {
    title: 'Name',
    dataIndex: 'display_name',
    key: 'display_name'
  },
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id'
  },
  {
    title: 'Short name & URL',
    dataIndex: 'name',
    key: 'name',
    render: (_, { name, id }) => (
      <Flex gap={5} align='center'>
        <Text.NormalMedium style={{ textOverflow: 'ellipsis', overflow: 'hidden' }}>{name}</Text.NormalMedium>
        <Tooltip orgId={id} color='#FFF' placement='topLeft' />
      </Flex>
    )
  },
  {
    title: 'Status',
    key: 'metadata.status',
    dataIndex: 'metadata.status',
    render: (_, record) => {
      const status = record?.metadata?.status
      return (
        <>
          {status === 'ACTIVE' ? (
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
    }
  },
  {
    title: 'Action',
    key: 'action',
    render: (_, record) => {
      const { id } = record
      const status = record?.metadata?.status
      const handleModify = (e: React.MouseEvent<HTMLElement, MouseEvent>, record: ICompany): void => {
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
          {status === 'ACTIVE' ? (
            <Fragment>
              <Button
                onClick={(e) => handleModify(e, record)}
                variant='link'
                type='link'
                color='danger'
                style={{ padding: '4px 0' }}
                data-testid='handle-modify'
              >
                Modify
              </Button>
              <Button
                onClick={(e) => handleDeactivate(e, id)}
                variant='link'
                type='link'
                color='danger'
                style={{ padding: '4px 0' }}
                data-testid='handle-deactivate'
              >
                Deactivate
              </Button>
            </Fragment>
          ) : (
            <Button
              onClick={(e) => handleActivate(e, id)}
              variant='link'
              type='link'
              color='danger'
              style={{ padding: '4px 0' }}
              data-testid='handle-activate'
            >
              Activate
            </Button>
          )}
        </Space>
      )
    }
  }
]
const errorMessage = 'The system has encountered an anomaly, please contact your system support team.'
const CustomerCompany = (): ReactElement => {
  const queryClient = useQueryClient()
  const [addForm] = Form.useForm<CreateOrganizationRequestBody>()
  const [editForm] = Form.useForm()
  const { data, isFetching } = useGetCompanyList()
  const companies = data?.data?.data ?? []
  const { mutate } = useAddOrganization()
  const { mutate: updateMutate } = useUpdateOrganization()
  const { mainColor } = useAppStore()
  const navigate = useNavigate()
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false)
  const [isWarning, setIsWarning] = useState<boolean>(false)
  const [isConfigLogin, setIsConfigLogin] = useState<boolean>(false)
  const [isUpdateModalOpen, setIsUpdateModalOpen] = useState<boolean>(false)
  const [isDeactivate, setIsDeactivate] = useState<boolean>(false)
  const [updateValue, setUpdateValue] = useState<Partial<ICompany>>({
    branding: {
      colors: { page_background: '', primary: '' },
      logo_url: ''
    },
    display_name: '',
    id: '',
    metadata: {
      connectionId: '',
      status: '',
      strategy: ''
    },
    name: ''
  })
  const [key, setKey] = useState<string>('')
  const handleOpenWarning = (): void => setIsWarning(true)
  const handleCloseWarning = (): void => setIsWarning(false)

  const handleSubmitConfigLogin = (): void => {
    setIsConfigLogin(false)
    navigate(`/settings/customers/${updateValue.display_name}`, { state: { record: updateValue } })
  }
  const handleCloseConfigModal = (): void => setIsConfigLogin(false)

  const handleOpenModal = (record: CreateOrganizationRequestBody): void => {
    // setUpdateValue(record)
    console.log(record)
    setIsConfigLogin(true)
  }

  const handleOk = async (): Promise<void> => {
    const values = await addForm.validateFields()
    mutate(
      {
        ...values,
        display_name: values.display_name,
        name: values.name.toLowerCase()
      },
      {
        onSuccess: async () => {
          await queryClient.invalidateQueries('getCompanyList')
          handleCancel()
          handleOpenModal(values)
        },
        onError: () => {
          message.error(errorMessage, 2)
        }
      }
    )
  }
  const handleCancel = () => {
    addForm.resetFields()
    setIsModalOpen(false)
  }
  const showModal = () => setIsModalOpen(true)
  const openUpdateModal = (record: ICompany): void => {
    editForm.setFieldsValue(record)
    setUpdateValue(record)
    setIsUpdateModalOpen(true)
  }

  const handleUpdate = async (): Promise<void> => {
    const values = await editForm.validateFields()
    updateMutate(
      {
        id: updateValue.id as string,
        request_body: {
          display_name: values.display_name
        }
      },
      {
        onSuccess: async () => {
          await queryClient.invalidateQueries('getCompanyList')
        },
        onError: () => {
          message.error(errorMessage)
        }
      }
    )
    closeUpdateModal()
  }
  const closeUpdateModal = (): void => {
    editForm.resetFields()
    setIsUpdateModalOpen(false)
  }
  const handleActivate = (key: string): void => {
    updateMutate(
      {
        id: key,
        request_body: {
          status: 'ACTIVE'
        }
      },
      {
        onSuccess: () => {
          queryClient.invalidateQueries('getCompanyList')
          showSuccessMessage('activated')
        },
        onError: () => {
          message.error(errorMessage, 2)
        }
      }
    )
  }
  const handleDeactivate = (key: string): void => {
    setKey(key)
    handleOpenDeactivate()
  }
  const handleClick = (): void => {
    if (companies.length > 200) {
      return handleOpenWarning()
    }
    showModal()
  }
  const showSuccessMessage = (status: string): void => {
    message.success(`The customer was ${status} successfully`, 2)
  }

  const handleCloseDeactivate = (): void => setIsDeactivate(false)
  const handleOpenDeactivate = (): void => setIsDeactivate(true)

  const handleDeactivateSubmit = (): void => {
    updateMutate(
      {
        id: key,
        request_body: {
          status: 'INACTIVE'
        }
      },
      {
        onSuccess: () => {
          queryClient.invalidateQueries('getCompanyList')
          handleCloseDeactivate()
          showSuccessMessage('deactivated')
        },
        onError: () => {
          message.error(errorMessage, 2)
          handleCloseDeactivate()
        }
      }
    )
  }
  const handleOnRowClick = (record: ICompany): void => {
    const { display_name } = record
    navigate(`/settings/customers/${display_name}`, { state: { record: record } })
  }
  return (
    <Flex style={{ width: '70%', margin: '0 auto' }} vertical justify='start' align='center' gap={20}>
      <Flex justify='space-between' style={{ width: '100%' }} align='center'>
        <Flex gap={15} align='center'>
          <StyledWrapper $backgroundColor={mainColor}>
            <CCIcon />
          </StyledWrapper>
          <Typography.Title level={3} style={{ margin: 0 }}>
            Customers
          </Typography.Title>
        </Flex>
        <Button variant='solid' color='primary' onClick={handleClick} data-testid='add-button'>
          Add customer
        </Button>
      </Flex>

      <StyledTable
        loading={isFetching}
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
        rowKey='id'
        style={{ width: '100%' }}
        locale={{
          emptyText: (
            <Flex align='center' justify='center' vertical>
              <CCEmpty />
              <Text.NormalLarge color='#000'>No customer</Text.NormalLarge>
            </Flex>
          )
        }}
        scroll={{ y: 560 }}
      />

      <CustomerCompanyModal
        title='Add customer'
        name='add_customer_company'
        companies={companies}
        form={addForm}
        handleCancel={handleCancel}
        handleOk={handleOk}
        initialValues={{ title: '', shortName: '' }}
        isModalOpen={isModalOpen}
        data-testid='add-modal'
      />

      <CustomerCompanyModal
        title='Modify customer'
        name='modify_customer_company'
        companies={companies}
        form={editForm}
        handleCancel={closeUpdateModal}
        handleOk={handleUpdate}
        initialValues={{ title: '', shortName: '' }}
        isModalOpen={isUpdateModalOpen}
        type='update'
        data-testid='update-modal'
      />
      <StyledModal
        centered
        title={
          <Flex align='center' gap={5}>
            <CCClose />
            <Text.NormalMedium style={{ fontSize: '16px', fontWeight: 'bold' }}>
              Cannot create more customer
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
        data-testid='confirm-user'
      >
        <Flex gap={10} vertical>
          <Text.NormalMedium>
            Users from this customer cannot login Vortex yet. <br /> You can select user login method for the customer
            and invite user or configure SSO accordingly
          </Text.NormalMedium>
        </Flex>
      </StyledModal>

      <StyledModal
        centered
        title={
          <Flex align='center' gap={5}>
            <CCWarning />
            <Text.NormalMedium style={{ fontSize: '16px', fontWeight: 'bold' }}>
              Are you sure to deactivate this customer?
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
        data-testid='deactivate-modal'
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
    </Flex>
  )
}

export default CustomerCompany
