<template>
  <div class="resume-edit-page">
    <header class="header">
      <div class="logo" @click="router.push('/')">技术论坛</div>
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">论坛</router-link>
        <router-link to="/resume/upload" class="nav-item">简历助手</router-link>
        <router-link to="/resume/edit" class="nav-item active">编辑简历</router-link>
        <router-link to="/my-resumes" class="nav-item">我的简历</router-link>
        <router-link to="/reading" class="nav-item">在线阅读</router-link>
        <router-link :to="interviewLink" class="nav-item">面试助手</router-link>
        <router-link to="/profile" class="nav-item">个人信息</router-link>
        <router-link to="/history" class="nav-item">查看历史</router-link>
        <router-link to="/my-bookmarks" class="nav-item">我的收藏</router-link>
        <router-link to="/forum/essences" class="nav-item">精华帖</router-link>
        <router-link to="/forum/authors" class="nav-item">热门作者</router-link>
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
              <textarea v-model="exp.content" placeholder="工作内容（每行一条）" class="form-textarea" @input="autoResize($event)"></textarea>
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
              <textarea v-model="proj.description" placeholder="项目描述（每行一条）" class="form-textarea" @input="autoResize($event)"></textarea>
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
            <textarea v-model="formData.skills" placeholder="技能列表（每行一条）" class="form-textarea" @input="autoResize($event)"></textarea>
          </div>
        </div>

        <div class="preview-panel">
          <div class="preview-header">
            <h2 class="panel-title">简历预览</h2>
            <div class="template-selector">
              <span class="selector-label">模板：</span>
              <select v-model="selectedTemplate" class="template-select">
                <option value="classic">经典蓝色</option>
                <option value="modern">现代简约</option>
                <option value="tech">技术风格</option>
                <option value="elegant">优雅商务</option>
                <option value="fresh">清新风格</option>
              </select>
            </div>
            <div class="export-buttons">
              <button @click="exportToPDF" class="btn-export btn-pdf">导出PDF</button>
              <button @click="exportToWord" class="btn-export btn-word">导出Word</button>
            </div>
          </div>
          <div class="preview-content" :class="'template-' + selectedTemplate" ref="resumePreview">
            <div class="resume-paper">
              <div class="resume-header" v-if="formData.name || formData.target">
                <h1 class="resume-name">{{ formData.name || '姓名' }}</h1>
                <div class="resume-contact">
                  <span v-if="formData.phone">{{ formData.phone }}</span>
                  <span v-if="formData.email">{{ formData.email }}</span>
                  <span v-if="formData.target">{{ formData.target }}</span>
                </div>
              </div>

              <div class="resume-section" v-if="formData.educations.length > 0">
                <h2 class="section-title">教育经历</h2>
                <div v-for="(edu, index) in formData.educations" :key="index" class="section-item">
                  <div class="item-header">
                    <span class="item-title">{{ edu.school }}</span>
                    <span class="item-date">{{ edu.startDate }} - {{ edu.endDate }}</span>
                  </div>
                  <div class="item-content">{{ edu.degree }} | {{ edu.major }}</div>
                </div>
              </div>

              <div class="resume-section" v-if="formData.experiences.length > 0">
                <h2 class="section-title">工作经历</h2>
                <div v-for="(exp, index) in formData.experiences" :key="index" class="section-item">
                  <div class="item-header">
                    <span class="item-title">{{ exp.company }}</span>
                    <span class="item-sub">{{ exp.position }}</span>
                    <span class="item-date">{{ exp.startDate }} - {{ exp.endDate }}</span>
                  </div>
                  <ul class="item-list" v-if="exp.content">
                    <li v-for="(line, i) in exp.content.split('\n').filter(l => l.trim())" :key="i">{{ line }}</li>
                  </ul>
                </div>
              </div>

              <div class="resume-section" v-if="formData.projects.length > 0">
                <h2 class="section-title">项目经历</h2>
                <div v-for="(proj, index) in formData.projects" :key="index" class="section-item">
                  <div class="item-header">
                    <span class="item-title">{{ proj.name }}</span>
                    <span class="item-date">{{ proj.startDate }} - {{ proj.endDate }}</span>
                  </div>
                  <div class="item-tech" v-if="proj.techStack">{{ proj.techStack }}</div>
                  <ul class="item-list" v-if="proj.description">
                    <li v-for="(line, i) in proj.description.split('\n').filter(l => l.trim())" :key="i">{{ line }}</li>
                  </ul>
                </div>
              </div>

              <div class="resume-section" v-if="formData.skills">
                <h2 class="section-title">技能证书</h2>
                <div class="skills-grid">
                  <span v-for="(skill, i) in formData.skills.split('\n').filter(s => s.trim())" :key="i" class="skill-tag">{{ skill }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { resumeEditApi } from '../api/resumeEdit'
import { jsPDF } from 'jspdf'
import { Document, Packer, Paragraph, TextRun, HeadingLevel } from 'docx'
import 'jspdf/dist/jspdf.umd.min.js'

const router = useRouter()
const route = useRoute()
const resumePreview = ref(null)

const token = localStorage.getItem('token')
const username = localStorage.getItem('username')

const interviewLink = computed(() => {
  if (!token) return '/login'
  return '/interview/1'
})

const selectedTemplate = ref('classic')

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

const autoResize = (event) => {
  const textarea = event.target
  textarea.style.height = 'auto'
  textarea.style.height = textarea.scrollHeight + 'px'
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

const loadHistoryDetail = async (id) => {
  try {
    const response = await resumeEditApi.getHistoryDetail(id)
    if (response.data.code === 200 && response.data.data) {
      const data = response.data.data.structuredData
      if (data) {
        Object.assign(formData, data)
      }
    }
  } catch (e) {
    console.error('加载简历详情失败', e)
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

const exportToPDF = async () => {
  if (!resumePreview.value) return

  try {
    const pdf = new jsPDF('p', 'mm', 'a4')
    const paper = resumePreview.value.querySelector('.resume-paper')
    if (!paper) return

    pdf.setFont('helvetica')
    pdf.setFontSize(10)

    let y = 20
    const margin = 20
    const pageWidth = 210
    const contentWidth = pageWidth - margin * 2

    if (formData.name) {
      pdf.setFontSize(24)
      pdf.setTextColor(0, 123, 255)
      pdf.text(formData.name, pageWidth / 2, y, { align: 'center' })
      y += 10
    }

    const contactParts = []
    if (formData.phone) contactParts.push(formData.phone)
    if (formData.email) contactParts.push(formData.email)
    if (formData.target) contactParts.push(formData.target)
    if (contactParts.length > 0) {
      pdf.setFontSize(10)
      pdf.setTextColor(100, 100, 100)
      pdf.text(contactParts.join('  |  '), pageWidth / 2, y, { align: 'center' })
      y += 10
    }

    y += 5
    pdf.setDrawColor(0, 123, 255)
    pdf.setLineWidth(0.5)
    pdf.line(margin, y, pageWidth - margin, y)
    y += 10

    const addSection = (title) => {
      pdf.setFontSize(14)
      pdf.setTextColor(0, 123, 255)
      pdf.text(title, margin, y)
      y += 7
    }

    const addItem = (mainText, subText, date) => {
      pdf.setFontSize(11)
      pdf.setTextColor(0, 0, 0)
      let lineX = margin
      pdf.text(mainText, lineX, y)
      if (subText) {
        lineX += pdf.getTextWidth(mainText) + 2
        pdf.setFontSize(10)
        pdf.setTextColor(100, 100, 100)
        pdf.text(subText, lineX, y)
      }
      if (date) {
        const dateWidth = pdf.getTextWidth(date)
        pdf.text(date, pageWidth - margin - dateWidth, y)
      }
      y += 5
    }

    const addContent = (text) => {
      if (!text) return
      const lines = text.split('\n').filter(l => l.trim())
      lines.forEach(line => {
        pdf.setFontSize(10)
        pdf.setTextColor(60, 60, 60)
        const maxWidth = contentWidth
        const splitted = pdf.splitTextToSize(line, maxWidth)
        splitted.forEach(s => {
          if (y > 277) {
            pdf.addPage()
            y = 20
          }
          pdf.text('• ' + s, margin + 3, y)
          y += 5
        })
      })
      y += 3
    }

    if (formData.educations.length > 0) {
      addSection('教育经历')
      formData.educations.forEach(e => {
        addItem(e.school || '', `${e.degree || ''} | ${e.major || ''}`, `${e.startDate || ''} - ${e.endDate || ''}`)
        y += 3
      })
      y += 5
    }

    if (formData.experiences.length > 0) {
      addSection('工作经历')
      formData.experiences.forEach(e => {
        addItem(e.company || '', e.position || '', `${e.startDate || ''} - ${e.endDate || ''}`)
        addContent(e.content)
        y += 3
      })
      y += 5
    }

    if (formData.projects.length > 0) {
      addSection('项目经历')
      formData.projects.forEach(p => {
        addItem(p.name || '', '', `${p.startDate || ''} - ${p.endDate || ''}`)
        if (p.techStack) {
          pdf.setFontSize(9)
          pdf.setTextColor(0, 123, 255)
          pdf.text(p.techStack, margin + 3, y)
          y += 5
        }
        addContent(p.description)
        y += 3
      })
      y += 5
    }

    if (formData.skills) {
      addSection('技能证书')
      const skills = formData.skills.split('\n').filter(s => s.trim())
      const skillText = skills.join('  |  ')
      pdf.setFontSize(10)
      pdf.setTextColor(60, 60, 60)
      const skillLines = pdf.splitTextToSize(skillText, contentWidth)
      skillLines.forEach(line => {
        if (y > 277) {
          pdf.addPage()
          y = 20
        }
        pdf.text(line, margin, y)
        y += 5
      })
    }

    pdf.save(`${formData.name || '简历'}_${new Date().toLocaleDateString('zh-CN').replace(/\//g, '-')}.pdf`)
  } catch (e) {
    console.error('PDF导出失败', e)
    alert('PDF导出失败')
  }
}

const exportToWord = async () => {
  try {
    const children = []

    if (formData.name) {
      children.push(
        new Paragraph({
          children: [
            new TextRun({
              text: formData.name,
              bold: true,
              size: 48,
              color: '007bff'
            })
          ],
          alignment: 'center'
        })
      )
    }

    const contactParts = []
    if (formData.phone) contactParts.push(formData.phone)
    if (formData.email) contactParts.push(formData.email)
    if (formData.target) contactParts.push(formData.target)
    if (contactParts.length > 0) {
      children.push(
        new Paragraph({
          children: [
            new TextRun({
              text: contactParts.join('  |  '),
              size: 20,
              color: '666666'
            })
          ],
          alignment: 'center'
        })
      )
    }

    children.push(new Paragraph({ children: [] }))

    const addWordSection = (title, items) => {
      if (!items || items.length === 0) return
      children.push(
        new Paragraph({
          children: [
            new TextRun({
              text: title,
              bold: true,
              size: 28,
              color: '007bff'
            })
          ]
        })
      )
      items.forEach(item => {
        if (typeof item === 'string') {
          children.push(
            new Paragraph({
              children: [
                new TextRun({ text: '• ' + item, size: 22 })
              ]
            })
          )
        }
      })
      children.push(new Paragraph({ children: [] }))
    }

    if (formData.educations.length > 0) {
      children.push(
        new Paragraph({
          children: [
            new TextRun({
              text: '教育经历',
              bold: true,
              size: 28,
              color: '007bff'
            })
          ]
        })
      )
      formData.educations.forEach(e => {
        children.push(
          new Paragraph({
            children: [
              new TextRun({ text: `${e.school || ''}  |  ${e.degree || ''} | ${e.major || ''}  |  ${e.startDate || ''} - ${e.endDate || ''}`, size: 22 })
            ]
          })
        )
      })
      children.push(new Paragraph({ children: [] }))
    }

    if (formData.experiences.length > 0) {
      children.push(
        new Paragraph({
          children: [
            new TextRun({
              text: '工作经历',
              bold: true,
              size: 28,
              color: '007bff'
            })
          ]
        })
      )
      formData.experiences.forEach(e => {
        children.push(
          new Paragraph({
            children: [
              new TextRun({ text: `${e.company || ''} - ${e.position || ''}  |  ${e.startDate || ''} - ${e.endDate || ''}`, bold: true, size: 24 })
            ]
          })
        )
        if (e.content) {
          e.content.split('\n').filter(l => l.trim()).forEach(line => {
            children.push(
              new Paragraph({
                children: [
                  new TextRun({ text: '• ' + line, size: 22 })
                ]
              })
            )
          })
        }
      })
      children.push(new Paragraph({ children: [] }))
    }

    if (formData.projects.length > 0) {
      children.push(
        new Paragraph({
          children: [
            new TextRun({
              text: '项目经历',
              bold: true,
              size: 28,
              color: '007bff'
            })
          ]
        })
      )
      formData.projects.forEach(p => {
        children.push(
          new Paragraph({
            children: [
              new TextRun({ text: `${p.name || ''}  |  ${p.startDate || ''} - ${p.endDate || ''}`, bold: true, size: 24 })
            ]
          })
        )
        if (p.techStack) {
          children.push(
            new Paragraph({
              children: [
                new TextRun({ text: p.techStack, size: 20, color: '007bff' })
              ]
            })
          )
        }
        if (p.description) {
          p.description.split('\n').filter(l => l.trim()).forEach(line => {
            children.push(
              new Paragraph({
                children: [
                  new TextRun({ text: '• ' + line, size: 22 })
                ]
              })
            )
          })
        }
      })
      children.push(new Paragraph({ children: [] }))
    }

    if (formData.skills) {
      children.push(
        new Paragraph({
          children: [
            new TextRun({
              text: '技能证书',
              bold: true,
              size: 28,
              color: '007bff'
            })
          ]
        })
      )
      formData.skills.split('\n').filter(s => s.trim()).forEach(skill => {
        children.push(
          new Paragraph({
            children: [
              new TextRun({ text: '• ' + skill, size: 22 })
            ]
          })
        )
      })
    }

    const doc = new Document({
      sections: [{
        children: children
      }]
    })

    const blob = await Packer.toBlob(doc)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${formData.name || '简历'}_${new Date().toLocaleDateString('zh-CN').replace(/\//g, '-')}.docx`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    console.error('Word导出失败', e)
    alert('Word导出失败')
  }
}

onMounted(() => {
  const fromId = route.query.from
  const id = route.params.id

  if (id) {
    loadHistoryDetail(id)
  } else if (fromId) {
    loadHistoryDetail(fromId)
  } else {
    loadDraft()
  }
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
  height: 80px;
  max-height: 300px;
  resize: vertical;
  font-family: inherit;
  overflow-y: auto;
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

.preview-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.template-selector {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.selector-label {
  font-size: 14px;
  color: #666;
}

.template-select {
  padding: 0.4rem 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background: #fff;
  cursor: pointer;
}

.export-buttons {
  display: flex;
  gap: 0.5rem;
  margin-left: auto;
}

.btn-export {
  padding: 0.4rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
}

.btn-pdf {
  background: #dc3545;
  color: #fff;
}

.btn-word {
  background: #007bff;
  color: #fff;
}

.preview-content {
  background: #e8e8e8;
  padding: 1rem;
  border-radius: 4px;
  overflow-y: auto;
  max-height: calc(100vh - 220px);
}

.resume-paper {
  background: #fff;
  padding: 40px;
  max-width: 800px;
  margin: 0 auto;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

/* Classic Template (Default Blue) */
.template-classic .resume-paper {
  font-family: 'Georgia', serif;
}

.template-classic .resume-name {
  color: #007bff;
  border-bottom: 2px solid #007bff;
  padding-bottom: 10px;
}

.template-classic .resume-section .section-title {
  color: #007bff;
  border-left: 4px solid #007bff;
  padding-left: 10px;
}

.template-classic .skill-tag {
  background: #e7f3ff;
  color: #007bff;
}

/* Modern Template */
.template-modern .resume-paper {
  font-family: 'Segoe UI', sans-serif;
}

.template-modern .resume-name {
  color: #333;
  font-weight: 300;
  letter-spacing: 2px;
}

.template-modern .resume-section .section-title {
  color: #333;
  border-bottom: 1px solid #333;
  padding-bottom: 5px;
}

.template-modern .skill-tag {
  background: #333;
  color: #fff;
}

/* Tech Template */
.template-tech .resume-paper {
  font-family: 'Consolas', monospace;
  background: #1a1a2e;
  color: #eee;
}

.template-tech .resume-name {
  color: #00d4ff;
  text-shadow: 0 0 10px rgba(0,212,255,0.5);
}

.template-tech .resume-section .section-title {
  color: #00d4ff;
}

.template-tech .skill-tag {
  background: transparent;
  border: 1px solid #00d4ff;
  color: #00d4ff;
}

.template-tech .item-header {
  border-left: 3px solid #00d4ff;
  padding-left: 8px;
}

/* Elegant Template */
.template-elegant .resume-paper {
  font-family: 'Times New Roman', serif;
  background: #faf8f5;
}

.template-elegant .resume-name {
  color: #2c3e50;
  font-style: italic;
}

.template-elegant .resume-section .section-title {
  color: #8b4513;
  border-bottom: 2px solid #8b4513;
}

.template-elegant .skill-tag {
  background: #f5ebe0;
  color: #8b4513;
}

/* Fresh Template */
.template-fresh .resume-paper {
  font-family: 'Arial', sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.template-fresh .resume-name {
  color: #fff;
}

.template-fresh .resume-contact {
  color: rgba(255,255,255,0.9);
}

.template-fresh .resume-section {
  background: rgba(255,255,255,0.1);
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
}

.template-fresh .resume-section .section-title {
  color: #fff;
  margin-top: 0;
}

.template-fresh .item-header {
  color: #fff;
}

.template-fresh .item-content,
.template-fresh .item-tech,
.template-fresh .item-list li {
  color: rgba(255,255,255,0.85);
}

.template-fresh .skill-tag {
  background: rgba(255,255,255,0.2);
  color: #fff;
  border: 1px solid rgba(255,255,255,0.3);
}

/* Resume Styles */
.resume-header {
  text-align: center;
  margin-bottom: 30px;
}

.resume-name {
  font-size: 32px;
  font-weight: bold;
  margin: 0 0 10px 0;
}

.resume-contact {
  font-size: 14px;
  color: #666;
}

.resume-contact span {
  margin: 0 8px;
}

.resume-section {
  margin-bottom: 25px;
}

.resume-section .section-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 15px 0;
  color: #007bff;
}

.section-item {
  margin-bottom: 15px;
}

.item-header {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 5px;
}

.item-title {
  font-weight: 600;
  font-size: 15px;
  color: #333;
}

.item-sub {
  color: #666;
  font-size: 14px;
}

.item-date {
  color: #999;
  font-size: 13px;
  margin-left: auto;
}

.item-tech {
  color: #007bff;
  font-size: 13px;
  margin-bottom: 5px;
}

.item-content {
  color: #666;
  font-size: 14px;
}

.item-list {
  margin: 8px 0;
  padding-left: 20px;
  color: #555;
  font-size: 14px;
}

.item-list li {
  margin: 4px 0;
  line-height: 1.5;
}

.skills-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  padding: 4px 12px;
  background: #e7f3ff;
  color: #007bff;
  border-radius: 20px;
  font-size: 13px;
}

@media (max-width: 900px) {
  .edit-container {
    grid-template-columns: 1fr;
  }
}
</style>