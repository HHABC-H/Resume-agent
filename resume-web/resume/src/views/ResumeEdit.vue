<template>
  <div class="resume-edit-page">
    <header class="header">
      <div class="logo" @click="router.push('/')">技术论坛</div>
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">论坛</router-link>
        <router-link to="/resume/upload" class="nav-item">简历助手</router-link>
        <router-link to="/my-resumes" class="nav-item active">我的简历</router-link>
        <router-link to="/reading" class="nav-item">在线阅读</router-link>
      </nav>
      <div class="header-right">
        <button @click="handleSaveDraft" class="btn-draft">保存草稿</button>
        <button @click="handleSaveVersion" class="btn-publish">保存版本</button>
        <button @click="router.go(-1)" class="btn-back">返回</button>
      </div>
    </header>

    <main class="main-content">
      <div class="edit-container">
        <div class="edit-panel">
          <h2 class="panel-title">简历编辑</h2>

          <div class="form-section">
            <h3 class="section-title">基本信息</h3>
            <div class="form-row">
              <input v-model="formData.name" placeholder="姓名" class="form-input" />
              <input v-model="formData.phone" placeholder="手机" class="form-input" />
            </div>
            <div class="form-row">
              <input v-model="formData.email" placeholder="邮箱" class="form-input" />
              <input v-model="formData.target" placeholder="求职意向" class="form-input" />
            </div>
          </div>

          <div class="form-section">
            <div class="section-header">
              <h3 class="section-title">教育经历</h3>
              <button @click="addEducation" class="btn-add">+ 添加</button>
            </div>
            <div v-for="(edu, index) in formData.educations" :key="index" class="item-card">
              <input v-model="edu.school" placeholder="学校" class="form-input" />
              <div class="form-row">
                <input v-model="edu.degree" placeholder="学历" class="form-input" />
                <input v-model="edu.major" placeholder="专业" class="form-input" />
              </div>
              <div class="form-row">
                <input v-model="edu.startDate" placeholder="开始时间" class="form-input" />
                <input v-model="edu.endDate" placeholder="结束时间" class="form-input" />
              </div>
              <button @click="removeEducation(index)" class="btn-remove">删除</button>
            </div>
          </div>

          <div class="form-section">
            <div class="section-header">
              <h3 class="section-title">工作经历</h3>
              <button @click="addExperience" class="btn-add">+ 添加</button>
            </div>
            <div v-for="(exp, index) in formData.experiences" :key="index" class="item-card">
              <input v-model="exp.company" placeholder="公司" class="form-input" />
              <input v-model="exp.position" placeholder="职位" class="form-input" />
              <textarea v-model="exp.content" placeholder="工作内容" class="form-textarea"></textarea>
              <div class="form-row">
                <input v-model="exp.startDate" placeholder="开始时间" class="form-input" />
                <input v-model="exp.endDate" placeholder="结束时间" class="form-input" />
              </div>
              <button @click="removeExperience(index)" class="btn-remove">删除</button>
            </div>
          </div>

          <div class="form-section">
            <div class="section-header">
              <h3 class="section-title">项目经历</h3>
              <button @click="addProject" class="btn-add">+ 添加</button>
            </div>
            <div v-for="(proj, index) in formData.projects" :key="index" class="item-card">
              <input v-model="proj.name" placeholder="项目名称" class="form-input" />
              <textarea v-model="proj.description" placeholder="项目描述" class="form-textarea"></textarea>
              <input v-model="proj.techStack" placeholder="技术栈" class="form-input" />
              <div class="form-row">
                <input v-model="proj.startDate" placeholder="开始时间" class="form-input" />
                <input v-model="proj.endDate" placeholder="结束时间" class="form-input" />
              </div>
              <button @click="removeProject(index)" class="btn-remove">删除</button>
            </div>
          </div>

          <div class="form-section">
            <h3 class="section-title">技能证书</h3>
            <textarea v-model="formData.skills" placeholder="技能列表（每行一条）" class="form-textarea"></textarea>
          </div>
        </div>

        <div class="preview-panel">
          <h2 class="panel-title">简历预览</h2>
          <div class="preview-content">
            <div class="preview-section" v-if="formData.name || formData.target">
              <h3>基本信息</h3>
              <p><strong>姓名：</strong>{{ formData.name }}</p>
              <p><strong>手机：</strong>{{ formData.phone }}</p>
              <p><strong>邮箱：</strong>{{ formData.email }}</p>
              <p><strong>求职意向：</strong>{{ formData.target }}</p>
            </div>

            <div class="preview-section" v-if="formData.educations.length > 0">
              <h3>教育经历</h3>
              <div v-for="(edu, index) in formData.educations" :key="index" class="preview-item">
                <p>{{ edu.school }} | {{ edu.degree }} | {{ edu.major }}</p>
                <p class="date">{{ edu.startDate }} ~ {{ edu.endDate }}</p>
              </div>
            </div>

            <div class="preview-section" v-if="formData.experiences.length > 0">
              <h3>工作经历</h3>
              <div v-for="(exp, index) in formData.experiences" :key="index" class="preview-item">
                <p><strong>{{ exp.company }}</strong> - {{ exp.position }}</p>
                <p class="date">{{ exp.startDate }} ~ {{ exp.endDate }}</p>
                <p>{{ exp.content }}</p>
              </div>
            </div>

            <div class="preview-section" v-if="formData.projects.length > 0">
              <h3>项目经历</h3>
              <div v-for="(proj, index) in formData.projects" :key="index" class="preview-item">
                <p><strong>{{ proj.name }}</strong></p>
                <p class="tech">{{ proj.techStack }}</p>
                <p class="date">{{ proj.startDate }} ~ {{ proj.endDate }}</p>
                <p>{{ proj.description }}</p>
              </div>
            </div>

            <div class="preview-section" v-if="formData.skills">
              <h3>技能证书</h3>
              <p>{{ formData.skills }}</p>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { resumeEditApi } from '../api/resumeEdit'

