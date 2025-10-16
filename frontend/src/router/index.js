import { createRouter, createWebHistory } from 'vue-router'
import CreateUser from '../views/CreateUser.vue'
import CreatePoll from '../views/CreatePoll.vue'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/create-user',
      name: 'createUser',
      component: CreateUser,
    },
    {
      path: '/create-poll',
      name: 'createPoll',
      component: CreatePoll,
    }
  ],
})

export default router
