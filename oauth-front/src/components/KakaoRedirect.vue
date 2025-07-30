<template>
    <div>
        카카오 로그인 진행 중...
    </div>
</template>
<script>
import axios from 'axios';

export default {
    created() {
        const code = new URL(window.location.href).searchParams.get("code");
        console.log(code);
        this.sendCodeToServer(code);
    },
    methods: {
        async sendCodeToServer(code) {
            const response = await axios.post("http://localhost:8080/user/kakao/login", {code});
            const accessToken = response.data.result.accessToken;
            const refreshToken = response.data.result.refreshToken;
            localStorage.setItem("accessToken", accessToken);
            localStorage.setItem("refreshToken", refreshToken);
            window.location.href = "/";
        }
    }
}
</script>