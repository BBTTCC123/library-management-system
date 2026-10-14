import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi } from '@/api/auth'
import { getMe } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    role: (state) => state.user?.role,
    displayName: (state) => state.user?.fullName || state.user?.username || '',
    isAdmin: (state) => state.user?.role === 'ADMIN',
    canManageLibrary: (state) =>
      state.user?.role === 'ADMIN' || state.user?.role === 'LIBRARIAN'
  },

  actions: {
    setAuth(payload) {
      this.token = payload.accessToken
      this.user = payload.user
      localStorage.setItem('token', payload.accessToken)
      localStorage.setItem('user', JSON.stringify(payload.user))
    },

    async login(credentials) {
      const res = await loginApi(credentials)
      this.setAuth(res.data)
      return res.data
    },

    async register(payload) {
      const res = await registerApi(payload)
      this.setAuth(res.data)
      return res.data
    },

    async fetchMe() {
      const res = await getMe()
      this.user = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
      return res.data
    },

    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
