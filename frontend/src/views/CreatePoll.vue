<script setup>
import {ref} from 'vue'

const question = ref ('')
const options = ref ([{ text: ''}, { text: ''}])
const deadline = ref ('')
const isPrivate = ref (false)
const message = ref ('')

function addOption() {
    options.value.push({ text: ''})
}

function removeOption() {
    options.value.slice(index, 1)
}


async function createPoll() {
    const pollData = {
        question: question.value,
        publishedAt: new Date().toISOString(),
        validUntil: new Date(deadline.value).toISOString(),
        isPrivate: isPrivate.value,
        user: {
            id: 1,
            username: 'bob',
            email: 'bob@gmail.com',
            password: 'pass',
        },
        voteOption: options.value.map((opt, index) => ({
            caption: opt.text,
            presentationOrder: index + 1
        })),
    }

    try {
        const response = await fetch('/api/polls', {
            method: 'POST',
            headers: {
                'content-Type': 'application/json'
            },
            body: JSON.stringify(pollData),
        })

        if (response.ok) {
            message.value = `Poll "${question.value}" created successfully!`
            question.value = ''
            options.value = [{ text: ''}, { text: ''}]
            deadline.value = ''
            isPrivate.value = false
        } else {
            const errorText = await response.text()
            message.value = `Error: ${errorText || 'Failed to create poll.'}`
        }
    } catch (error) {
        message.value = 'Network error'
        console.error('Fetch error:', error)
    }
}
</script>

<template>
  <div>
    <h2>Create a New Poll</h2>
    <form @submit.prevent="createPoll">
      <div class="form-group">
        <label for="question">Question:</label>
        <input type="text" v-model="question" id="question" required />
      </div>

      <div class="form-group">
        <label>Voting Options:</label>
        <div v-for="(option, index) in options" :key="index" class="option-input">
          <input type="text" v-model="option.text" :placeholder="`Option ${index + 1}`" required />
          <button type="button" @click="removeOption(index)" v-if="options.length > 2">Remove</button>
        </div>
        <button type="button" @click="addOption">Add Option</button>
      </div>

      <div class="form-group">
        <label for="deadline">Deadline:</label>
        <input type="datetime-local" v-model="deadline" id="deadline" required />
      </div>

      <div class="form-group-inline">
        <label for="isPrivate">Make this poll private</label>
        <input type="checkbox" v-model="isPrivate" id="isPrivate" />
      </div>

      <button type="submit">Create Poll</button>
    </form>
    <p v-if="message">{{ message }}</p>
  </div>
</template>

<style scoped>
form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 500px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.form-group-inline {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.option-input {
  display: flex;
  gap: 0.5rem;
}
</style>