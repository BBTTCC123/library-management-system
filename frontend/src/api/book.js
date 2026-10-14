import request from './request'

export const getBooks = (params) => request.get('/books', { params })

export const getBook = (id) => request.get(`/books/${id}`)

export const createBook = (data) => request.post('/books', data)

export const updateBook = (id, data) => request.put(`/books/${id}`, data)

export const deleteBook = (id) => request.delete(`/books/${id}`)
