<template>
  <div class="max-w-6xl mx-auto animate-fade-in">
    <!-- 顶部导航 -->
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 mb-6">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <el-button circle text @click="router.back()">
            <el-icon :size="20"><ArrowLeft /></el-icon>
          </el-button>
          <div>
            <h2 class="text-xl font-bold text-gray-800">发布新作业</h2>
            <p class="text-sm text-gray-500">创建作业并配置 AI 智能批改参数</p>
          </div>
        </div>
        <div class="flex gap-3">
          <el-button @click="saveDraft">保存草稿</el-button>
          <el-button type="primary" :loading="publishing" @click="publish">
            发布作业
          </el-button>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-12 gap-6">
      <!-- 左侧主要内容 -->
      <div class="col-span-8 space-y-6">
        <!-- 基本信息 -->
        <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h3 class="font-bold mb-4 flex items-center gap-2">
            <el-icon class="text-primary-500"><Document /></el-icon>
            基本信息
          </h3>
          <el-form :model="form" label-position="top">
            <div class="grid grid-cols-2 gap-4">
              <el-form-item label="标题" required>
                <el-input v-model="form.title" placeholder="请输入作业标题" size="large" clearable />
              </el-form-item>
              <el-form-item label="关联课程" required>
                <el-select v-model="form.course" placeholder="选择学科/课程" size="large" class="w-full">
                  <el-option label="语文" value="chinese" />
                </el-select>
              </el-form-item>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <el-form-item label="截止日期" required>
                <el-date-picker v-model="form.deadline" type="datetime" placeholder="选择截止时间" size="large" class="w-full" />
              </el-form-item>
              <el-form-item label="发布班级" required>
                <el-select v-model="form.classes" multiple placeholder="选择班级" size="large" class="w-full">
                  <el-option 
                    v-for="cls in classList" 
                    :key="cls.id" 
                    :label="cls.name" 
                    :value="cls.id" 
                  />
                </el-select>
              </el-form-item>
            </div>

            <el-form-item label="说明">
              <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入作业说明、要求或注意事项" />
            </el-form-item>
          </el-form>
        </div>

        <!-- 文件批量导入区域 -->
        <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <h3 class="font-bold mb-4 flex items-center gap-2">
            <el-icon class="text-green-500"><Upload /></el-icon>
            题目导入
          </h3>
          
          <!-- Word格式要求说明 -->
          <el-alert type="info" :closable="false" class="mb-4">
            <template #title>
              <div class="flex items-center gap-2">
                <el-icon><InfoFilled /></el-icon>
                <span>Word文档格式要求</span>
              </div>
            </template>
            <div class="mt-2 text-sm space-y-1">
              <p><strong>题目格式：</strong>每道题以 "题号." 开头，如 "1."、"2."</p>
              <p><strong>题型标识：</strong>选择题用 【选择】，填空题用 【填空】，判断题用 【判断】，阅读理解用 【阅读】</p>
              <p><strong>选项格式：</strong>A.选项内容  B.选项内容  C.选项内容  D.选项内容</p>
              <p><strong>答案格式：</strong>答案: X 或 答案: 内容（放在每道题末尾）</p>
              <p><strong>分值格式：</strong>分值: X分（已禁用，作业不需要分数）</p>
              <p><strong>示例：</strong>1.【选择】(3分) 1+1=?  A.1  B.2  C.3  D.4  答案: B</p>
            </div>
          </el-alert>
          
          <!-- 上传拖拽区 -->
          <el-upload
            drag
            action="#"
            :auto-upload="false"
            :on-change="handleFileUpload"
            :file-list="uploadedFiles"
            accept=".doc,.docx,.pdf,.xlsx,.xls,.txt"
            class="w-full"
          >
            <el-icon class="el-icon--upload" :size="50"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽题目文件到此处，或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip flex items-center justify-center gap-4 text-sm">
                <span>支持格式：Word (.doc/.docx)、Excel (.xlsx/.xls)、PDF (.pdf)、文本 (.txt)</span>
                <el-button type="primary" link @click.stop="downloadTemplate">
                  <el-icon class="mr-1"><Download /></el-icon>下载导入模板
                </el-button>
              </div>
            </template>
          </el-upload>

          <!-- 已上传文件列表 -->
          <div v-if="uploadedFiles.length > 0" class="mt-4">
            <div class="flex items-center justify-between mb-2">
              <span class="text-sm font-medium">已上传文件</span>
              <el-button type="primary" size="small" :loading="parsingFile" @click="parseUploadedFiles">
                <el-icon class="mr-1"><MagicStick /></el-icon>AI 解析导入
              </el-button>
            </div>
            <div class="space-y-2">
              <div v-for="(file, idx) in uploadedFiles" :key="idx" class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <div class="flex items-center gap-3">
                  <el-icon :size="20" class="text-primary-500"><Document /></el-icon>
                  <span class="text-sm">{{ file.name }}</span>
                  <el-tag size="small" type="info">{{ formatFileSize(file.size) }}</el-tag>
                </div>
                <el-button type="danger" text circle size="small" @click="removeUploadedFile(idx)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>

          <!-- AI 解析预览 -->
          <div v-if="parsedQuestions.length > 0" class="mt-4 p-4 bg-blue-50 rounded-xl border border-blue-100">
            <div class="flex items-center justify-between mb-3">
              <div class="flex items-center gap-2 text-blue-700 font-medium">
                <el-icon><Cpu /></el-icon>
                <span>AI 解析预览</span>
              </div>
              <el-tag type="primary" effect="light">{{ parsedQuestions.length }} 道题</el-tag>
            </div>
            <div class="space-y-2 max-h-48 overflow-y-auto">
              <div v-for="(q, idx) in parsedQuestions" :key="idx" class="p-2 bg-white rounded-lg text-sm">
                <div class="flex items-center gap-2">
                  <span class="font-bold">{{ idx + 1 }}.</span>
                  <span class="truncate flex-1">{{ q.content.substring(0, 50) }}...</span>
                  <el-tag size="small" :type="q.type === 'objective' ? 'success' : 'warning'">
                    {{ q.type === 'objective' ? '客观题' : '主观题' }}
                  </el-tag>
                </div>
              </div>
            </div>
            <div class="flex gap-2 mt-3">
              <el-button type="primary" size="small" @click="applyParsedQuestions">应用到题目列表</el-button>
              <el-button size="small" @click="clearParsedQuestions">清除</el-button>
            </div>
          </div>
        </div>

        <!-- 题目列表 -->
        <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
          <div class="flex items-center justify-between mb-4">
            <h3 class="font-bold flex items-center gap-2">
              <el-icon class="text-blue-500"><List /></el-icon>
              题目设置
              <el-tag type="info" effect="light" round>{{ form.questions.length }} 题</el-tag>
            </h3>
            <div class="flex gap-2">
              <el-button type="primary" plain @click="openQuestionBankDialog">
                <el-icon class="mr-1"><Collection /></el-icon>使用题库
              </el-button>
              <el-button type="success" plain @click="aiGenerateQuestion">
                <el-icon class="mr-1"><Cpu /></el-icon>AI 出题
              </el-button>
            </div>
          </div>

          <div class="space-y-4">
            <div v-for="(q, idx) in form.questions" :key="idx" 
                 class="p-5 border border-gray-100 rounded-xl hover:border-primary-200 transition-all"
                 :class="{ 'ring-2 ring-primary-100': currentQuestion === idx }">
              <!-- 题目标题栏 -->
              <div class="flex items-center gap-3 mb-4">
                <span class="w-10 h-10 rounded-xl bg-primary-100 text-primary-700 flex items-center justify-center font-bold text-lg">{{ idx + 1 }}</span>
                <el-radio-group v-model="q.type" size="small">
                <el-radio-button label="single_choice">单选题</el-radio-button>
                <el-radio-button label="multiple_choice">多选题</el-radio-button>
                <el-radio-button label="fill_blank">填空题</el-radio-button>
                <el-radio-button label="true_false">判断题</el-radio-button>
                <el-radio-button label="short_answer">简答题</el-radio-button>
                <el-radio-button label="reading_comprehension">阅读理解</el-radio-button>
              </el-radio-group>
                <el-select v-model="q.difficulty" placeholder="难度" size="small" style="width: 100px">
                  <el-option label="简单" value="easy" />
                  <el-option label="中等" value="medium" />
                  <el-option label="困难" value="hard" />
                </el-select>
                <!-- 分值设置已移除 - 作业不需要分数 -->
                <el-button type="danger" text circle @click="removeQuestion(idx)" class="ml-auto">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>

              <!-- 题目内容 -->
              <el-form-item class="mb-3">
                <el-input v-model="q.content" type="textarea" :rows="3" placeholder="输入题目内容" />
              </el-form-item>

              <!-- 单选题/多选题选项区域 -->
              <template v-if="q.type === 'single_choice' || q.type === 'multiple_choice'">
                <div class="space-y-2 mb-3 bg-gray-50 rounded-lg p-3">
                  <div class="text-sm font-medium text-gray-600 mb-2">选项设置</div>
                  <div v-for="(_opt, optIdx) in q.options" :key="optIdx" class="flex items-center gap-2">
                    <el-radio v-if="q.type === 'single_choice'" v-model="q.correctAnswer" :label="optIdx" class="!mr-0">
                      <span class="font-bold mr-2">{{ ['A', 'B', 'C', 'D', 'E', 'F'][optIdx] }}.</span>
                    </el-radio>
                    <el-checkbox v-else v-model="q.correctAnswers" :label="optIdx" class="!mr-0">
                      <span class="font-bold mr-2">{{ ['A', 'B', 'C', 'D', 'E', 'F'][optIdx] }}.</span>
                    </el-checkbox>
                    <el-input v-model="q.options[optIdx]" placeholder="选项内容" size="small" class="flex-1" />
                    <el-button v-if="q.options.length > 2" type="danger" text circle size="small" @click="removeOption(q, optIdx)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                  <el-button v-if="q.options.length < 6" type="primary" text @click="addOption(q)">
                    <el-icon class="mr-1"><Plus /></el-icon>添加选项
                  </el-button>
                </div>
              </template>

              <!-- 填空题答案 -->
              <template v-if="q.type === 'fill_blank'">
                <div class="space-y-2 mb-3 bg-gray-50 rounded-lg p-3">
                  <div class="text-sm font-medium text-gray-600 mb-2">答案（多个答案用分号分隔）</div>
                  <el-input v-model="q.correctAnswerText" placeholder="如：3; x=5; 2或3" size="small" />
                </div>
              </template>

              <!-- 判断题答案 -->
              <template v-if="q.type === 'true_false'">
                <div class="space-y-2 mb-3 bg-gray-50 rounded-lg p-3">
                  <div class="text-sm font-medium text-gray-600 mb-2">正确答案</div>
                  <el-radio-group v-model="q.correctAnswer">
                    <el-radio :label="true">正确</el-radio>
                    <el-radio :label="false">错误</el-radio>
                  </el-radio-group>
                </div>
              </template>

              <!-- 简答题/阅读理解参考答案 -->
              <template v-if="q.type === 'short_answer' || q.type === 'reading_comprehension'">
                <div class="space-y-2 mb-3 bg-gray-50 rounded-lg p-3">
                  <div class="text-sm font-medium text-gray-600 mb-2">参考答案</div>
                  <el-input v-model="q.correctAnswerText" type="textarea" :rows="3" placeholder="输入参考答案" size="small" />
                </div>
              </template>

              <!-- 知识点关联 -->
              <div class="flex items-center gap-2">
                <span class="text-sm text-gray-500">知识点：</span>
                <el-select v-model="q.knowledgePoints" multiple placeholder="关联知识点" size="small" class="flex-1">
                  <el-option v-for="kp in knowledgePoints" :key="kp" :label="kp" :value="kp" />
                </el-select>
              </div>

              <!-- AI 分析预览 -->
              <div v-if="q.aiAnalysis" class="mt-3 p-3 bg-blue-50 rounded-lg border border-blue-100">
                <div class="flex items-center gap-2 text-xs text-blue-600 mb-1">
                  <el-icon><Cpu /></el-icon>
                  <span>AI 预分析</span>
                </div>
                <p class="text-sm text-gray-600">{{ q.aiAnalysis }}</p>
              </div>
            </div>
          </div>

          <!-- 添加题目按钮 -->
          <div class="flex gap-3 mt-4">
            <el-button type="primary" plain class="flex-1" @click="addQuestion">
              <el-icon class="mr-1"><Plus /></el-icon>添加题目
            </el-button>
            <el-button type="success" plain class="flex-1" @click="aiGenerateQuestion">
              <el-icon class="mr-1"><Cpu /></el-icon>AI 智能出题
            </el-button>
          </div>
        </div>
      </div>

      <!-- 右侧设置面板 -->
      <div class="col-span-4 space-y-6">
        <!-- AI 智能设置 -->
        <div class="bg-white rounded-2xl p-6 shadow-sm border border-gray-100 sticky top-4">
          <h3 class="font-bold mb-4 flex items-center gap-2">
            <el-icon class="text-purple-500"><Cpu /></el-icon>
            AI 智能批改设置
          </h3>
          
          <div class="space-y-4">
            <el-form-item label="启用 AI 智能批改">
              <el-switch v-model="form.aiEnabled" active-text="启用" inactive-text="关闭" />
            </el-form-item>
            
            <template v-if="form.aiEnabled">
              <el-form-item label="批改严格程度">
                <el-slider v-model="form.aiStrictness" :max="100" show-stops :marks="{0: '宽松', 50: '标准', 100: '严格'}" />
              </el-form-item>
              
              <el-form-item label="评分维度">
                <el-checkbox-group v-model="form.aiDimensions" class="flex flex-col gap-2">
                  <el-checkbox label="accuracy">答案准确性</el-checkbox>
                  <el-checkbox label="logic">逻辑推导</el-checkbox>
                  <el-checkbox label="process">解题过程</el-checkbox>
                  <el-checkbox label="expression">表达规范</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              
              <el-form-item label="自动评语">
                <el-switch v-model="form.aiComment" active-text="生成" inactive-text="不生成" />
              </el-form-item>
            </template>
          </div>

          <el-divider />

          <h3 class="font-bold mb-4">作业设置</h3>
          <div class="space-y-3">
            <el-form-item>
              <el-checkbox v-model="form.allowResubmit">允许重复提交</el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="form.showAnswer">提交后显示答案</el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="form.showAnalysis">显示 AI 解析</el-checkbox>
            </el-form-item>
          </div>

          <el-divider />

          <!-- 作业统计预览 -->
          <h3 class="font-bold mb-4">预览统计</h3>
          <div class="space-y-2 text-sm">
            <div class="flex justify-between">
              <span class="text-gray-500">总题数</span>
              <span class="font-bold">{{ form.questions.length }} 题</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">单选题</span>
              <span class="font-bold">{{ form.questions.filter(q => q.type === 'single_choice').length }} 题</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">多选题</span>
              <span class="font-bold">{{ form.questions.filter(q => q.type === 'multiple_choice').length }} 题</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">填空题</span>
              <span class="font-bold">{{ form.questions.filter(q => q.type === 'fill_blank').length }} 题</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">判断题</span>
              <span class="font-bold">{{ form.questions.filter(q => q.type === 'true_false').length }} 题</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">简答题</span>
              <span class="font-bold">{{ form.questions.filter(q => q.type === 'short_answer').length }} 题</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-500">阅读理解</span>
              <span class="font-bold">{{ form.questions.filter(q => q.type === 'reading_comprehension').length }} 题</span>
            </div>
            <!-- 总分值统计已移除 - 作业不需要分数 -->
          </div>
        </div>
      </div>
    </div>

    <!-- AI 出题对话框 -->
    <el-dialog v-model="showAIGenerateDialog" title="AI 智能出题" width="600px">
      <el-form label-position="top">
        <el-form-item label="出题数量（最多10道）">
          <el-slider v-model="aiGenerateConfig.count" :min="1" :max="10" show-stops :marks="{1: '1', 5: '5', 10: '10'}" />
          <div class="text-sm text-gray-500 mt-1">当前选择：{{ aiGenerateConfig.count }} 道</div>
        </el-form-item>

        <el-form-item label="题目类型">
          <el-checkbox-group v-model="aiGenerateConfig.types">
            <el-checkbox label="single_choice">单选题</el-checkbox>
            <el-checkbox label="multiple_choice">多选题</el-checkbox>
            <el-checkbox label="fill_blank">填空题</el-checkbox>
            <el-checkbox label="true_false">判断题</el-checkbox>
            <el-checkbox label="short_answer">简答题</el-checkbox>
          </el-checkbox-group>
          <div class="text-xs text-gray-400 mt-1">至少选择一种题型</div>
        </el-form-item>

        <el-form-item label="难度">
          <el-radio-group v-model="aiGenerateConfig.difficulty">
            <el-radio-button label="easy">简单</el-radio-button>
            <el-radio-button label="medium">中等</el-radio-button>
            <el-radio-button label="hard">困难</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="知识点">
          <el-select v-model="aiGenerateConfig.knowledgePoint" placeholder="选择知识点（可选）" class="w-full">
            <el-option v-for="kp in chineseKnowledgePoints" :key="kp" :label="kp" :value="kp" />
          </el-select>
        </el-form-item>

        <el-form-item label="出题提示词（可选，最多100字）">
          <el-input
            v-model="aiGenerateConfig.prompt"
            type="textarea"
            :rows="3"
            placeholder="请输入提示词来锁定出题范围，如：重点考察成语辨析、文言文翻译等"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="flex justify-between items-center">
          <div class="text-sm text-gray-500">
            <el-icon class="mr-1"><InfoFilled /></el-icon>
            默认生成高中语文题目
          </div>
          <div class="flex gap-2">
            <el-button @click="showAIGenerateDialog = false">取消</el-button>
            <el-button type="success" @click="confirmAIGenerate" :loading="aiGenerating">
              <el-icon class="mr-1"><Cpu /></el-icon>
              开始出题
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <!-- 题库选择对话框 -->
    <el-dialog v-model="showQuestionBankDialog" title="从题库选择题目标" width="800px" destroy-on-close>
      <div class="mb-4 flex gap-3 flex-wrap">
        <el-input 
          v-model="questionBankSearch" 
          placeholder="搜索题目内容..." 
          clearable 
          style="width: 200px"
          @keyup.enter="loadQuestionBank"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        
        <el-select v-model="questionBankFilter.subject" placeholder="学科" clearable style="width: 120px" @change="loadQuestionBank">
          <el-option label="语文" value="chinese" />
        </el-select>
        
        <el-select v-model="questionBankFilter.type" placeholder="题型" clearable style="width: 120px" @change="loadQuestionBank">
          <el-option label="单选题" value="single_choice" />
          <el-option label="多选题" value="multiple_choice" />
          <el-option label="填空题" value="fill_blank" />
          <el-option label="判断题" value="true_false" />
          <el-option label="简答题" value="short_answer" />
        </el-select>
        
        <el-select v-model="questionBankFilter.difficulty" placeholder="难度" clearable style="width: 120px" @change="loadQuestionBank">
          <el-option label="简单" value="easy" />
          <el-option label="中等" value="medium" />
          <el-option label="困难" value="hard" />
        </el-select>
        
        <el-button type="primary" text @click="loadQuestionBank">
          <el-icon class="mr-1"><RefreshRight /></el-icon>刷新
        </el-button>
      </div>
      
      <el-table 
        :data="questionBankList" 
        style="width: 100%; max-height: 400px; overflow-y: auto;"
        :header-cell-style="{ background: '#f9fafb' }"
        @selection-change="selectedBankQuestions = $event"
        v-loading="questionBankLoading"
      >
        <el-table-column type="selection" width="55" align="center" />
        
        <el-table-column label="题目内容" min-width="250">
          <template #default="{ row }">
            <div class="text-sm text-gray-800 line-clamp-2">{{ row.content }}</div>
          </template>
        </el-table-column>
        
        <el-table-column label="题型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="light">{{ getQuestionTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="难度" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="getDifficultyType(row.difficulty)" effect="light">
              {{ getDifficultyText(row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="学科" width="80" align="center">
          <template #default="{ row }">
            <span class="text-sm text-gray-600">{{ row.subject }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="分值" width="70" align="center">
          <template #default="{ row }">
            <span class="text-sm">{{ row.score }}分</span>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="mt-4 flex justify-end">
        <el-pagination
          v-model:current-page="questionBankPage"
          v-model:page-size="questionBankPageSize"
          :total="questionBankTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadQuestionBank"
          @current-change="loadQuestionBank"
        />
      </div>
      
      <template #footer>
        <div class="flex justify-between items-center">
          <span class="text-sm text-gray-500">已选择 {{ selectedBankQuestions.length }} 道题目</span>
          <div class="flex gap-2">
            <el-button @click="showQuestionBankDialog = false">取消</el-button>
            <el-button type="primary" :disabled="selectedBankQuestions.length === 0" @click="applySelectedQuestions">
              <el-icon class="mr-1"><Plus /></el-icon>添加到作业
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Document, List, Collection, Cpu, Delete, Plus, Upload, UploadFilled, Download, MagicStick, InfoFilled, Search, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { homeworkApi, fileApi, aiApi, classApi, questionApi } from '@/api'
import { useUserStore } from '@/stores'

const router = useRouter()
const userStore = useUserStore()
const publishing = ref(false)
const currentQuestion = ref(0)
const aiGenerating = ref(false)
const showQuestionBankDialog = ref(false)
const showAIGenerateDialog = ref(false)
const aiGenerateConfig = reactive({
  count: 5,
  types: ['single_choice', 'multiple_choice'] as string[],
  difficulty: 'medium',
  prompt: '',
  knowledgePoint: ''
})

// 班级列表
const classList = ref<any[]>([])

// 获取班级列表
const fetchClassList = async () => {
  if (!userStore.user?.id) return
  try {
    const res = await classApi.getClassList(userStore.user.id)
    classList.value = res || []
  } catch (error) {
    console.error('获取班级列表失败', error)
  }
}

// 页面加载时获取班级列表
fetchClassList()

const form = reactive({
  title: '',
  course: '',
  deadline: '',
  classes: [] as string[],
  description: '',
  aiEnabled: true,
  aiStrictness: 70,
  aiDimensions: ['accuracy', 'logic', 'process'],
  aiComment: true,
  allowResubmit: false,
  showAnswer: false,
  showAnalysis: true,
  questions: [
    {
      type: 'short_answer',
      difficulty: 'medium',
      score: 10,
      content: '',
      knowledgePoints: [] as string[],
      options: ['', '', '', ''] as string[],
      correctAnswer: null as number | boolean | null,
      correctAnswers: [] as number[],
      correctAnswerText: '',
      aiAnalysis: ''
    }
  ]
})

// 文件上传相关
const uploadedFiles = ref<File[]>([])
const parsingFile = ref(false)
const parsedQuestions = ref<any[]>([])

const knowledgePoints = ['文言文阅读', '古诗词鉴赏', '现代文阅读', '作文写作', '语言文字运用', '文学常识']
const chineseKnowledgePoints = ['文言文阅读', '古诗词鉴赏', '现代文阅读', '作文写作', '语言文字运用', '文学常识']

// 题库相关
const questionBankList = ref<any[]>([])
const questionBankLoading = ref(false)
const questionBankSearch = ref('')
const questionBankFilter = ref({
  subject: '',
  type: '',
  difficulty: ''
})
const selectedBankQuestions = ref<any[]>([])
const questionBankPage = ref(1)
const questionBankPageSize = ref(10)
const questionBankTotal = ref(0)

// 加载题库列表
const loadQuestionBank = async () => {
  questionBankLoading.value = true
  try {
    console.log('开始加载题库数据...')
    const res = await questionApi.getQuestionList({
      keyword: questionBankSearch.value || undefined,
      subject: questionBankFilter.value.subject || undefined,
      type: questionBankFilter.value.type || undefined,
      difficulty: questionBankFilter.value.difficulty || undefined,
      page: questionBankPage.value - 1,
      size: questionBankPageSize.value
    })
    console.log('题库API返回:', res)
    questionBankList.value = res.content || []
    questionBankTotal.value = res.totalElements || 0
    console.log('题库数据条数:', questionBankList.value.length, '总计:', questionBankTotal.value)
    if (questionBankList.value.length === 0) {
      ElMessage.info('题库暂无数据，请先在题库管理页面添加题目')
    }
  } catch (error: any) {
    console.error('加载题库失败:', error)
    ElMessage.error('加载题库失败: ' + error.message)
  } finally {
    questionBankLoading.value = false
  }
}

// 打开题库对话框时加载数据
const openQuestionBankDialog = () => {
  showQuestionBankDialog.value = true
  selectedBankQuestions.value = []
  questionBankPage.value = 1
  loadQuestionBank()
}

// 应用选中的题库题目到作业
const applySelectedQuestions = () => {
  if (selectedBankQuestions.value.length === 0) {
    ElMessage.warning('请先选择题目')
    return
  }
  
  selectedBankQuestions.value.forEach((q: any, index: number) => {
    // 处理选项格式
    let options: string[] = ['', '', '', '']
    if (q.options) {
      if (typeof q.options === 'string') {
        // 尝试解析JSON或按分隔符分割
        try {
          const parsed = JSON.parse(q.options)
          if (Array.isArray(parsed)) {
            options = parsed
          }
        } catch {
          // 按 | 分割
          options = q.options.split('|').filter((o: string) => o.trim())
        }
      } else if (Array.isArray(q.options)) {
        options = q.options
      }
    }
    
    // 处理正确答案
    let correctAnswer: number | boolean | null = null
    let correctAnswers: number[] = []
    let correctAnswerText = ''
    
    if (q.correctAnswer) {
      const answerStr = q.correctAnswer.toString().trim()
      if (q.type === 'single_choice') {
        if (answerStr === 'A' || answerStr === 'a' || answerStr === '0') correctAnswer = 0
        else if (answerStr === 'B' || answerStr === 'b' || answerStr === '1') correctAnswer = 1
        else if (answerStr === 'C' || answerStr === 'c' || answerStr === '2') correctAnswer = 2
        else if (answerStr === 'D' || answerStr === 'd' || answerStr === '3') correctAnswer = 3
        correctAnswerText = answerStr
      } else if (q.type === 'true_false') {
        const isTrue = ['正确', '对', 'true', 'True', 'TRUE', '0'].includes(answerStr)
        const isFalse = ['错误', '错', 'false', 'False', 'FALSE', '1'].includes(answerStr)
        correctAnswer = isTrue ? true : (isFalse ? false : null)
        correctAnswerText = answerStr
      } else if (q.type === 'multiple_choice') {
        const cleaned = answerStr.replace(/[,，\s]/g, '')
        correctAnswers = cleaned.split('').filter((c: string) => ['A', 'B', 'C', 'D', 'a', 'b', 'c', 'd'].includes(c)).map((c: string) => {
          return ['A', 'B', 'C', 'D', 'a', 'b', 'c', 'd'].indexOf(c) % 4
        }).filter((i: number, idx: number, arr: number[]) => arr.indexOf(i) === idx)
        correctAnswerText = answerStr
      } else {
        correctAnswerText = answerStr
      }
    }
    
    const newQuestion = {
      type: q.type || 'short_answer',
      difficulty: q.difficulty || 'medium',
      score: q.score || 10,
      content: q.content || '',
      knowledgePoints: q.knowledgePoint ? [q.knowledgePoint] : [],
      options: options,
      correctAnswer: correctAnswer,
      correctAnswers: correctAnswers,
      correctAnswerText: correctAnswerText,
      aiAnalysis: q.analysis || ''
    }
    
    // 如果是第一道题且当前第一题是空白题（content为空），则替换第一题
    if (index === 0 && form.questions.length > 0 && !form.questions[0].content) {
      form.questions[0] = newQuestion
    } else {
      form.questions.push(newQuestion)
    }
  })
  
  ElMessage.success(`已添加 ${selectedBankQuestions.value.length} 道题目`)
  showQuestionBankDialog.value = false
  selectedBankQuestions.value = []
}

const totalScore = computed(() => form.questions.reduce((sum, q) => sum + (q.score || 0), 0))

const addQuestion = () => {
  form.questions.push({
    type: 'short_answer',
    difficulty: 'medium',
    score: 10,
    content: '',
    knowledgePoints: [],
    options: ['', '', '', ''],
    correctAnswer: null as number | boolean | null,
    correctAnswers: [],
    correctAnswerText: '',
    aiAnalysis: ''
  })
  currentQuestion.value = form.questions.length - 1
  ElMessage.success('已添加新题目')
}

const removeQuestion = (idx: number) => {
  if (form.questions.length <= 1) {
    ElMessage.warning('至少需要保留一道题目')
    return
  }
  form.questions.splice(idx, 1)
  ElMessage.success('已删除题目')
}

const addOption = (q: any) => {
  q.options.push('')
}

const removeOption = (q: any, idx: number) => {
  q.options.splice(idx, 1)
}

// 获取题型名称
const getQuestionTypeName = (type: string) => {
  const map: Record<string, string> = {
    single_choice: '单选题',
    multiple_choice: '多选题',
    fill_blank: '填空题',
    true_false: '判断题',
    short_answer: '简答题',
    reading_comprehension: '阅读理解'
  }
  return map[type] || type
}

// 获取难度类型
const getDifficultyType = (difficulty: string) => {
  const map: Record<string, string> = {
    easy: 'success',
    medium: 'warning',
    hard: 'danger'
  }
  return map[difficulty] || 'info'
}

// 获取难度文本
const getDifficultyText = (difficulty: string) => {
  const map: Record<string, string> = {
    easy: '简单',
    medium: '中等',
    hard: '困难'
  }
  return map[difficulty] || difficulty
}

const aiGenerateQuestion = () => {
  // 验证是否选择了课程
  if (!form.course) {
    ElMessage.warning('请先选择关联课程')
    return
  }
  showAIGenerateDialog.value = true
}

const confirmAIGenerate = async () => {
  // 验证
  if (aiGenerateConfig.types.length === 0) {
    ElMessage.warning('请至少选择一种题目类型')
    return
  }
  
  aiGenerating.value = true
  
  try {
    const subject = form.course || 'chinese'
    const grade = '高中'
    const subjectName = subject === 'chinese' ? '语文' : subject
    
    // 循环生成指定数量的题目
    for (let i = 0; i < aiGenerateConfig.count; i++) {
      // 随机选择一种题目类型
      const randomType = aiGenerateConfig.types[Math.floor(Math.random() * aiGenerateConfig.types.length)]
      
      // 确定知识点
      let knowledgePoint = aiGenerateConfig.knowledgePoint
      if (!knowledgePoint) {
        knowledgePoint = chineseKnowledgePoints[Math.floor(Math.random() * chineseKnowledgePoints.length)]
      }
      
      ElMessage.info(`正在生成第 ${i + 1}/${aiGenerateConfig.count} 道题目...`)
      
      const result = await aiApi.generateQuestion({
        subject: `${grade}${subjectName}`,
        knowledgePoint,
        difficulty: aiGenerateConfig.difficulty,
        questionType: randomType,
        prompt: aiGenerateConfig.prompt || undefined
      })
      
      const questionData = result.data || result
      
      // 根据题型处理选项和正确答案
      let correctAnswer: number | boolean | null = null
      let correctAnswers: number[] = []
      let correctAnswerText = ''
      let options: string[] = questionData.options || ['', '', '', '']
      
      // 标准化处理 correctAnswer 字段
      let answerStr = ''
      if (questionData.correctAnswer) {
        answerStr = questionData.correctAnswer.toString().trim()
      }
      
      if (randomType === 'single_choice') {
        // 单选题
        if (answerStr) {
          if (answerStr === 'A' || answerStr === 'a') correctAnswer = 0
          else if (answerStr === 'B' || answerStr === 'b') correctAnswer = 1
          else if (answerStr === 'C' || answerStr === 'c') correctAnswer = 2
          else if (answerStr === 'D' || answerStr === 'd') correctAnswer = 3
        }
        correctAnswerText = questionData.correctAnswer || ''
      } else if (randomType === 'true_false') {
        // 判断题：处理多种可能的答案格式，返回布尔值
        if (answerStr) {
          const isTrue = ['正确', '对', 'true', 'True', 'TRUE', '是', 'yes', 'Yes', 'YES'].includes(answerStr)
          const isFalse = ['错误', '错', 'false', 'False', 'FALSE', '否', 'no', 'No', 'NO'].includes(answerStr)
          if (isTrue) correctAnswer = true
          else if (isFalse) correctAnswer = false
        }
        correctAnswerText = answerStr
      } else if (randomType === 'multiple_choice') {
        // 多选题：处理逗号分隔或连续字母格式
        if (answerStr) {
          // 移除逗号、空格，只保留字母
          const cleaned = answerStr.replace(/[,，\s]/g, '')
          correctAnswers = cleaned.split('').filter((c: string) => ['A', 'B', 'C', 'D', 'a', 'b', 'c', 'd'].includes(c)).map((c: string) => {
            return ['A', 'B', 'C', 'D', 'a', 'b', 'c', 'd'].indexOf(c) % 4
          }).filter((i: number, idx: number, arr: number[]) => arr.indexOf(i) === idx) // 去重
        }
        correctAnswerText = answerStr
      } else if (randomType === 'fill_blank') {
        // 填空题 - 如果答案是单个字母，可能是AI格式错误，使用analysis作为备选
        if (answerStr && /^[A-Da-d]$/.test(answerStr)) {
          // AI 错误地返回了选择题答案格式，使用 analysis 作为答案
          correctAnswerText = questionData.analysis || ''
        } else {
          correctAnswerText = answerStr || questionData.analysis || ''
        }
      } else {
        // 简答题和阅读理解 - 如果答案是单个字母，可能是AI格式错误，使用analysis作为备选
        if (answerStr && /^[A-Da-d]$/.test(answerStr)) {
          // AI 错误地返回了选择题答案格式，使用 analysis 作为答案
          correctAnswerText = questionData.analysis || ''
        } else {
          correctAnswerText = answerStr || questionData.analysis || ''
        }
      }
      
      const newQuestion = {
        type: randomType,
        difficulty: aiGenerateConfig.difficulty,
        score: questionData.score || 10,
        content: questionData.content || '',
        knowledgePoints: [knowledgePoint],
        options: options,
        correctAnswer: correctAnswer,
        correctAnswers: correctAnswers,
        correctAnswerText: correctAnswerText,
        aiAnalysis: questionData.analysis || ''
      }
      
      // 如果是第一道题且当前第一题是空白题（content为空），则替换第一题
      if (i === 0 && form.questions.length > 0 && !form.questions[0].content) {
        form.questions[0] = newQuestion
      } else {
        form.questions.push(newQuestion)
      }
    }
    
    ElMessage.success(`AI 已成功生成 ${aiGenerateConfig.count} 道高中语文题目`)
    showAIGenerateDialog.value = false
  } catch (error: any) {
    ElMessage.error('AI 出题失败: ' + error.message)
  } finally {
    aiGenerating.value = false
  }
}

// 文件上传相关方法
const handleFileUpload = (file: any) => {
  uploadedFiles.value.push(file.raw)
  ElMessage.success(`已添加文件：${file.name}`)
  return false // 阻止自动上传
}

const removeUploadedFile = (idx: number) => {
  uploadedFiles.value.splice(idx, 1)
  ElMessage.success('已移除文件')
}

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

const downloadTemplate = () => {
  ElMessage.success('正在下载导入模板...')
  // 模拟下载
  setTimeout(() => {
    ElMessage.success('模板下载成功')
  }, 1000)
}

const parseUploadedFiles = async () => {
  if (uploadedFiles.value.length === 0) {
    ElMessage.warning('请先上传文件')
    return
  }
  parsingFile.value = true
  ElMessage.info('AI 正在解析文件内容...')
  
  try {
    // 实际调用后端API解析文件
    const result = await fileApi.uploadQuestionFile(uploadedFiles.value[0])
    parsedQuestions.value = result.map((q: any) => ({
      type: q.type,
      content: q.content,
      options: q.options
    }))
    ElMessage.success(`成功解析出 ${parsedQuestions.value.length} 道题目`)
  } catch (error: any) {
    ElMessage.error('解析失败: ' + error.message)
  } finally {
    parsingFile.value = false
  }
}

const applyParsedQuestions = () => {
  parsedQuestions.value.forEach(q => {
    form.questions.push({
      type: q.type,
      difficulty: 'medium',
      score: 10,
      content: q.content,
      knowledgePoints: [],
      options: q.options || ['', '', '', ''],
      correctAnswer: null,
      correctAnswers: [],
      correctAnswerText: '',
      aiAnalysis: ''
    })
  })
  parsedQuestions.value = []
  uploadedFiles.value = []
  ElMessage.success('已导入到题目列表')
}

const clearParsedQuestions = () => {
  parsedQuestions.value = []
  ElMessage.info('已清除解析结果')
}

const saveDraft = () => {
  localStorage.setItem('homework_draft', JSON.stringify(form))
  ElMessage.success('草稿已保存')
}

const publish = async () => {
  if (!form.title || !form.course || !form.deadline || form.classes.length === 0) {
    ElMessage.error('请填写完整的发布信息')
    return
  }
  
  if (form.questions.some(q => !q.content)) {
    try {
      await ElMessageBox.confirm('部分题目内容为空，确定要发布吗？', '提示', { type: 'warning' })
    } catch {
      return
    }
  }
  
  if (!userStore.user) {
    ElMessage.error('请先登录')
    return
  }
  
  if (!userStore.user.id) {
    ElMessage.error('用户ID获取失败，请重新登录')
    return
  }
  
  publishing.value = true
  try {
    const teacherId = userStore.user.id
    const classId = form.classes[0]
    
    if (!classId) {
      ElMessage.error('请选择发布班级')
      publishing.value = false
      return
    }
    
    // 创建普通作业 - 先创建作业，再添加题目
    const homework = await homeworkApi.createHomework({
      title: form.title,
      description: form.description,
      teacherId,
      classId,
      subject: form.course,
      deadline: form.deadline,
      aiEnabled: form.aiEnabled,
      aiStrictness: form.aiStrictness,
      aiDimensions: JSON.stringify(form.aiDimensions),
      allowResubmit: form.allowResubmit,
      showAnswer: form.showAnswer,
      showAnalysis: form.showAnalysis,
      status: 1
    })
    
    // 保存题目
    const validQuestions = form.questions.filter(q => q.content && q.content.trim())
    if (validQuestions.length > 0 && homework.id) {
      await questionApi.batchAddQuestions(homework.id, validQuestions.map(q => ({
        type: q.type,
        content: q.content,
        score: q.score,
        difficulty: q.difficulty,
        knowledgePoints: q.knowledgePoints,
        options: q.options,
        correctAnswer: q.correctAnswer,
        correctAnswers: q.correctAnswers,
        correctAnswerText: q.correctAnswerText,
        aiAnalysis: q.aiAnalysis
      })))
    }
    
    ElMessage.success('作业发布成功！')
    router.push('/teacher/dashboard')
  } catch (error: any) {
    ElMessage.error('发布失败: ' + error.message)
  } finally {
    publishing.value = false
  }
}
</script>
