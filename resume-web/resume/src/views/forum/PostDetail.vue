<template>
  <div class="post-container">
    <div class="page-header">
      <button class="back-btn" @click="router.go(-1)">← 返回</button>
      <h1 class="page-title">帖子详情</h1>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="post" class="post">
      <h1>
        <span v-if="post.status === 1" class="badge essence">精华</span>
        <span v-if="post.status === 2" class="badge top">置顶</span>
        {{ post.title }}
      </h1>
      <div class="meta">
        <span>{{ post.authorName }}</span>
        <span>{{ post.categoryName }}</span>
        <span>{{ formatDate(post.createdAt) }}</span>
        <span>阅读 {{ post.viewCount }}</span>
        <span>评论 {{ post.commentCount }}</span>
      </div>
      <div class="content">{{ post.content }}</div>
      <div class="actions">
        <button @click="likePost" :disabled="liked || post.liked" :class="{ active: liked || post.liked }">👍 赞 ({{ post.likeCount }})</button>
        <button @click="dislikePost" :disabled="disliked || post.disliked" :class="{ active: disliked || post.disliked }">👎 踩 ({{ post.dislikeCount }})</button>
      </div>
    </div>

    <div class="comments">
      <h3>评论 ({{ comments.length }})</h3>
      <div class="comment-form">
        <textarea v-model="commentContent" placeholder="写下你的评论..."></textarea>
        <button @click="submitComment">发表评论</button>
      </div>

      <div class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-meta">
            <span>{{ comment.authorName }}</span>
            <span>{{ formatDate(comment.createdAt) }}</span>
          </div>
          <div class="comment-content">{{ comment.content }}</div>
          <div class="comment-actions">
            <span @click="likeComment(comment)" :class="{ active: comment.liked }" :style="{ opacity: comment.liked || comment.disliked ? 0.5 : 1 }">👍 {{ comment.likeCount }}</span>
            <span @click="dislikeComment(comment)" :class="{ active: comment.disliked }" :style="{ opacity: comment.liked || comment.disliked ? 0.5 : 1 }">👎 {{ comment.dislikeCount }}</span>
            <span @click="showReplyForm(comment.id)">回复</span>
          </div>

          <div v-if="replyTo === comment.id" class="reply-form">
            <textarea v-model="replyContent" placeholder="写下你的回复..."></textarea>
            <button @click="submitReply(comment.id)">提交回复</button>
            <button @click="cancelReply">取消</button>
          </div>

          <div v-if="comment.children && comment.children.length" class="replies">
            <div v-for="reply in comment.children" :key="reply.id" class="reply-item">
              <div class="comment-meta">
                <span>{{ reply.authorName }}</span>
                <span>{{ formatDate(reply.createdAt) }}</span>
              </div>
              <div class="comment-content">{{ reply.content }}</div>
              <div class="comment-actions">
                <span @click="likeComment(reply)" :class="{ active: reply.liked }" :style="{ opacity: reply.liked || reply.disliked ? 0.5 : 1 }">👍 {{ reply.likeCount }}</span>
                <span @click="dislikeComment(reply)" :class="{ active: reply.disliked }" :style="{ opacity: reply.liked || reply.disliked ? 0.5 : 1 }">👎 {{ reply.dislikeCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const post = ref(null)
const comments = ref([])
const loading = ref(true)
const error = ref('')
const commentContent = ref('')
const replyContent = ref('')
const replyTo = ref(null)
const liked = ref(false)
const disliked = ref(false)

const postId = route.params.id

const loadPost = async () => {
  try {
    loading.value = true
    const response = await axios.get(`/forum/post/${postId}/detail`)
    post.value = response.data.data
    comments.value = response.data.data.comments || []
  } catch (e) {
    error.value = '加载失败: ' + (e.message || '未知错误')
  } finally {
    loading.value = false
  }
}

const likePost = async () => {
  if (liked.value || post.value.liked) return
  if (disliked.value || post.value.disliked) {
    post.value.dislikeCount--
    disliked.value = false
    post.value.disliked = false
  }
  try {
    await axios.post(`/forum/post/${postId}/like`)
    liked.value = true
    post.value.liked = true
    post.value.likeCount++
  } catch (e) {
    alert('操作失败')
  }
}

const dislikePost = async () => {
  if (disliked.value || post.value.disliked) return
  if (liked.value || post.value.liked) {
    post.value.likeCount--
    liked.value = false
    post.value.liked = false
  }
  try {
    await axios.post(`/forum/post/${postId}/dislike`)
    disliked.value = true
    post.value.disliked = true
    post.value.dislikeCount++
  } catch (e) {
    alert('操作失败')
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    alert('评论内容不能为空')
    return
  }
  try {
    await axios.post('/forum/comment', {
      postId: parseInt(postId),
      content: commentContent.value
    })
    commentContent.value = ''
    loadPost()
  } catch (e) {
    alert('发表评论失败: ' + (e.response?.data?.error || e.message))
  }
}

const showReplyForm = (commentId) => {
  replyTo.value = replyTo.value === commentId ? null : commentId
  replyContent.value = ''
}

const cancelReply = () => {
  replyTo.value = null
  replyContent.value = ''
}

const submitReply = async (parentId) => {
  if (!replyContent.value.trim()) {
    alert('回复内容不能为空')
    return
  }
  try {
    await axios.post('/forum/comment', {
      postId: parseInt(postId),
      parentId: parentId,
      content: replyContent.value
    })
    replyContent.value = ''
    replyTo.value = null
    loadPost()
  } catch (e) {
    alert('回复失败: ' + (e.response?.data?.error || e.message))
  }
}

const likeComment = async (comment) => {
  if (comment.liked || comment.disliked) return
  try {
    await axios.post(`/forum/comment/${comment.id}/like`)
    comment.liked = true
    comment.likeCount++
  } catch (e) {
    alert('操作失败')
  }
}

const dislikeComment = async (comment) => {
  if (comment.liked || comment.disliked) return
  try {
    await axios.post(`/forum/comment/${comment.id}/dislike`)
    comment.disliked = true
    comment.dislikeCount++
  } catch (e) {
    alert('操作失败')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadPost()
})
</script>

<style scoped>
.post-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  padding: 1rem;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 1rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.back-btn {
  padding: 0.5rem 1rem;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
  margin-right: 1rem;
}

.back-btn:hover {
  border-color: #007bff;
  color: #007bff;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.post {
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.post h1 {
  font-size: 24px;
  margin-bottom: 15px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 10px;
}

.badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 14px;
  font-weight: normal;
}

.badge.essence {
  background: #ffc107;
  color: #000;
}

.badge.top {
  background: #dc3545;
  color: #fff;
}

.meta {
  color: #999;
  font-size: 14px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.meta span {
  margin-right: 20px;
}

.content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
}

.actions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 15px;
}

.actions button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}

