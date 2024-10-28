declare module '@/utils/moduleLoader' {
  import React from 'react'

  interface ParsedModule {
    name: string
    address: string
    original: string
  }

  export function parseModules(): ParsedModule[]

  export function loadScope(url: string, scope: string): Promise<any>

  export function loadModule(name: string, address: string, scope: string): Promise<any>

  export function lazyLoadFederatedModule(moduleName: string, scope?: string, fallbackComponent?: React.ReactNode): any

  export function lazyLoadFederatedModuleAsFunction(moduleName: string, scope?: string): Promise<any> | undefined
}
