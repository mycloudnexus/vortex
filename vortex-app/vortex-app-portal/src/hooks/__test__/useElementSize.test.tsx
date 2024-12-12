import { useState } from 'react'
import useElementSize from '@/hooks/useElementSize'
import { render, fireEvent, screen } from '@/test/setupTest'
Object.defineProperties(window.HTMLElement.prototype, {
  offsetLeft: {
    get() {
      return parseFloat(this.style.marginLeft) || 0
    }
  },
  offsetTop: {
    get() {
      return parseFloat(this.style.marginTop) || 0
    }
  },
  offsetHeight: {
    get() {
      return parseFloat(this.style.height) || 0
    }
  },
  offsetWidth: {
    get() {
      return parseFloat(this.style.width) || 0
    }
  }
})

const TestComponent = () => {
  const [width, setWidth] = useState(40)
  const [height, setHeight] = useState(35)
  const [{ width: widthNumber, height: heightNumber }, ref] = useElementSize()

  return (
    <>
      <div style={{ width, height }} ref={ref} data-testid='test'>
        {widthNumber} {heightNumber}
      </div>
      <button
        onClick={() => {
          setWidth(80)
          setHeight(70)
        }}
      >
        change width!
      </button>
    </>
  )
}
test('useElementSize hook', async () => {
  const { container } = render(<TestComponent />)
  expect(container).toBeInTheDocument()
  const element = screen.getByTestId('test')
  expect(element.innerHTML).toBe('40 35')
  fireEvent.click(screen.getByText('change width!'))
  fireEvent(window, new Event('resize'))
  expect(element.innerHTML).toBe('80 70')
})
