import request from '../utils/request'

export function getTeaList(params) {
  return request.get('/teas', { params })
}

export function getTeaById(id) {
  return request.get(`/teas/${id}`)
}

export function createTea(data) {
  return request.post('/teas', data)
}

export function updateTea(id, data) {
  return request.put(`/teas/${id}`, data)
}

export function deleteTea(id) {
  return request.delete(`/teas/${id}`)
}

export function getCategories() {
  return request.get('/teas/categories')
}

export function getRegions() {
  return request.get('/teas/regions')
}

export function getBrewingParams(teaId) {
  return request.get(`/teas/${teaId}/brewing-params`)
}

export function createBrewingParam(teaId, data) {
  return request.post(`/teas/${teaId}/brewing-params`, data)
}

export function updateBrewingParam(teaId, id, data) {
  return request.put(`/teas/${teaId}/brewing-params/${id}`, data)
}

export function deleteBrewingParam(teaId, id) {
  return request.delete(`/teas/${teaId}/brewing-params/${id}`)
}

export function getBrewingTemplate(teaId, category) {
  return request.get(`/teas/${teaId}/brewing-params/template`, { params: { category } })
}

export function getBrewingTemplateByCategory(category) {
  return request.get(`/teas/brewing-template/${category}`)
}

export function getStorageRecords(teaId) {
  return request.get(`/teas/${teaId}/storage-records`)
}

export function createStorageRecord(teaId, data) {
  return request.post(`/teas/${teaId}/storage-records`, data)
}

export function updateStorageRecord(teaId, id, data) {
  return request.put(`/teas/${teaId}/storage-records/${id}`, data)
}

export function deleteStorageRecord(teaId, id) {
  return request.delete(`/teas/${teaId}/storage-records/${id}`)
}

export function getTastingNotes(teaId) {
  return request.get(`/teas/${teaId}/tasting-notes`)
}

export function createTastingNote(teaId, data) {
  return request.post(`/teas/${teaId}/tasting-notes`, data)
}

export function updateTastingNote(teaId, id, data) {
  return request.put(`/teas/${teaId}/tasting-notes/${id}`, data)
}

export function deleteTastingNote(teaId, id) {
  return request.delete(`/teas/${teaId}/tasting-notes/${id}`)
}

export function getAgingTimeline(teaId) {
  return request.get(`/teas/${teaId}/aging-timeline`)
}

export function getAgingTimelines(category) {
  return request.get('/teas/aging-timelines', { params: { category } })
}

export function getBrewingSessions(teaId) {
  return request.get(`/teas/${teaId}/brewing-sessions`)
}

export function createBrewingSession(teaId, data) {
  return request.post(`/teas/${teaId}/brewing-sessions`, data)
}

export function updateBrewingSession(teaId, id, data) {
  return request.put(`/teas/${teaId}/brewing-sessions/${id}`, data)
}

export function deleteBrewingSession(teaId, id) {
  return request.delete(`/teas/${teaId}/brewing-sessions/${id}`)
}

export function getTemplateVersions() {
  return request.get('/tea-templates/versions')
}

export function getTemplateVersion(category) {
  return request.get(`/tea-templates/${category}/version`)
}

export function evaluateSuitability(teaId, data) {
  return request.post(`/teas/${teaId}/suitability/evaluate`, data)
}

export function getSuitabilityRecords(teaId) {
  return request.get(`/teas/${teaId}/suitability`)
}

export function getLatestSuitability(teaId) {
  return request.get(`/teas/${teaId}/suitability/latest`)
}

export function deleteSuitability(teaId, id) {
  return request.delete(`/teas/${teaId}/suitability/${id}`)
}

export function getTastingVocabularies(params) {
  return request.get('/tasting-vocabularies', { params })
}

export function getTastingVocabularyById(id) {
  return request.get(`/tasting-vocabularies/${id}`)
}

export function createTastingVocabulary(data) {
  return request.post('/tasting-vocabularies', data)
}

export function updateTastingVocabulary(id, data) {
  return request.put(`/tasting-vocabularies/${id}`, data)
}

export function deleteTastingVocabulary(id) {
  return request.delete(`/tasting-vocabularies/${id}`)
}

export function getVocabulariesByTypeAndCategory(vocabularyType, teaCategory) {
  return request.get('/tasting-vocabularies', { params: { vocabularyType, teaCategory } })
}
