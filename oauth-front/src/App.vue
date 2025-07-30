<template>
  <v-app>
    <HeaderComponent />
    <v-main>
      <router-view />
    </v-main>
  </v-app>
</template>

<script>
import HeaderComponent from './components/HeaderComponent.vue';


export default {
  components: {
    HeaderComponent
  }
}
</script>

