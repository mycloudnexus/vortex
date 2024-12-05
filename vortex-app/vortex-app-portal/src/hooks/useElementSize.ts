import { useState, useEffect, useRef, MutableRefObject } from 'react'

const useElementSize: () => [{ width: number; height: number }, MutableRefObject<any>] = () => {
  const ref = useRef<any>(null)
  const [size, setSize] = useState({
    width: 0,
    height: 0
  })

  useEffect(() => {
    const updateSize = () => {
      if (ref.current) {
        setSize({
          width: ref.current.offsetWidth as number,
          height: ref.current.offsetHeight as number
        })
      }
    }
    window.addEventListener('resize', updateSize)
    updateSize()

    return () => window.removeEventListener('resize', updateSize)
  }, [])

  return [size, ref]
}

export default useElementSize
