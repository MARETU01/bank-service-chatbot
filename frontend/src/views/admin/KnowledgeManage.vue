<template>
  <div class="knowledge-manage">
    <div class="page-header">
      <h2>📚 Knowledge Base Management</h2>
      <p class="page-desc">Manage the knowledge base content for the chatbot, upload documents for automatic vectorization</p>
    </div>

    <!-- Important Notice -->
    <div class="notice-card">
      <div class="notice-icon">⚠️</div>
      <div class="notice-content">
        <h4>Document Upload Guidelines</h4>
        <ul>
          <li><strong>PDF Files</strong>: Each <strong>page</strong> is processed separately (pagesPerDocument=1), with each page vectorized as an independent document. Be sure to use this format for PDF submission, otherwise it may cause abnormal knowledge base slicing.</li>
          <li><strong>TXT Files</strong>: The entire file is treated as one document, sliced by TokenTextSplitter at 800 tokens before being stored.</li>
          <li>After upload, documents will automatically undergo <strong>text slicing → Embedding vectorization → Writing to Redis vector database</strong>. This process may take some time.</li>
          <li>Currently only <strong>PDF</strong> and <strong>TXT</strong> formats are supported.</li>
        </ul>
      </div>
    </div>

    <!-- Toolbar -->
    <div class="toolbar">
      <div class="toolbar-left">
        <button class="btn btn-success" @click="triggerFileInput">📤 Upload Documents</button>
        <input
          ref="fileInput"
          type="file"
          multiple
          accept=".pdf,.txt"
          style="display: none"
          @change="handleFileSelect"
        />
        <span class="file-hint">PDF, TXT supported. Multiple files allowed</span>
      </div>
      <div class="toolbar-right">
        <button class="btn btn-danger" @click="handleClearKnowledge" :disabled="clearing">
          {{ clearing ? 'Clearing...' : '🗑️ Clear Knowledge Base' }}
        </button>
      </div>
    </div>

    <!-- Drag & Drop Upload Zone -->
    <div
      class="upload-zone"
      :class="{ 'drag-over': isDragOver, 'uploading': uploading }"
      @dragover.prevent="isDragOver = true"
      @dragleave.prevent="isDragOver = false"
      @drop.prevent="handleDrop"
    >
      <div v-if="uploading" class="upload-progress">
        <div class="spinner"></div>
        <p>Uploading and processing documents, please wait...</p>
        <p class="progress-hint">PDFs will be vectorized after page-by-page slicing, which may take longer</p>
        <div v-if="uploadProgress > 0" class="progress-bar">
          <div class="progress-fill" :style="{ width: uploadProgress + '%' }"></div>
        </div>
        <span v-if="uploadProgress > 0" class="progress-text">{{ uploadProgress }}%</span>
      </div>
      <div v-else>
        <div class="upload-icon">📁</div>
        <p class="upload-text">Drag and drop files here, or click the button above to select files</p>
        <p class="upload-hint">PDF (page-by-page slicing) and TXT files supported. Batch upload allowed</p>
      </div>
    </div>

    <!-- Pending Files List -->
    <div v-if="selectedFiles.length > 0" class="pending-files">
      <div class="section-header">
        <h3>📋 Files to Upload ({{ selectedFiles.length }})</h3>
        <div class="section-actions">
          <button class="btn btn-primary" @click="handleUpload" :disabled="uploading">
            {{ uploading ? 'Uploading...' : 'Confirm Upload' }}
          </button>
          <button class="btn btn-cancel" @click="clearSelectedFiles" :disabled="uploading">Clear List</button>
        </div>
      </div>
      <div class="file-list">
        <div class="file-item" v-for="(file, index) in selectedFiles" :key="index">
          <div class="file-info">
            <span class="file-icon">{{ getFileIcon(file.name) }}</span>
            <span class="file-name">{{ file.name }}</span>
            <span class="file-size">{{ formatFileSize(file.size) }}</span>
            <span class="file-type-tag" :class="getFileTypeClass(file.name)">
              {{ getFileExtension(file.name).toUpperCase() }}
            </span>
            <span v-if="getFileExtension(file.name) === 'pdf'" class="slice-hint">Page-by-page slicing</span>
          </div>
          <button class="btn-sm btn-delete" @click="removeFile(index)" :disabled="uploading">✕</button>
        </div>
      </div>
    </div>

    <!-- Upload Results -->
    <div v-if="uploadResults.length > 0" class="upload-results">
      <div class="section-header">
        <h3>📊 Recent Upload Results</h3>
        <button class="btn-sm btn-clear-results" @click="uploadResults = []">Clear Records</button>
      </div>
      <div class="result-list">
        <div
          class="result-item"
          v-for="(result, index) in uploadResults"
          :key="index"
          :class="result.status === 'success' ? 'result-success' : 'result-failed'"
        >
          <div class="result-icon">{{ result.status === 'success' ? '✅' : '❌' }}</div>
          <div class="result-info">
            <span class="result-filename">{{ result.filename }}</span>
            <span v-if="result.status === 'success' && result.detail" class="result-detail">
              Type: {{ result.detail.fileType?.toUpperCase() }} |
              Original Docs: {{ result.detail.originalDocCount }} |
              Chunks: {{ result.detail.chunkCount }}
            </span>
            <span v-if="result.status === 'failed'" class="result-error">
              Error: {{ result.error }}
            </span>
          </div>
          <div class="result-status">
            <span class="status-badge" :class="result.status === 'success' ? 'active' : 'inactive'">
              {{ result.status === 'success' ? 'Success' : 'Failed' }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, getCurrentInstance } from 'vue'