const router = useRouter()

const formData = reactive({
  name: '',
  phone: '',
  email: '',
  target: '',
  educations: [],
  experiences: [],
  projects: [],
  skills: ''
})

const addEducation = () => {
  formData.educations.push({ school: '', degree: '', major: '', startDate: '', endDate: '' })
}

const removeEducation = (index) => {
  formData.educations.splice(index, 1)
}

const addExperience = () => {
  formData.experiences.push({ company: '', position: '', content: '', startDate: '', endDate: '' })
}

const removeExperience = (index) => {
  formData.experiences.splice(index, 1)
}

const addProject = () => {
  formData.projects.push({ name: '', description: '', techStack: '', startDate: '', endDate: '' })
}

const removeProject = (index) => {
  formData.projects.splice(index, 1)
}

const getResumeText = () => {
  let text = ''
  if (formData.name) text += `姓名：${formData.name}\n`
  if (formData.phone) text += `手机：${formData.phone}\n`
  if (formData.email) text += `邮箱：${formData.email}\n`
  if (formData.target) text += `求职意向：${formData.target}\n\n`

  if (formData.educations.length > 0) {
    text += '教育经历\n'
    formData.educations.forEach(e => {
      text += `${e.school} | ${e.degree} | ${e.major} | ${e.startDate}~${e.endDate}\n`
    })
    text += '\n'
  }

  if (formData.experiences.length > 0) {
    text += '工作经历\n'
    formData.experiences.forEach(e => {
      text += `${e.company} - ${e.position} | ${e.startDate}~${e.endDate}\n${e.content}\n`
    })
    text += '\n'
  }

  if (formData.projects.length > 0) {
    text += '项目经历\n'
    formData.projects.forEach(p => {
      text += `${p.name} | ${p.techStack} | ${p.startDate}~${p.endDate}\n${p.description}\n`
    })
    text += '\n'
  }

  if (formData.skills) {
    text += `技能证书\n${formData.skills}\n`
  }

  return text
}

const loadDraft = async () => {
  try {
    const response = await resumeEditApi.getCurrentDraft()
    if (response.data.code === 200 && response.data.data) {
      const data = response.data.data.structuredData
      if (data) {
        Object.assign(formData, data)
      }
    }
  } catch (e) {
    console.log('暂无草稿')
  }
}

const handleSaveDraft = async () => {
  try {
    const requestData = {
      structuredData: { ...formData },
      resumeText: getResumeText()
    }
    await resumeEditApi.saveDraft(requestData)
    alert('草稿保存成功')
  } catch (e) {
    alert('保存失败')
  }
}

const handleSaveVersion = async () => {
  try {
    const requestData = {
      structuredData: { ...formData },
      resumeText: getResumeText(),
      publish: true
    }
    await resumeEditApi.saveAsVersion(requestData)
    alert('简历保存成功')
    router.push('/my-resumes')
  } catch (e) {
    alert('保存失败')
  }
}

onMounted(() => {
  loadDraft()
})
</script>

<style scoped>
.resume-edit-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.8rem 2rem;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  font-size: 1.4rem;
  font-weight: bold;
  color: #007bff;
  cursor: pointer;
  margin-right: 2rem;
}

.nav-menu {
  display: flex;
  gap: 0.3rem;
  flex: 1;
}

.nav-item {
  color: #666;
  text-decoration: none;
  padding: 0.5rem 0.9rem;
  border-radius: 4px;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.2s;
}

.nav-item:hover {
  background: #f0f7ff;
  color: #007bff;
}

.nav-item.active {
  background: #007bff;
  color: #fff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.btn-draft, .btn-publish, .btn-back {
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  border: none;
}

.btn-draft {
  background: #fff;
  border: 1px solid #ddd;
  color: #666;
}

.btn-publish {
  background: #007bff;
  color: #fff;
}

.btn-back {
  background: #fff;
  border: 1px solid #ddd;
  color: #666;
}

.main-content {
  flex: 1;
  padding: 1.5rem 2rem;
}

.edit-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  max-width: 1400px;
  margin: 0 auto;
}

.edit-panel, .preview-panel {
  background: #fff;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}

.panel-title {
  margin: 0 0 1rem 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.form-section {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  margin: 0 0 1rem 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.form-input {
  width: 100%;
  padding: 0.6rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  margin-bottom: 0.5rem;
}

.form-textarea {
  width: 100%;
  padding: 0.6rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-height: 80px;
  resize: vertical;
  font-family: inherit;
}

.item-card {
  background: #f9f9f9;
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 0.5rem;
}

.btn-add {
  padding: 0.3rem 0.8rem;
  background: #007bff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}

.btn-remove {
  padding: 0.3rem 0.8rem;
  background: #dc3545;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  margin-top: 0.5rem;
}

.preview-content {
  font-size: 14px;
  line-height: 1.6;
}

.preview-section {
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.preview-section h3 {
  margin: 0 0 0.5rem 0;
  font-size: 15px;
  color: #007bff;
}

.preview-item {
  margin-bottom: 1rem;
  padding-left: 0.5rem;
  border-left: 2px solid #e0e0e0;
}

.preview-item p {
  margin: 0.3rem 0;
}

.date {
  color: #666;
  font-size: 13px;
}

.tech {
  color: #007bff;
  font-size: 13px;
}

@media (max-width: 900px) {
  .edit-container {
    grid-template-columns: 1fr;
  }
}
</style>