/**
 * 跨平台文件操作统一 API
 * 封装 H5 和小程序的文件选择、上传、下载差异
 */

import { getUploadSignature } from '@/services/resource'
import type { OSSSignature } from './upload'

/**
 * 文件信息统一接口
 */
export interface FileInfo {
  /** 文件路径（H5: blob URL, 小程序: tempFilePath） */
  path: string
  /** 文件名 */
  name: string
  /** 文件大小（字节） */
  size: number
  /** 文件类型（MIME type）*/
  type?: string
  /** 原始文件对象（仅 H5） */
  raw?: File
}

/**
 * 文件选择选项
 */
export interface ChooseFileOptions {
  /** 允许选择的文件类型（扩展名数组，如 ['pdf', 'doc', 'docx']） */
  extensions?: string[]
  /** 最大文件大小（字节）*/
  maxSize?: number
  /** 是否允许多选（默认 false）*/
  multiple?: boolean
  /** 最多选择数量（仅多选时有效，默认 9）*/
  count?: number
}

/**
 * 文件上传选项
 */
export interface UploadFileOptions {
  /** 文件信息 */
  file: FileInfo
  /** 进度回调 */
  onProgress?: (progress: number) => void
  /** 成功回调 */
  onSuccess?: (url: string) => void
  /** 失败回调 */
  onError?: (error: Error) => void
}

/**
 * 文件下载选项
 */
export interface DownloadFileOptions {
  /** 下载 URL */
  url: string
  /** 文件名 */
  filename: string
  /** 进度回调 */
  onProgress?: (progress: number) => void
  /** 成功回调 */
  onSuccess?: () => void
  /** 失败回调 */
  onError?: (error: Error) => void
}

/**
 * 🎯 统一文件选择方法
 *
 * 用法:
 * ```typescript
 * const files = await chooseFile({
 *   extensions: ['pdf', 'doc', 'docx'],
 *   maxSize: 50 * 1024 * 1024 // 50MB
 * })
 * ```
 */
export const chooseFile = async (
  options: ChooseFileOptions = {}
): Promise<FileInfo[]> => {
  const {
    extensions = [],
    maxSize = 50 * 1024 * 1024, // 默认 50MB
    multiple = false,
    count = 9
  } = options

  return new Promise((resolve, reject) => {
    // #ifdef H5
    const input = document.createElement('input')
    input.type = 'file'
    input.multiple = multiple

    // 设置允许的文件类型
    if (extensions.length > 0) {
      input.accept = extensions.map(ext => `.${ext}`).join(',')
    }

    input.onchange = (e: any) => {
      const selectedFiles = Array.from(e.target.files as FileList)

      if (selectedFiles.length === 0) {
        reject(new Error('未选择文件'))
        return
      }

      // 验证文件
      const invalidFiles: string[] = []
      const validFiles: FileInfo[] = []

      for (const file of selectedFiles) {
        // 验证大小
        if (file.size > maxSize) {
          invalidFiles.push(`${file.name}（超过 ${formatFileSize(maxSize)}）`)
          continue
        }

        // 验证格式
        if (extensions.length > 0) {
          const ext = getFileExtension(file.name)
          if (!extensions.includes(ext)) {
            invalidFiles.push(`${file.name}（格式不支持）`)
            continue
          }
        }

        validFiles.push({
          path: URL.createObjectURL(file),
          name: file.name,
          size: file.size,
          type: file.type,
          raw: file
        })
      }

      if (invalidFiles.length > 0) {
        reject(new Error(`以下文件不符合要求：\n${invalidFiles.join('\n')}`))
        return
      }

      if (validFiles.length === 0) {
        reject(new Error('没有有效的文件'))
        return
      }

      resolve(validFiles)
    }

    input.click()
    // #endif
  })
}

/**
 * 🎯 统一文件上传方法（上传到 OSS）
 *
 * 用法:
 * ```typescript
 * const url = await uploadFile({
 *   file: fileInfo,
 *   onProgress: (progress) => console.log(progress)
 * })
 * ```
 */
