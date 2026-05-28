import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import ArticleDetail from '../views/ArticleDetail.vue'
import About from '../views/About.vue'
import Links from '../views/Links.vue'
import MessageBoard from '../views/MessageBoard.vue'
import Login from '../views/Login.vue'
import Admin from '../views/Admin.vue'
import NotFound from '../views/NotFound.vue'

const routes = [
  { path: '/', component: Home },
  { path: '/article/:slug', component: ArticleDetail },
  { path: '/about', component: About },
  { path: '/links', component: Links },
  { path: '/messages', component: MessageBoard },
  { path: '/login', component: Login },
  { path: '/admin', component: Admin, meta: { auth: true } },
  { path: '/:pathMatch(.*)*', component: NotFound }
]

const router = createRouter({ history: createWebHistory(), routes, scrollBehavior: () => ({ top: 0 }) })
router.beforeEach((to, from, next) => {
  if (to.meta.auth && !localStorage.getItem('token')) next('/login')
  else next()
})
export default router
