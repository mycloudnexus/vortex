import type { ReactElement, ReactNode } from 'react'
import type { BreadcrumbItemType, BreadcrumbSeparatorType } from 'antd/es/breadcrumb/Breadcrumb'

import { Link, matchPath, useLocation } from 'react-router-dom'
import { IRouteObject, routes } from '@/routers'

import { Breadcrumb as AntBreadcrumb } from 'antd'
import { styled } from 'styled-components'
import { useAppStore } from '@/stores/app.store'

interface StyledLinkProps {
  isLast: boolean
  mainColor: string
}

const StyledLink = styled(Link).withConfig({
  shouldForwardProp: (prop) => !['isLast', 'mainColor'].includes(prop)
})<StyledLinkProps>`
  color: ${({ isLast, mainColor }) => (isLast ? '#000' : mainColor)} !important;
  font-weight: normal;
  &:hover {
    color: ${({ isLast, mainColor }) => (isLast ? mainColor : '#000')} !important;
  }
`

const StyledBreadCrumb = styled(AntBreadcrumb)`
  padding: 15px 10px;
`

const renderBreadCrumbItems = (
  route: Partial<BreadcrumbItemType & BreadcrumbSeparatorType>,
  _params: object,
  routes: Partial<BreadcrumbItemType & BreadcrumbSeparatorType>[],
  _paths: string[],
  mainColor: string
): ReactNode => {
  const last = routes.indexOf(route) === routes.length - 1
  return (
    <StyledLink to={route.path || ''} isLast={last} mainColor={mainColor}>
      {route.breadcrumbName}
    </StyledLink>
  )
}

const getBreadCrumbName = (path: string, routes: IRouteObject[]): string => {
  for (const route of routes) {
    if (route.path) {
      const match = matchPath({ path: route.path, end: true }, path)

      if (match) {
        if (route.breadCrumbName) return route.breadCrumbName
        const dynamicSegment = Object.values(match.params)[0]
        return dynamicSegment ? `${dynamicSegment}` : path
      }
    }

    if (route.children) {
      const childBreadCrumb = getBreadCrumbName(path, route.children)
      if (childBreadCrumb) {
        return childBreadCrumb
      }
    }
  }

  return ''
}

const BreadCrumb = (): ReactElement => {
  const location = useLocation()
  const { mainColor } = useAppStore()
  const pathSegments = location.pathname === '/' ? [] : location.pathname.split('/').filter(Boolean)

  const breadCrumbsItems: Partial<BreadcrumbItemType & BreadcrumbSeparatorType>[] =
    pathSegments.length === 0
      ? [{ breadcrumbName: getBreadCrumbName('/', routes), path: '/' }]
      : pathSegments.map((_, index) => {
          const url = `/${pathSegments.slice(0, index + 1).join('/')}`
          return {
            breadcrumbName: decodeURIComponent(getBreadCrumbName(url, routes)),
            path: url
          }
        })

  if (breadCrumbsItems.length === 0) return <></>

  return (
    <StyledBreadCrumb
      separator='>'
      items={breadCrumbsItems}
      style={{ fontSize: '18px' }}
      itemRender={(route, params, routes, paths) => renderBreadCrumbItems(route, params, routes, paths, mainColor)}
    />
  )
}

export default BreadCrumb
