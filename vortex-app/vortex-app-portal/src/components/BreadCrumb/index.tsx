import type { ReactElement, ReactNode } from 'react'
import { Link, matchPath, useLocation } from 'react-router-dom'
import { styled } from 'styled-components'
import { Breadcrumb as AntBreadcrumb } from 'antd'
import type { BreadcrumbItemType, BreadcrumbSeparatorType } from 'antd/es/breadcrumb/Breadcrumb'
import { RightOutlined } from '@ant-design/icons'
import { IRouteObject, routes } from '@/routers'
import { useAppStore } from '@/stores/app.store'

interface StyledLinkProps {
  isLast: boolean
  mainColor: string
}

const StyledLink = styled(Link).withConfig({
  shouldForwardProp: (prop) => !['isLast', 'mainColor'].includes(prop)
})<StyledLinkProps>`
  color: ${({ isLast, mainColor }) => (isLast ? '#000' : mainColor)} !important;
  font-weight: 600;
  font-size: 1rem;
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
    <StyledLink to={route.path ?? ''} isLast={last} mainColor={mainColor}>
      {route.breadcrumbName}
    </StyledLink>
  )
}

const findMatchingRoute = (path: string, routes: IRouteObject[]): IRouteObject | null => {
  for (const route of routes) {
    if (route.path && matchPath({ path: route.path, end: true }, path)) {
      return route
    }

    if (route.children) {
      const matchingChild = findMatchingRoute(path, route.children)
      if (matchingChild) {
        return matchingChild
      }
    }
  }
  return null
}

const getBreadCrumbName = (path: string, routes: IRouteObject[]): string => {
  const route = findMatchingRoute(path, routes)
  if (!route) return ''
  if (route.breadCrumbName) return route.breadCrumbName
  const match = matchPath({ path: route.path!, end: true }, path)
  const dynamicSegment = match && Object.values(match.params)[0]
  return dynamicSegment ? `${dynamicSegment}` : path
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
      separator={<RightOutlined />}
      items={breadCrumbsItems}
      style={{ fontSize: '18px' }}
      itemRender={(route, params, routes, paths) => renderBreadCrumbItems(route, params, routes, paths, mainColor)}
    />
  )
}

export default BreadCrumb
