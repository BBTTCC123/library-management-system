import request from './request'

export const borrowBook = (bookId) => request.post('/borrows', { bookId })

export const returnBook = (id) => request.post(`/borrows/${id}/return`)

export const renewBorrow = (id) => request.post(`/borrows/${id}/renew`)

export const getMyBorrows = (params) => request.get('/borrows/me', { params })

export const getBorrows = (params) => request.get('/borrows', { params })

export const getBorrow = (id) => request.get(`/borrows/${id}`)

export const getStats = () => request.get('/stats')