.actions button:hover {
  background: #f5f5f5;
}

.actions button.active {
  color: #007bff;
  border-color: #007bff;
}

.comments {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.comments h3 {
  margin-bottom: 20px;
  color: #333;
}

.comment-form {
  margin-bottom: 30px;
}

.comment-form textarea {
  width: 100%;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
  min-height: 100px;
  font-size: 14px;
  font-family: inherit;
}

.comment-form button {
  margin-top: 10px;
  padding: 10px 30px;
  background: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.comment-item {
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-meta {
  color: #999;
  font-size: 13px;
  margin-bottom: 10px;
}

.comment-meta span {
  margin-right: 15px;
}

.comment-content {
  color: #333;
  line-height: 1.6;
  margin-bottom: 10px;
}

.comment-actions {
  display: flex;
  gap: 15px;
}

.comment-actions span {
  color: #999;
  font-size: 13px;
  cursor: pointer;
}

.comment-actions span:hover {
  color: #007bff;
}

.reply-form {
  margin-top: 15px;
  margin-left: 20px;
}

.reply-form textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
  min-height: 60px;
  font-size: 14px;
  font-family: inherit;
}

.reply-form button {
  margin-top: 10px;
  margin-right: 10px;
  padding: 8px 16px;
  background: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.reply-form button:last-child {
  background: #999;
}

.replies {
  margin-left: 40px;
  margin-top: 15px;
  padding-left: 15px;
  border-left: 2px solid #eee;
}

.reply-item {
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.reply-item:last-child {
  border-bottom: none;
}

.loading, .error {
  text-align: center;
  padding: 50px;
  color: #999;
}

.error {
  color: #dc3545;
}
</style>
