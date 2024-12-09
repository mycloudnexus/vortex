import Text from '@/components/Text'
import { UploadOutlined } from '@ant-design/icons'
import { Button, Flex, Form, FormProps, Input, Select, Switch, Upload, UploadFile } from 'antd'
import { Fragment, useState, type ReactElement } from 'react'
import { StyledButton, StyledForm, StyledFormItem } from './styled'
import { useAppStore } from '@/stores/app.store'
import ConfirmationModal from './ConfirmationModal'

interface SSOForm {
  signInEndpoint: string
  signingCert: File | null
  userIdAttribute: string
  signatureAlgorithm: string
  digestAlgorithm: string
  signRequest: boolean
  binding: string
}

interface SSOFormProps {
  loginMethod: string | undefined
}

const SSOForm = ({ loginMethod }: SSOFormProps): ReactElement => {
  const [form] = Form.useForm()
  const { mainColor } = useAppStore()
  const [warning, setWarning] = useState<boolean>(false)
  const [isSubmitted, setIsSubmitted] = useState<boolean>(false)
  const [isButtonDisabled, setIsButtonDisabled] = useState<boolean>(true)
  const [showSignRequest, setShowSignRequest] = useState<boolean>(false)
  const [fileList, setFileList] = useState<UploadFile[]>([])
  const changeValues: FormProps['onValuesChange'] = (
    changedValues: Partial<SSOForm>,
    allValues: Partial<SSOForm>
  ): void => {
    const { signRequest } = changedValues
    if (signRequest !== undefined) {
      setShowSignRequest(signRequest)
    }

    const { signInEndpoint, signingCert } = allValues

    if (signInEndpoint && signingCert) {
      form
        .validateFields(['signInEndpoint', 'signingCert'], { validateOnly: true })
        .then(() => setIsButtonDisabled(true))
        .catch(() => setIsButtonDisabled(false))
    } else {
      setIsButtonDisabled(true)
    }
  }

  const handleCloseWarning = (): void => {
    form.resetFields()
    setWarning(false)
  }
  const handleOpenWarning = (): void => setWarning(true)
  const handleWarningSubmit = (): void => {
    setIsSubmitted(true)
    handleCloseWarning()
    setIsButtonDisabled(true)
  }

  const handleSubmit = async (): Promise<void> => {
    try {
      //This will be used on integration
      // const values = await form.validateFields()
      if (loginMethod === 'email') {
        handleOpenWarning()
        return
      }

      setIsSubmitted(true)
      setIsButtonDisabled(true)
    } catch (error) {
      console.log(error)
    }
  }

  const handleRemove = () => {
    form.setFieldsValue({ signCertificate: undefined })
    setFileList([])
  }
  return (
    <Fragment>
      <StyledForm form={form} name='sso-form' layout='vertical' wrapperCol={{ span: 18 }} onValuesChange={changeValues}>
        <Form.Item
          name='signInEndpoint'
          label={
            <Flex align='center' gap={5}>
              Sign in URL<span style={{ color: 'red' }}>*</span>
            </Flex>
          }
          required={false}
          rules={[
            { required: true, message: 'Customer name cannot be empty' },
            {
              pattern: /^(https?:\/\/)?([\w-]+\.)+[\w-]{2,}(\/[^\s]*)?$/,
              message: 'Please enter a valid URL format (e.g., https://example.com/login)'
            }
          ]}
          help={
            <Flex vertical gap={5} style={{ paddingTop: '10px' }}>
              {/* eslint-disable-next-line react/no-unescaped-entities */}
              <span>Auth0 redirect to reseller company's login page</span>
              <span>Format: protocol :// hostname[:port] / path / [:parameters][?query]#fragment</span>
            </Flex>
          }
        >
          <Input type='email' placeholder='https://sample.example.com/login' data-testid='sso-url' />
        </Form.Item>
        <Form.Item
          name='signingCert'
          label={
            <Flex align='center' gap={5}>
              X509 Signing Certificate<span style={{ color: 'red' }}>*</span>
            </Flex>
          }
          required={false}
          rules={[
            {
              required: true,
              message: 'Must be real and valid valid'
            }
          ]}
          help='Must be real and valid certificate. SAMLP server public key encoded in PEM or CER format.'
        >
          <Upload
            beforeUpload={(file) => {
              form.setFieldsValue({ signingCert: file })
              setFileList([file])
              return false
            }}
            onRemove={handleRemove}
            fileList={fileList}
            data-testid='upload-docs'
            accept='.cert, .pem'
          >
            <Button icon={<UploadOutlined />}>Click to Upload</Button>
          </Upload>
        </Form.Item>
        <Form.Item
          name='userIdAttribute'
          label={
            <Flex align='center' gap={5}>
              User ID Attribute
            </Flex>
          }
          required={false}
          help={`Optional: This is the attribute in the SAML token that will be mapped to the user_id property in Auth0`}
        >
          <Input
            type='email'
            placeholder='http://schemas.xmlsoap.org/ws/2005/05/identity/claims/nameidentifier'
            data-testid='id-attribute'
          />
        </Form.Item>
        <StyledFormItem name='signRequest' label='Sign Request'>
          <Switch data-testid='switch-request' />
        </StyledFormItem>
        <Text.NormalMedium color='rgba(0, 0, 0, 0.45)' style={{ display: 'inline-block', width: '75%' }}>
          When enabled, the SAML authentication request will be signed. Download the <a href='/'>certificate</a> and
          give it to SAMLP that will receive the signed asserion so it can validate the signature
        </Text.NormalMedium>
        {showSignRequest ? (
          <Flex vertical style={{ background: '#f0f0f0', width: '75%', padding: '15px' }}>
            <Form.Item name='signatureAlgorithm' label='Sign Request Algorithm'>
              <Select
                options={[
                  {
                    value: 'rsa-sha256',
                    label: 'RSA-SHA256'
                  },
                  {
                    value: 'rsa-sha1',
                    label: 'RSA-SHA1'
                  },
                  {
                    value: 'sha1',
                    label: 'SHA1'
                  }
                ]}
                placeholder='Please select'
                style={{ width: '38rem' }}
              />
            </Form.Item>
            <Form.Item name='digestAlgorithm' label='Sign Request Algorithm Digest'>
              <Select
                options={[
                  {
                    value: 'sha256',
                    label: 'SHA256'
                  },
                  {
                    value: 'sha1',
                    label: 'SHA1'
                  }
                ]}
                placeholder='Please select'
                style={{ width: '38rem' }}
              />
            </Form.Item>
          </Flex>
        ) : (
          <></>
        )}
        <Form.Item name='binding' label='Protocol Binding'>
          <Select
            options={[
              {
                value: 'http-redirect',
                label: 'HTTP-Redirect'
              },
              {
                value: 'http-post',
                label: 'HTTP-POST'
              }
            ]}
            placeholder='Please select'
          />
        </Form.Item>
        <StyledButton
          $width={'15%'}
          $backgroundColor={mainColor}
          onClick={handleSubmit}
          variant='solid'
          disabled={isButtonDisabled}
          data-testid='submit-button'
        >
          Submit
        </StyledButton>

        {isSubmitted && (
          <Flex vertical gap={5} justify='flex-start' style={{ paddingTop: '10px' }}>
            <Text.BoldMedium>Please check below login URL for this customer</Text.BoldMedium>
            <Text.NormalMedium style={{ color: '#000', opacity: '30%' }}>
              https://dev-demo-portal.wl.dev.consolecore.io/org_NvvU4CRX5CTD1cNR/login
            </Text.NormalMedium>
          </Flex>
        )}
      </StyledForm>
      <ConfirmationModal
        text={
          <>
            Are you sure to enable SSO-SAML 2.0 login method? All the invited users will be deprecated.
            <br /> Continue?
          </>
        }
        handleCancel={handleCloseWarning}
        handleOk={handleWarningSubmit}
        open={warning}
        data-testid='warning-modal'
      />
    </Fragment>
  )
}

export default SSOForm
