import GoogleRedirect from "@/components/GoogleRedirect.vue"
import KakaoRedirect from "@/components/KakaoRedirect.vue"
import MemberCreate from "@/components/MemberCreate.vue"
import MemberLogin from "@/components/MemberLogin.vue"
import { createRouter, createWebHistory } from "vue-router"


const routes = [
    {
        path: '/member/create',
        component: MemberCreate
    },
    {
        path: '/member/login',
        component: MemberLogin
    },
    {
        path: "/oauth/google/redirect",
        component: GoogleRedirect
    },
    {
        path: "/oauth/kakao/redirect",
        component: KakaoRedirect
    }
]

const router = createRouter(
    {
        history: createWebHistory(),
        routes
    }
)

export default router;