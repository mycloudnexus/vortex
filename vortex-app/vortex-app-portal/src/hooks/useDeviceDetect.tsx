import { useEffect, useState } from 'react'

const DEVICE_SIZE = {
  mobile: {
    min: 0,
    max: 767
  },
  table: {
    min: 768,
    max: 1023
  },
  desktop: {
    min: 1024
  }
}

// TODO: this is causing re-render every time the windows resize
const useDeviceDetect = () => {
  const [windowSize, setWindowSize] = useState({
    width: 0,
    height: 0
  })

  useEffect(() => {
    const handleResize = () => {
      setWindowSize({
        width: window.innerWidth,

        height: window.innerHeight
      })
    }

    window.addEventListener('resize', handleResize)

    handleResize()

    return () => window.removeEventListener('resize', handleResize)
  }, [])

  return {
    isMobile: windowSize.width >= DEVICE_SIZE.mobile.min && windowSize.width <= DEVICE_SIZE.mobile.max,
    isTablet: windowSize.width >= DEVICE_SIZE.table.min && windowSize.width <= DEVICE_SIZE.table.max,
    isDesktop: windowSize.width >= DEVICE_SIZE.desktop.min
  }
}

export default useDeviceDetect
