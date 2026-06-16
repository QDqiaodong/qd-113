import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'TeaList',
    component: () => import('../views/TeaList.vue')
  },
  {
    path: '/tea/create',
    name: 'TeaCreate',
    component: () => import('../views/TeaForm.vue')
  },
  {
    path: '/tea/:id',
    name: 'TeaDetail',
    component: () => import('../views/TeaDetail.vue')
  },
  {
    path: '/tea/:id/edit',
    name: 'TeaEdit',
    component: () => import('../views/TeaForm.vue')
  },
  {
    path: '/compare',
    name: 'Compare',
    component: () => import('../views/CompareView.vue')
  },
  {
    path: '/aging-timeline',
    name: 'AgingTimeline',
    component: () => import('../views/AgingTimelineView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