import { knowledgeApi } from '@/api/api'

export default {
  name: 'KnowledgeManage',
  setup() {
    const { proxy } = getCurrentInstance()

    const fileInput = ref(null)
    const selectedFiles = ref([])
    const uploading = ref(false)
    const uploadProgress = ref(0)
    const clearing = ref(false)
    const isDragOver = ref(false)
    const uploadResults = ref([])

    // 允许的文件类型
    const ALLOWED_EXTENSIONS = ['pdf', 'txt']

    /**
     * Trigger file selection dialog
     */
    const triggerFileInput = () => {
      fileInput.value?.click()
    }

    /**
     * Handle file selection
     */
    const handleFileSelect = (event) => {
      const files = Array.from(event.target.files)
      addFiles(files)
      // 重置 input，允许重复选择同一文件
      event.target.value = ''
    }

    /**
     * Handle drag and drop
     */
    const handleDrop = (event) => {
      isDragOver.value = false
      const files = Array.from(event.dataTransfer.files)
      addFiles(files)
    }

    /**
     * Add files to pending upload list (with validation)
     */
    const addFiles = (files) => {
      const validFiles = []
      const invalidFiles = []

      files.forEach(file => {
        const ext = getFileExtension(file.name)
        if (ALLOWED_EXTENSIONS.includes(ext)) {
          // 检查是否已存在同名文件
          const exists = selectedFiles.value.some(f => f.name === file.name && f.size === file.size)
          if (!exists) {
            validFiles.push(file)
          }
        } else {
          invalidFiles.push(file.name)
        }
      })

      if (invalidFiles.length > 0) {
        proxy.$message.warning(`The following file formats are not supported and have been skipped: ${invalidFiles.join(', ')}. Only PDF and TXT formats are supported.`)
      }

      if (validFiles.length > 0) {
        selectedFiles.value.push(...validFiles)
        proxy.$message.success(`${validFiles.length} file(s) added`)
      }
    }

    /**
     * Remove pending file
     */
    const removeFile = (index) => {
      selectedFiles.value.splice(index, 1)
    }

    /**
     * Clear pending list
     */
    const clearSelectedFiles = () => {
      selectedFiles.value = []
    }

    /**
     * Execute upload
     * Note: PDF files will be sliced page by page on the backend (pagesPerDocument=1),
     *       be sure to use multipart/form-data format with parameter name "files",
     *       do not modify this submission format, otherwise it will cause knowledge base slicing issues
     */
    const handleUpload = async () => {
      if (selectedFiles.value.length === 0) {
        proxy.$message.warning('Please select files to upload first')
        return
      }

      uploading.value = true
      uploadProgress.value = 0

      try {
        const response = await knowledgeApi.uploadDocuments(
          selectedFiles.value,
          (progressEvent) => {
            if (progressEvent.total) {
              uploadProgress.value = Math.round((progressEvent.loaded * 100) / progressEvent.total)
            }
          }
        )

        const { code, data, message } = response.data
        if (code === 1 || code === 200) {
          // 将结果添加到上传记录
          if (Array.isArray(data)) {
            uploadResults.value = [...data, ...uploadResults.value]
          }

          const successCount = data?.filter(r => r.status === 'success').length || 0
          const failCount = data?.filter(r => r.status === 'failed').length || 0

          if (failCount === 0) {
            proxy.$message.success(`All files uploaded successfully! ${successCount} file(s) stored`)
          } else {
            proxy.$message.warning(`Upload completed: ${successCount} successful, ${failCount} failed`)
          }

          // 清空待上传列表
          selectedFiles.value = []
        } else {
          proxy.$message.error(message || 'Upload failed')
        }
      } catch (error) {
        console.error('Upload documents failed:', error)
        proxy.$message.error('Upload failed: ' + (error.response?.data?.message || error.message || 'Network error'))
      } finally {
        uploading.value = false
        uploadProgress.value = 0
      }
    }

    /**
     * Clear knowledge base
     */
    const handleClearKnowledge = async () => {
      if (!confirm('⚠️ Are you sure you want to clear the entire knowledge base?\n\nThis operation will delete all vector data in the database and cannot be undone!')) {
        return
      }

      clearing.value = true
      try {
        const response = await knowledgeApi.clearKnowledge()
        const { code, message } = response.data
        if (code === 1 || code === 200) {
          proxy.$message.success('Knowledge base cleared')
          uploadResults.value = []
        } else {
          proxy.$message.error(message || 'Clear failed')
        }
      } catch (error) {
        console.error('Clear knowledge base failed:', error)
        proxy.$message.error('Clear failed: ' + (error.response?.data?.message || error.message || 'Network error'))
      } finally {
        clearing.value = false
      }
    }

    // ==================== Utility Methods ====================

    const getFileExtension = (filename) => {
      return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase()
    }

    const getFileIcon = (filename) => {
      const ext = getFileExtension(filename)
      return ext === 'pdf' ? '📕' : '📄'
    }

    const getFileTypeClass = (filename) => {
      const ext = getFileExtension(filename)
      return ext === 'pdf' ? 'type-pdf' : 'type-txt'
    }

    const formatFileSize = (bytes) => {
      if (bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }

    return {
      fileInput,
      selectedFiles,
      uploading,
      uploadProgress,
      clearing,
      isDragOver,
      uploadResults,
      triggerFileInput,
      handleFileSelect,
      handleDrop,
      removeFile,
      clearSelectedFiles,
      handleUpload,
      handleClearKnowledge,
      getFileExtension,
      getFileIcon,
      getFileTypeClass,
      formatFileSize
    }
  }
}
</script>

<style scoped>
.knowledge-manage {
  max-width: var(--container-max-width-lg);
  margin: 0 auto;
}

.page-header {
  margin-bottom: var(--spacing-2xl);
}

.page-header h2 {
  font-size: var(--font-size-5xl);
  color: var(--color-white);
  margin: 0 0 var(--spacing-sm) 0;
}

.page-desc {
  color: var(--text-on-gradient-muted);
  font-size: var(--font-size-lg);
  margin: 0;
}

/* 重要提示卡片 */
.notice-card {
  display: flex;
  gap: var(--spacing-lg);
  padding: var(--spacing-xl) var(--spacing-2xl);
  background: rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.3);
  border-radius: var(--radius-2xl);
  margin-bottom: var(--spacing-2xl);
}

