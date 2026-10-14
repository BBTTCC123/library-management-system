import request from './request'

export const getMe = () => request.get('/users/me')

export const getUsers = (params) => request.get('/users', { params })

export const getUser = (id) => request.get(`/users/${id}`)

export const updateUserRole = (id, role) =>
  request.patch(`/users/${id}/role`, { role })

export const updateUserEnabled = (id, enabled) =>
  request.patch(`/users/${id}/enabled`, null, { params: { enabled } })
