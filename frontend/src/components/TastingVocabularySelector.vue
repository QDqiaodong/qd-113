<template>
  <div class="vocabulary-selector">
    <div class="selector-header">
      <span class="selector-label">推荐词汇</span>
      <el-tag size="small" type="info" effect="plain">{{ teaCategory }} · {{ typeLabel }}</el-tag>
    </div>
    <div class="vocabulary-tags" v-loading="loading">
      <el-tag
        v-for="item in vocabularies"
        :key="item.id"
        :type="isSelected(item.word) ? 'primary' : 'info'"
        :effect="isSelected(item.word) ? 'dark' : 'plain'"
        class="vocab-tag"
        :class="{ 'selected': isSelected(item.word) }"
        @click="toggleVocabulary(item.word)"
        :title="item.description"
        size="large"
      >
        {{ item.word }}
      </el-tag>
      <el-empty v-if="!loading && vocabularies.length === 0" description="暂无推荐词汇" :image-size="60" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { getVocabulariesByTypeAndCategory } from '../api/tea'
import { TASTING_SCORE_CATEGORIES } from '../utils/constants'

const props = defineProps({
  vocabularyType: {
    type: String,
    required: true
  },
  teaCategory: {
    type: String,
    required: true
  },
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const loading = ref(false)
const vocabularies = ref([])
const selectedWords = ref([])

const vocabularyTypeMap = {
  'aroma': 'aroma',
  'liquorColor': 'liquor_color',
  'taste': 'taste',
  'aftertaste': 'aftertaste'
}

const backendVocabularyType = computed(() => {
  return vocabularyTypeMap[props.vocabularyType] || props.vocabularyType
})

const typeLabel = computed(() => {
  const cat = TASTING_SCORE_CATEGORIES.find(c => c.key === props.vocabularyType)
  return cat ? cat.label : props.vocabularyType
})

function isSelected(word) {
  return selectedWords.value.includes(word)
}

function toggleVocabulary(word) {
  const idx = selectedWords.value.indexOf(word)
  if (idx > -1) {
    selectedWords.value.splice(idx, 1)
  } else {
    selectedWords.value.push(word)
  }
  updateDescription()
}

function updateDescription() {
  const desc = selectedWords.value.join('、')
  emit('update:modelValue', desc)
}

function parseSelectedWords() {
  if (!props.modelValue) {
    selectedWords.value = []
    return
  }
  selectedWords.value = props.modelValue.split(/[、,，\s]+/).filter(w => w.trim())
}

async function loadVocabularies() {
  if (!backendVocabularyType.value || !props.teaCategory) {
    vocabularies.value = []
    return
  }
  loading.value = true
  try {
    const res = await getVocabulariesByTypeAndCategory(backendVocabularyType.value, props.teaCategory)
    vocabularies.value = res.data || []
  } catch (e) {
    console.error('Failed to load vocabularies:', e)
    vocabularies.value = []
  } finally {
    loading.value = false
  }
}

watch(
  () => [backendVocabularyType.value, props.teaCategory],
  () => {
    loadVocabularies()
  }
)

watch(
  () => props.modelValue,
  (newVal) => {
    const currentDesc = selectedWords.value.join('、')
    if (newVal !== currentDesc) {
      parseSelectedWords()
    }
  }
)

onMounted(() => {
  parseSelectedWords()
  loadVocabularies()
})
</script>

<style scoped>
.vocabulary-selector {
  margin-top: 8px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.selector-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.selector-label {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.vocabulary-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 40px;
}

.vocab-tag {
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.vocab-tag:hover {
  transform: translateY(-1px);
}

.vocab-tag.selected {
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}
</style>