.notice-icon {
  font-size: var(--font-size-3xl);
  flex-shrink: 0;
  line-height: 1.5;
}

.notice-content h4 {
  margin: 0 0 var(--spacing-sm) 0;
  color: #fbbf24;
  font-size: var(--font-size-lg);
}

.notice-content ul {
  margin: 0;
  padding-left: var(--spacing-xl);
  color: var(--text-on-gradient);
  font-size: var(--font-size-md);
  line-height: 1.8;
}

.notice-content li {
  margin-bottom: var(--spacing-xs);
}

.notice-content strong {
  color: #fbbf24;
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-2xl);
  gap: var(--spacing-lg);
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
}

.file-hint {
  color: var(--text-on-gradient-muted);
  font-size: var(--font-size-sm);
}

/* 按钮 */
.btn {
  padding: var(--spacing-md) var(--spacing-xl);
  border: none;
  border-radius: var(--radius-xl);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all var(--transition-normal);
  white-space: nowrap;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
}

.btn-primary {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: var(--color-white);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(59, 130, 246, 0.4);
}

.btn-success {
  background: linear-gradient(135deg, #10b981, #059669);
  color: var(--color-white);
}

.btn-success:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(16, 185, 129, 0.4);
}

.btn-danger {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: var(--color-white);
}

.btn-danger:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(239, 68, 68, 0.4);
}

.btn-cancel {
  background: var(--glass-bg);
  color: var(--color-white);
  border: 1px solid var(--glass-border);
}

.btn-cancel:hover:not(:disabled) {
  background: var(--glass-bg-hover);
}

/* 拖拽上传区域 */
.upload-zone {
  border: 2px dashed var(--glass-border);
  border-radius: var(--radius-3xl);
  padding: var(--spacing-4xl);
  text-align: center;
  transition: all var(--transition-normal);
  background: var(--glass-bg);
  margin-bottom: var(--spacing-2xl);
  min-height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-zone.drag-over {
  border-color: #3b82f6;
  background: rgba(59, 130, 246, 0.1);
  transform: scale(1.01);
}

.upload-zone.uploading {
  border-color: #10b981;
  background: rgba(16, 185, 129, 0.05);
}

.upload-icon {
  font-size: 48px;
  margin-bottom: var(--spacing-lg);
}

.upload-text {
  color: var(--text-on-gradient);
  font-size: var(--font-size-xl);
  margin: 0 0 var(--spacing-sm) 0;
}

.upload-hint {
  color: var(--text-on-gradient-muted);
  font-size: var(--font-size-md);
  margin: 0;
}

/* 上传进度 */
.upload-progress {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-md);
}

