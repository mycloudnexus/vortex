declare module '*.svg' {
  import * as React from 'react'

  export const ReactComponent: React.FunctionComponent<React.SVGProps<SVGSVGElement> & { title?: string }>

  const src: string
  export default src
}

declare module '*.module.css'
declare module '*.module.scss'
declare module '*.png'
declare interface Window {
  portalConfig: Record<string, unknown>
  portalLoggedInUser: Record<string, unknown>
  portalAccessRoles: Record<string, unknown>
  portalToken: string
}
