<template>
    <v-container>
        <v-row justify="center">
            <v-col md="4">
                <v-card>
                    <v-card-title class="text-h5 text-center">
                        로그인
                    </v-card-title>
                    <v-card-text>
                        <v-form>
                            <v-text-field
                                label="email"
                                v-model="email"
                            >

                            </v-text-field>
                            <v-text-field
                                label="password"
                                v-model="password"
                                type="password"
                            >
                                
                            </v-text-field>
                            <v-btn type="button" color="primary" block @click="memberLogin()">로그인</v-btn>
                        </v-form>
                        <br>
                        <v-row>
                            <v-col cols="6" class="d-flex justify-center">
                                <img
                                    src="@/assets/google_login.png"
                                    style="max-height: 40px; width: auto;"
                                    @click="googleLogin()"
                                />
                            </v-col>
                            <v-col cols="6" class="d-flex justify-center">
                                <img
                                    src="@/assets/kakao_login.png"
                                    style="max-height: 40px; width: auto;"
                                    @click="kakaoLogin()"
                                />
                            </v-col>
                        </v-row>
                    </v-card-text>
                </v-card>
            </v-col>
        </v-row>
    </v-container>
</template>

<script>
import axios from 'axios';

export default {
    data() {
        return{
            email : "",
            password : "",
            googleUrl: "https://accounts.google.com/o/oauth2/auth",
            googleClientId: "184039275121-3v895hcqj0imbjne3pfs9ceg6h8gld30.apps.googleusercontent.com",
            googleRedirectUrl: "http://localhost:3000/oauth/google/redirect",
            // openid는 요청하지 않아도 기본적으로 제공. email과 profile은 요청 시 제공.
            googleScope: "openid email profile",

            kakaoUrl: "https://kauth.kakao.com/oauth/authorize",
            kakaoClientId: "f04e0b2f9773e2e421e24a448dc478a0",
            kakaoRedirectUrl: "http://localhost:3000/oauth/kakao/redirect",
        }
    },
    methods: {
        async memberLogin() {
            const loginData = {
                email : this.email,
                password : this.password
            }
            const response = await axios.post("http://localhost:8080/member/doLogin", loginData);
            const token = response.data.token;
            localStorage.setItem("token", token);
            window.location.href = "/";
        },
        googleLogin() {
            const auth_uri = `${this.googleUrl}?client_id=${this.googleClientId}&redirect_uri=${this.googleRedirectUrl}&response_type=code&scope=${this.googleScope}`;
            console.log(auth_uri);
            window.location.href = auth_uri;
        },
        kakaoLogin() {
            const auth_uri = `${this.kakaoUrl}?client_id=${this.kakaoClientId}&redirect_uri=${this.kakaoRedirectUrl}&response_type=code`;
            window.location.href = auth_uri;
        }
    }
}
</script>