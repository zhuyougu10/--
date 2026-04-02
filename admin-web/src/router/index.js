import { createRouter, createWebHistory } from 'vue-router'
import { isMobileDevice } from '@/utils/device'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/components/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'dashboard' }
      },
      {
        path: 'venue',
        name: 'VenueList',
        component: () => import('@/views/venue/list.vue'),
        meta: { title: '球馆管理', icon: 'home' }
      },
      {
        path: 'venue/create',
        name: 'VenueCreate',
        component: () => import('@/views/venue/form.vue'),
        meta: { title: '新建球馆', hidden: true }
      },
      {
        path: 'venue/:id/edit',
        name: 'VenueEdit',
        component: () => import('@/views/venue/form.vue'),
        meta: { title: '编辑球馆', hidden: true }
      },
      {
        path: 'court',
        name: 'CourtList',
        component: () => import('@/views/court/list.vue'),
        meta: { title: '场地管理', icon: 'appstore' }
      },
      {
        path: 'court/create',
        name: 'CourtCreate',
        component: () => import('@/views/court/form.vue'),
        meta: { title: '新建场地', hidden: true }
      },
      {
        path: 'court/:id/edit',
        name: 'CourtEdit',
        component: () => import('@/views/court/form.vue'),
        meta: { title: '编辑场地', hidden: true }
      },
      {
        path: 'booking/today',
        name: 'TodaySchedule',
        component: () => import('@/views/booking/today.vue'),
        meta: { title: '今日排场', icon: 'calendar' }
      },
      {
        path: 'booking',
        name: 'BookingList',
        component: () => import('@/views/booking/list.vue'),
        meta: { title: '预约记录', icon: 'file-text' }
      },
      {
        path: 'checkin',
        name: 'Checkin',
        component: () => import('@/views/checkin/index.vue'),
        meta: { title: '扫码核销', icon: 'scan' }
      },
      {
        path: 'user',
        name: 'UserList',
        component: () => import('@/views/user/list.vue'),
        meta: { title: '用户管理', icon: 'user' }
      },
      {
        path: 'm/checkin',
        name: 'MobileCheckin',
        redirect: '/checkin',
        meta: { hidden: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  const isMobileLogin = localStorage.getItem('admin_is_mobile') === '1'
  const shouldGoCheckin = isMobileLogin || isMobileDevice()
  
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next(shouldGoCheckin ? '/checkin' : '/dashboard')
  } else if (shouldGoCheckin && to.path !== '/checkin' && to.path !== '/m/checkin' && to.path !== '/login') {
    next('/checkin')
  } else {
    next()
  }
})

export default router