.upload-progress p {
  color: var(--text-on-gradient);
  font-size: var(--font-size-xl);
  margin: 0;
}

.progress-hint {
  color: var(--text-on-gradient-muted) !important;
  font-size: var(--font-size-md) !important;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255, 255, 255, 0.2);
  border-top-color: #10b981;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.progress-bar {
  width: 300px;
  height: 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #10b981, #34d399);
  border-radius: var(--radius-full);
  transition: width 0.3s ease;
}

.progress-text {
  color: #34d399;
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
}

/* 待上传文件列表 & 上传结果 */
.pending-files, .upload-results {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border-radius: var(--radius-3xl);
  border: 1px solid var(--glass-border);
  overflow: hidden;
  margin-bottom: var(--spacing-2xl);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg) var(--spacing-2xl);
  background: var(--glass-bg-light);
  border-bottom: 1px solid var(--glass-border);
}

.section-header h3 {
  margin: 0;
  color: var(--color-white);
  font-size: var(--font-size-xl);
}

.section-actions {
  display: flex;
  gap: var(--spacing-md);
}

/* 文件列表项 */
.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-2xl);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: all var(--transition-normal);
}

.file-item:hover {
  background: var(--glass-bg-light);
}

.file-item:last-child {
  border-bottom: none;
}

.file-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  flex: 1;
  min-width: 0;
}

.file-icon {
  font-size: var(--font-size-2xl);
  flex-shrink: 0;
}

.file-name {
  color: var(--color-white);
  font-size: var(--font-size-lg);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  color: var(--text-on-gradient-muted);
  font-size: var(--font-size-sm);
  flex-shrink: 0;
}

.file-type-tag {
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  flex-shrink: 0;
}

.type-pdf {
  background: rgba(239, 68, 68, 0.3);
  color: #f87171;
}

.type-txt {
  background: rgba(59, 130, 246, 0.3);
  color: #93c5fd;
}

.slice-hint {
  color: #fbbf24;
  font-size: var(--font-size-xs);
  background: rgba(245, 158, 11, 0.2);
  padding: 2px 8px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.btn-sm {
  padding: var(--spacing-xs) var(--spacing-md);
  border: none;
  border-radius: var(--radius-lg);
  font-size: var(--font-size-xs);
  cursor: pointer;
  transition: all var(--transition-normal);
  color: var(--color-white);
}

.btn-sm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-delete {
  background: rgba(239, 68, 68, 0.4);
  font-size: var(--font-size-lg);
  padding: var(--spacing-xs) var(--spacing-sm);
}

.btn-delete:hover:not(:disabled) {
  background: rgba(239, 68, 68, 0.6);
}

.btn-clear-results {
  background: rgba(107, 114, 128, 0.4);
}

.btn-clear-results:hover {
  background: rgba(107, 114, 128, 0.6);
}

/* 上传结果项 */
.result-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-lg) var(--spacing-2xl);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.result-item:last-child {
  border-bottom: none;
}

.result-success {
  border-left: 3px solid #10b981;
}

.result-failed {
  border-left: 3px solid #ef4444;
}

.result-icon {
  font-size: var(--font-size-2xl);
  flex-shrink: 0;
}

.result-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.result-filename {
  color: var(--color-white);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-medium);
}

.result-detail {
  color: var(--text-on-gradient-muted);
  font-size: var(--font-size-sm);
}

.result-error {
  color: #f87171;
  font-size: var(--font-size-sm);
}

.result-status {
  flex-shrink: 0;
}

.status-badge {
  padding: var(--spacing-xs) var(--spacing-md);
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
}

.status-badge.active {
  background: rgba(16, 185, 129, 0.3);
  color: #34d399;
}

.status-badge.inactive {
  background: rgba(239, 68, 68, 0.3);
  color: #f87171;
}

/* 响应式 */
@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-left, .toolbar-right {
    justify-content: center;
  }

  .section-header {
    flex-direction: column;
    gap: var(--spacing-md);
    align-items: flex-start;
  }

  .file-info {
    flex-wrap: wrap;
  }

  .notice-card {
    flex-direction: column;
  }

  .upload-zone {
    padding: var(--spacing-2xl);
    min-height: 140px;
  }
}
</style>
