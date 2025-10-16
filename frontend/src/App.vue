<script>
import { RouterLink, RouterView } from 'vue-router';
</script>

<template>
  <header>
    <nav>
      <RouterLink to="/create-user">Create User</RouterLink>
      <RouterLink to="/create-poll">Create Poll</RouterLink>
      <RouterLink to="/vote">Vote on Poll</RouterLink>
    </nav>
  </header>

      <main>
        <RouterView />
      </main>
</template>

<style scoped>
  header {
    padding: 1rem;
    border-bottom: 1px solid #ccc;
  }
  
  nav {
    display: flex;
    gap: 1rem;
    font-size: 1.2rem;
  }

  main {
    padding: 1rem;
  }
</style>