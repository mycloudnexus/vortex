import type { ReactElement } from 'react'
import { Flex, Form, FormInstance, ModalProps } from 'antd'

import { CustomInput, StyledModal } from './styled'
import Tooltip from './Tooltip'
import { ICompany } from '@/services/types'

export interface CustomerCompanyModalProps<T> extends ModalProps {
  title: string
  isModalOpen: boolean
  handleOk: () => void
  handleCancel: () => void
  form: FormInstance
  initialValues: T
  companies: ICompany[]
  name: string
  type?: 'add' | 'update'
}

const CustomerCompanyModal = <T extends object>({
  title,
  isModalOpen,
  handleOk,
  handleCancel,
  form,
  initialValues,
  companies,
  name,
  type = 'add',
  ...rest
}: CustomerCompanyModalProps<T>): ReactElement => {
  return (
    <StyledModal
      centered
      $withDivider
      title={title}
      open={isModalOpen}
      onOk={handleOk}
      onCancel={handleCancel}
      destroyOnClose
      {...rest}
    >
      <Form form={form} name={name} initialValues={initialValues} layout='vertical' style={{ padding: '20px' }}>
        <Form.Item
          name='display_name'
          label={
            <Flex align='center' gap={5}>
              Customer company name<span style={{ color: 'red' }}>*</span>
            </Flex>
          }
          required={false}
          rules={[
            { required: true, message: 'Customer name cannot be empty' },
            {
              validator: async (_, value) => {
                const exist = companies.some((company) => company.display_name === value)
                if (exist) {
                  return Promise.reject(new Error('Customer name cannot be duplicated'))
                }
                return Promise.resolve()
              }
            }
          ]}
        >
          <CustomInput placeholder='Please enter' data-testid='customer-name' maxLength={255} />
        </Form.Item>

        <Form.Item
          name='name'
          label={
            <Flex gap={5} align='center'>
              Customer company URL short name
              <Tooltip
                title='This is used for login url generation. Please be careful as it cannot be changed once provided.'
                color='black'
                placement='topRight'
              />
              <span style={{ color: 'red' }}>*</span>
            </Flex>
          }
          required={false}
          rules={[
            { required: true, message: ' Customer company URL short name cannot be empty' },
            {
              pattern: /^[a-z0-9-]{1,20}$/,
              message: 'Only lowercase letters, numbers, and hyphens are allowed. No more than 20 characters.'
            },
            {
              validator: async (_, value) => {
                const exist = companies.some((company) => company.name === value)
                const isUpdate = type !== 'update'
                if (exist && isUpdate) {
                  return Promise.reject(new Error('Customer shortname cannot be duplicated'))
                }
                return Promise.resolve()
              }
            }
          ]}
          extra='Only lower case letters, numbers, - , allowed. No more than 20 characters in total'
        >
          <CustomInput placeholder='Please enter' disabled={type === 'update'} maxLength={20} />
        </Form.Item>
      </Form>
    </StyledModal>
  )
}

export default CustomerCompanyModal
