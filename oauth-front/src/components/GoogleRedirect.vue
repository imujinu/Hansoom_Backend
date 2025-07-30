<template>
    <div>
        구글 로그인 진행 중...
    </div>
</template>
<script>
import axios from 'axios';

export default {
    created() {
        const code = new URL(window.location.href).searchParams.get("code");
        this.sendCodeToServer(code);
    },
    methods: {
        async sendCodeToServer(code) {
            const response = await axios.post("http://localhost:8080/member/google/doLogin", {code});
            const token = response.data.token;
            localStorage.setItem("token", token);
            window.location.href = "/";
        }
    }
}
</script>