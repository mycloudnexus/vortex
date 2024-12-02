/* eslint-disable no-undef */
import React from 'react'
import { ENV } from '@/constant'

function parseModules() {
  const modules = ENV.VORTEX_MODULES
  const parsedModules = []
  if (typeof modules === 'string') {
    const urls = modules.split(',')
    urls.forEach((url) => {
      const parts = url.trim().split('@')
      const module = {
        name: parts[0],
        address: parts[1],
        original: url.trim()
      }
      parsedModules.push(module)
    })
  }
  return parsedModules
}

function loadScope(url, scope) {
  const element = document.createElement('script')
  const promise = new Promise((resolve, reject) => {
    element.src = url
    element.type = 'text/javascript'
    element.async = true
    element.onload = () => resolve(window[scope])
    element.onerror = reject
  })
  document.head.appendChild(element)
  promise.finally(() => document.head.removeChild(element))
  return promise
}

async function loadModule(name, address, scope) {
  try {
    const container = await loadScope(address, name)
    await __webpack_init_sharing__('default')
    await container.init(__webpack_share_scopes__.default)
    const factory = await container.get(scope)
    return factory()
  } catch (error) {
    console.log(error)
  }

  return Promise.reject(`Error when loading external module ${name}`)
}

export function lazyLoadFederatedModule(moduleName, scope = './App', fallbackComponent = null) {
  const parsedModules = parseModules()
  const parsedModule = parsedModules.find((module) => module.name === moduleName)
  if (parsedModule) {
    const { name, address } = parsedModule
    return React.lazy(() => loadModule(name, address, scope))
  }

  if (!parsedModule) {
    return fallbackComponent
  }
}

export function lazyLoadFederatedModuleAsFunction(moduleName, scope = './App') {
  const parsedModules = parseModules()
  const parsedModule = parsedModules.find((module) => module.name === moduleName)

  if (parsedModule) {
    const { name, address } = parsedModule
    return loadModule(name, address, scope)
  }
}
