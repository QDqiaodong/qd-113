import { reactive, computed } from 'vue'

const state = reactive({
  currentCurveData: null,
  selectedParamId: null
})

function generateCurvePoints(brewingParam) {
  if (!brewingParam) return []
  
  const totalInfusions = brewingParam.totalInfusions || 4
  const points = []
  
  for (let i = 1; i <= totalInfusions; i++) {
    let infusionTime
    if (i === 1) {
      infusionTime = brewingParam.firstInfusionTime
    } else if (i === 2) {
      infusionTime = brewingParam.secondInfusionTime
    } else if (i === 3) {
      infusionTime = brewingParam.thirdInfusionTime
    } else {
      infusionTime = brewingParam.subsequentInfusionTime
    }
    
    points.push({
      brewNumber: i,
      waterTemperature: brewingParam.waterTemperature,
      teaAmount: brewingParam.teaAmount,
      waterAmount: brewingParam.waterAmount,
      infusionTime: infusionTime
    })
  }
  
  return points
}

function setCurveData(brewingParam) {
  state.currentCurveData = {
    ...brewingParam,
    curvePoints: generateCurvePoints(brewingParam)
  }
  state.selectedParamId = brewingParam?.id || null
}

function updateCurvePoint(index, field, value) {
  if (!state.currentCurveData?.curvePoints) return
  
  const point = state.currentCurveData.curvePoints[index]
  if (point) {
    point[field] = value
  }
}

function addBrewPoint() {
  if (!state.currentCurveData?.curvePoints) return
  
  const points = state.currentCurveData.curvePoints
  const lastPoint = points[points.length - 1]
  const newBrewNumber = points.length + 1
  
  points.push({
    brewNumber: newBrewNumber,
    waterTemperature: lastPoint?.waterTemperature || 90,
    teaAmount: lastPoint?.teaAmount || 5,
    waterAmount: lastPoint?.waterAmount || 150,
    infusionTime: lastPoint?.infusionTime ? lastPoint.infusionTime + 5 : 30
  })
  
  state.currentCurveData.totalInfusions = points.length
}

function removeBrewPoint(index) {
  if (!state.currentCurveData?.curvePoints) return
  if (state.currentCurveData.curvePoints.length <= 1) return
  
  state.currentCurveData.curvePoints.splice(index, 1)
  
  state.currentCurveData.curvePoints.forEach((point, i) => {
    point.brewNumber = i + 1
  })
  
  state.currentCurveData.totalInfusions = state.currentCurveData.curvePoints.length
}

function clearCurveData() {
  state.currentCurveData = null
  state.selectedParamId = null
}

function getBrewingParamFromCurve() {
  if (!state.currentCurveData?.curvePoints) return null
  
  const points = state.currentCurveData.curvePoints
  
  return {
    ...state.currentCurveData,
    firstInfusionTime: points[0]?.infusionTime,
    secondInfusionTime: points[1]?.infusionTime,
    thirdInfusionTime: points[2]?.infusionTime,
    subsequentInfusionTime: points[3]?.infusionTime,
    totalInfusions: points.length,
    waterTemperature: points[0]?.waterTemperature,
    teaAmount: points[0]?.teaAmount,
    waterAmount: points[0]?.waterAmount
  }
}

const hasCurveData = computed(() => !!state.currentCurveData?.curvePoints?.length)

export function useBrewingCurveStore() {
  return {
    state,
    hasCurveData,
    setCurveData,
    updateCurvePoint,
    addBrewPoint,
    removeBrewPoint,
    clearCurveData,
    getBrewingParamFromCurve,
    generateCurvePoints
  }
}
