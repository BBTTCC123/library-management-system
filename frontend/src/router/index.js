import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页概览', icon: 'Odometer' }
      },
      {
        path: 'books',
        name: 'books',
        component: () => import('@/views/Books.vue'),
        meta: { title: '图书管理', icon: 'Reading' }
      },
      {
        path: 'categories',
        name: 'categories',
        component: () => import('@/views/Categories.vue'),
        meta: { title: '分类管理', icon: 'Collection', roles: ['ADMIN', 'LIBRARIAN'] }
      },
      {
        path: 'borrows',
        name: 'my-borrows',
        component: () => import('@/views/MyBorrows.vue'),
        meta: { title: '我的借阅', icon: 'Tickets' }
      },
      {
        path: 'borrow-records',
        name: 'borrow-records',
        component: () => import('@/views/BorrowRecords.vue'),
        meta: { title: '借阅记录', icon: 'List', roles: ['ADMIN', 'LIBRARIAN'] }
      },
      {
        path: 'users',
        name: 'users',
        component: () => import('@/views/Users.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['ADMIN'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFound.vue'),
    meta: { public: true, title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const store = useUserStore()
  document.title = to.meta.title ? `${to.meta.title} · 图书借阅管理系统` : '图书借阅管理系统'

  if (to.meta.public) {
    if (store.isLoggedIn && to.name === 'login') {
      return { name: 'dashboard' }
    }
    return true
  }

  if (!store.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.meta.roles && !to.meta.roles.includes(store.role)) {
    return { name: 'dashboard' }
  }

  return true
})

export default router