export const uploadFile = async (
  options: UploadFileOptions
): Promise<string> => {
  const { file, onProgress, onSuccess, onError } = options

  try {
    // 1. 获取 OSS 签名
    const signature = await getUploadSignature(file.name)

    // 2. 上传到 OSS
    const fileUrl = await uploadToOSS(file, signature, onProgress)

    // 3. 成功回调
    if (onSuccess) {
      onSuccess(fileUrl)
    }

    return fileUrl
  } catch (error: any) {
    // 失败回调
    if (onError) {
      onError(error)
    }
    throw error
  }
}

/**
 * 🎯 内部方法：上传到 OSS
 */
const uploadToOSS = async (
  file: FileInfo,
  signature: OSSSignature,
  onProgress?: (progress: number) => void
): Promise<string> => {
  return new Promise((resolve, reject) => {
    // #ifdef H5
    // H5 使用 XMLHttpRequest
    const formData = new FormData()
    formData.append('key', signature.key)
    formData.append('policy', signature.policy)
    formData.append('OSSAccessKeyId', signature.accessId)
    formData.append('signature', signature.signature)
    formData.append('success_action_status', '200')
    formData.append('file', file.raw!) // H5 有 raw 文件对象

    const xhr = new XMLHttpRequest()

    // 进度监听
    if (onProgress) {
      xhr.upload.onprogress = (e) => {
        if (e.lengthComputable) {
          const progress = Math.round((e.loaded / e.total) * 100)
          onProgress(progress)
        }
      }
    }

    xhr.onload = () => {
      if (xhr.status === 200 || xhr.status === 204) {
        const fileUrl = `${signature.host}/${signature.key}`
        resolve(fileUrl)
      } else {
        reject(new Error('上传失败'))
      }
    }

    xhr.onerror = () => reject(new Error('网络错误'))

    xhr.open('POST', signature.host)
    xhr.send(formData)
    // #endif
  })
}

/**
 * 🎯 统一文件下载方法
 *
 * 用法:
 * ```typescript
 * await downloadFile({
 *   url: 'https://example.com/file.pdf',
 *   filename: 'example.pdf',
 *   onProgress: (progress) => console.log(progress)
 * })
 * ```
 */
export const downloadFile = async (
  options: DownloadFileOptions
): Promise<void> => {
  const { url, filename, onProgress, onSuccess, onError } = options

  try {
    // #ifdef H5
    // H5 使用 <a> 标签下载
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.click()

    // H5 无法获取下载进度，直接返回成功
    if (onSuccess) {
      onSuccess()
    }
    // #endif
  } catch (error: any) {
    if (onError) {
      onError(error)
    } else {
      throw error
    }
  }
}

/**
 * 🎯 批量上传文件
 *
 * 用法:
 * ```typescript
 * const urls = await uploadFiles(files, {
 *   onProgress: (current, total) => console.log(`${current}/${total}`)
 * })
 * ```
 */
export const uploadFiles = async (
  files: FileInfo[],
  options: {
    onProgress?: (current: number, total: number, currentProgress: number) => void
    onError?: (file: FileInfo, error: Error) => void
  } = {}
): Promise<string[]> => {
  const { onProgress, onError } = options
  const urls: string[] = []
  const total = files.length

  for (let i = 0; i < files.length; i++) {
    const file = files[i]

    try {
      const url = await uploadFile({
        file,
        onProgress: (progress) => {
          if (onProgress) {
            onProgress(i + 1, total, progress)
          }
        }
      })

      urls.push(url)
    } catch (error: any) {
      if (onError) {
        onError(file, error)
      } else {
        throw error
      }
    }
  }

  return urls
}

/**
 * 🎯 工具方法：获取文件扩展名
 */
export const getFileExtension = (filename: string): string => {
  return filename.slice(((filename.lastIndexOf('.') - 1) >>> 0) + 2).toLowerCase()
}

/**
 * 🎯 工具方法：格式化文件大小
 */
export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

/**
 * 🎯 工具方法：验证文件类型
 */
export const validateFileType = (
  filename: string,
  allowedExtensions: string[]
): boolean => {
  const ext = getFileExtension(filename)
  return allowedExtensions.includes(ext)
}

/**
 * 🎯 工具方法：验证文件大小
 */
export const validateFileSize = (
  fileSize: number,
  maxSize: number
): boolean => {
  return fileSize <= maxSize
}
