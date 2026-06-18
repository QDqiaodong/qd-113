export const TEA_CATEGORIES = [
  '绿茶', '红茶', '乌龙茶', '普洱茶', '白茶', '黑茶', '黄茶', '花茶'
]

export const ORIGIN_REGIONS = [
  '福建', '云南', '浙江', '安徽', '江苏', '四川', '广东',
  '台湾', '贵州', '湖南', '湖北', '江西', '河南', '广西', '山东'
]

export const STORAGE_METHODS = [
  '常温密封', '冷藏保存', '干燥避光', '真空包装', '陶罐储存', '锡罐储存'
]

export const SEAL_CONDITIONS = [
  '完全密封', '半密封', '已开封', '密封不良', '重新密封'
]

export const WATER_QUALITY_OPTIONS = [
  '纯净水', '山泉水', '矿泉水', '自来水过滤', '蒸馏水'
]

export const BREWING_METHODS = [
  '盖碗冲泡', '紫砂壶冲泡', '玻璃杯冲泡', '飘逸杯冲泡', '煮茶', '冷泡'
]

export const STOCK_UNITS = ['克', '两', '斤', '饼', '砖', '袋', '罐']

export const TEA_STORAGE_CONDITIONS = {
  '绿茶': { tempMin: 0, tempMax: 5, humidityMin: 50, humidityMax: 60 },
  '红茶': { tempMin: 20, tempMax: 25, humidityMin: 50, humidityMax: 65 },
  '乌龙茶': { tempMin: 15, tempMax: 25, humidityMin: 50, humidityMax: 65 },
  '普洱茶': { tempMin: 20, tempMax: 30, humidityMin: 60, humidityMax: 80 },
  '白茶': { tempMin: 10, tempMax: 25, humidityMin: 40, humidityMax: 60 },
  '黑茶': { tempMin: 20, tempMax: 30, humidityMin: 60, humidityMax: 80 },
  '黄茶': { tempMin: 0, tempMax: 10, humidityMin: 50, humidityMax: 60 },
  '花茶': { tempMin: 15, tempMax: 25, humidityMin: 50, humidityMax: 65 },
  '默认': { tempMin: 15, tempMax: 25, humidityMin: 50, humidityMax: 70 }
}

export const TASTING_SCORE_ANCHORS = [
  { value: 0, label: '未评分' },
  { value: 20, label: '较差' },
  { value: 40, label: '一般' },
  { value: 60, label: '良好' },
  { value: 80, label: '优秀' },
  { value: 100, label: '极佳' }
]

export const TASTING_SCORE_CATEGORIES = [
  { key: 'aroma', label: '香气', desc: '如：花香馥郁、蜜香高扬' },
  { key: 'liquorColor', label: '汤色', desc: '如：金黄透亮、橙红明亮' },
  { key: 'taste', label: '滋味', desc: '如：醇厚甘甜、鲜爽回甘' },
  { key: 'aftertaste', label: '回甘', desc: '如：回甘持久、生津明显' }
]

export function getScoreLabel(score) {
  if (score == null) return '未评分'
  const s = Number(score)
  if (s >= 90) return '极佳'
  if (s >= 75) return '优秀'
  if (s >= 60) return '良好'
  if (s >= 40) return '一般'
  if (s >= 20) return '较差'
  return '待评'
}

export function getScoreColor(score) {
  if (score == null) return '#909399'
  const s = Number(score)
  if (s >= 90) return '#67c23a'
  if (s >= 75) return '#409eff'
  if (s >= 60) return '#e6a23c'
  if (s >= 40) return '#f56c6c'
  return '#909399'
}

export const TEA_RECOMMENDED_STORAGE = {
  '绿茶': ['冷藏保存', '锡罐储存', '真空包装'],
  '红茶': ['常温密封', '锡罐储存', '陶罐储存'],
  '乌龙茶': ['常温密封', '真空包装', '锡罐储存'],
  '普洱茶': ['陶罐储存', '干燥避光', '常温密封'],
  '白茶': ['陶罐储存', '干燥避光', '常温密封'],
  '黑茶': ['陶罐储存', '干燥避光', '常温密封'],
  '黄茶': ['冷藏保存', '锡罐储存', '真空包装'],
  '花茶': ['常温密封', '锡罐储存', '干燥避光'],
  '默认': ['常温密封', '干燥避光']
}

export const STORAGE_METHOD_GUIDELINES = {
  '常温密封': {
    tempSuggestion: { min: 15, max: 25, label: '建议温度 15-25℃' },
    humiditySuggestion: { min: 50, max: 70, label: '建议湿度 50-70%' },
    sealSuggestion: ['完全密封', '重新密封'],
    sealTip: '常温储存需确保完全密封，防止受潮和香气散失',
    tips: ['放置于阴凉干燥处', '避免阳光直射', '远离异味源', '定期检查密封状况']
  },
  '冷藏保存': {
    tempSuggestion: { min: 0, max: 5, label: '建议温度 0-5℃' },
    humiditySuggestion: { min: 50, max: 60, label: '建议湿度 50-60%' },
    sealSuggestion: ['完全密封', '真空包装'],
    sealTip: '冷藏前必须完全密封，防止冷凝水浸湿茶叶',
    tips: ['使用密封袋或密封罐', '取出前先回温至室温再开封', '避免频繁进出冷藏室', '与其他食物隔离存放']
  },
  '干燥避光': {
    tempSuggestion: { min: 15, max: 25, label: '建议温度 15-25℃' },
    humiditySuggestion: { min: 40, max: 60, label: '建议湿度 40-60%' },
    sealSuggestion: ['完全密封', '半密封'],
    sealTip: '干燥环境下可适当透气，但仍需避免强光',
    tips: ['使用不透明容器', '放置在避光柜子中', '可放置干燥剂', '避免放置在窗边']
  },
  '真空包装': {
    tempSuggestion: { min: 10, max: 25, label: '建议温度 10-25℃' },
    humiditySuggestion: { min: 40, max: 70, label: '建议湿度 40-70%' },
    sealSuggestion: ['完全密封'],
    sealTip: '真空包装已处于最佳密封状态，注意检查包装是否完好',
    tips: ['检查真空袋有无漏气', '避免尖锐物品刺破包装', '长期存放可定期抽真空', '开封后建议更换储存方式']
  },
  '陶罐储存': {
    tempSuggestion: { min: 15, max: 28, label: '建议温度 15-28℃' },
    humiditySuggestion: { min: 50, max: 75, label: '建议湿度 50-75%' },
    sealSuggestion: ['半密封', '完全密封'],
    sealTip: '陶罐透气性好，适合需要陈化的茶类，可根据需要调整密封程度',
    tips: ['适合普洱茶、白茶等需要陈化的茶叶', '新陶罐需先去火气再使用', '定期检查罐内有无异味', '罐口可垫棉纸增强过滤效果']
  },
  '锡罐储存': {
    tempSuggestion: { min: 15, max: 25, label: '建议温度 15-25℃' },
    humiditySuggestion: { min: 40, max: 65, label: '建议湿度 40-65%' },
    sealSuggestion: ['完全密封', '重新密封'],
    sealTip: '锡罐密封性能优良，是茶叶储存的上等容器',
    tips: ['锡罐避光防潮性极佳', '适合存放高级绿茶、红茶', '盖紧罐盖确保密封', '避免放置在潮湿环境']
  }
}
