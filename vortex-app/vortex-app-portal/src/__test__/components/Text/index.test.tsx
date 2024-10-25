import Text from '@/components/Text'
import { render } from '@/test/setupTest'

test(`Test text component`, async () => {
  const { container } = render(
    <div>
      <Text.NormalLarge>abc</Text.NormalLarge>
      <Text.LightMedium>abcd</Text.LightMedium>
      <Text.Custom color='red'>bcd</Text.Custom>
    </div>
  )
  expect(container).toBeInTheDocument()
})
