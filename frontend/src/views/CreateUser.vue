<script setup>
import { ref } from 'vue'

const username = ref('')
const email = ref('')
const password = ref('')
const message = ref('')

async function createUser() {
    try {
        const response = await fetch('http://localhost:8080/api/users', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: username.value,
                email: email.value,
                password: password.value,
            }),
        })
        if(response.ok) {
            message.value = `User "${username.value}" created successfully!`
            username.value = ''
            email.value = ''
            password.value = ''
        } else {
            const errorData = await response.json()
            message.value = `Error "${errorData.message || 'Failed to create user.'}"`
        }
    } catch (error) {
        message.value = 'A error occured. Please try again.'
        console.error('fetch error', error)
    }
}
</script>

<template>
    <div>
        <h2>Create a new User</h2>
        <form @submit.prevent="createUser">
            <div>
                <label for="username">Username:</label>
                <input id="username" v-model="username" type="text" required/>
            </div>
            <div>
                <label for="email">Email:</label>
                <input id="email" v-model="email" type="text" required />
            </div>
            <div>
                <label for="password">Password:</label>
                <input id="password" v-model="password" type="password" required />
            </div>
            <button type="submit">Create User</button>
        </form>
        <p v-if="message">{{ message }}</p>
    </div>
</template>

<style scoped>
form {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    max-width: 300px;
}

div {
    display: flex;
    flex-direction: column;
}
</style>