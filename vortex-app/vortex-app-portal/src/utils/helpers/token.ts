/**
 * store the token, using localstorage
 * @param token
 */
export const storeToken = (token: string): string => {
  window.localStorage.setItem('token', token)
  return token
}

/**
 * get the token, from localstorage
 */
export const getToken = () => {
  if (typeof window !== 'undefined') {
    return window.localStorage.getItem('token')
  }
}

/**
 * clear the token
 */
export const clearToken = () => {
  window.localStorage.removeItem('token')
}

export const storeOrg = (token: string): string => {
  window.localStorage.setItem('org', token)
  return token
}
/**
 * get the token, from localstorage
 */
export const getOrg = () => {
  if (typeof window !== 'undefined') {
    return window.localStorage.getItem('org')
  }
}

/**
 * clear the token
 */
export const clearOrg = () => {
  window.localStorage.removeItem('org')
}
