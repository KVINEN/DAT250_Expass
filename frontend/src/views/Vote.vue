<script setup>

import {ref, onMounted} from 'vue'

const polls = ref([])
const isLoading = ref(true)
const message = ref('')

async function fetchPolls() {
    try {
        isLoading.value = true
        const response = await fetch('/api/polls')

        if (response.ok) {
            polls.value = await response.json()
        } else {
            throw new Error(`HTTP error! Status ${response.status}`)
        } 
    } catch (error) {
    message.value = error.message
    console.error('Error fetching polls:', error)
    } finally {
        isLoading.value = false
    }
}

async function castVote(poll, option) {
    const voter = {id: 2, username: 'voter', email: 'voter@gmail.com' }

    const voteData = {
        user: voter,
        publishedAt: new Date().toISOString(),
        votesOn: option,
    }

    try {
        const response = await fetch(`/api/polls/${poll.id}/votes`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(voteData),
        })

        if (response.ok) {
            alert(`You voted for "${option.caption}"!`)
        } else {
            const errorText = await response.text()
            message.value = errorText || 'Failed to cast vote.'
            throw new Error(message.value)
        }
    } catch (error) {
        alert(`Error: ${error.message}`)
        console.error('Error cating vote', error)
    }
}

onMounted (() => {
    fetchPolls()
})

</script>

<template>
  <div>
    <h2>Vote on a Poll</h2>

    <p v-if="isLoading">Loading polls...</p>

    <p v-else-if="message" class="error-message">
      Could not load polls. Is the backend server running? <br />
      Error: {{ message }}
    </p>

    <div v-else class="poll-list">
      <div v-for="poll in polls" :key="poll.id" class="poll-card">
        <h3>{{ poll.question }}</h3>
        <ul class="options-list">
          <li v-for="option in poll.options" :key="option.id" class="option-item">
            <span>{{ option.caption }}</span>
            <button @click="castVote(poll, option)">Vote</button>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>
.poll-list {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}
.poll-card {
  border: 1px solid #ccc;
  padding: 1rem;
  border-radius: 8px;
}
.options-list {
  list-style: none;
  padding: 0;
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.option-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem;
  background-color: #f9f9f9;
  border-radius: 4px;
}
.error-message {
  color: red;
}
</style>