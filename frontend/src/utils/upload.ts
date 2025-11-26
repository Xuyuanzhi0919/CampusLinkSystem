/**
 * 文件上传工具函数
 */

/**
 * OSS上传签名响应
 */
export interface OSSSignature {
  accessId: string  // 修正：与后端字段名一致
  policy: string
  signature: string
  dir: string
  host: string
  expire: number
  key: string  // 后端生成的唯一文件路径
}

/**
 * 获取Token
 */
function getToken(): string {
  return uni.getStorageSync('campuslink_token') || ''
}

/**
 * 获取OSS上传签名
 * @param fileName 文件名（用于提取扩展名）
 * @returns OSS签名信息
 */
export async function getOSSSignature(fileName: string): Promise<OSSSignature> {
  return new Promise((resolve, reject) => {
    const token = getToken()

    uni.request({
      url: '/api/v1/resource/upload/signature',
      method: 'GET',
      data: {
        fileName: fileName
      },
      header: {
        'Authorization': token ? `Bearer ${token}` : '',
        'Content-Type': 'application/json'
      },
      success: (res: any) => {
        console.log('OSS签名响应:', res)

        if (res.statusCode === 200 && res.data.code === 200) {
          console.log('签名数据:', res.data.data)
          resolve(res.data.data)
        } else {
          console.error('签名失败:', res.data)
          reject(new Error(res.data.message || '获取签名失败'))
        }
      },
      fail: (err) => {
        console.error('请求失败:', err)
        reject(err)
      }
    })
  })
}

/**
 * 上传文件到OSS
 * @param filePath 本地文件路径
 * @param signature OSS签名信息
 * @returns OSS文件URL
 */
export async function uploadToOSS(filePath: string, signature: OSSSignature): Promise<string> {
  return new Promise((resolve, reject) => {
    // 使用后端返回的key（已包含完整路径和唯一文件名）
    const key = signature.key

    uni.uploadFile({
      url: signature.host,
      filePath: filePath,
      name: 'file',
      formData: {
        key: key,
        policy: signature.policy,
        OSSAccessKeyId: signature.accessId,  // 修正：使用accessId
        signature: signature.signature,
        success_action_status: '200'
      },
      success: (res) => {
        if (res.statusCode === 200) {
          // 返回文件完整URL
          const fileUrl = `${signature.host}/${key}`
          resolve(fileUrl)
        } else {
          reject(new Error('上传失败'))
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

/**
 * 批量上传图片到OSS
 * @param filePaths 本地文件路径数组
 * @param onProgress 进度回调
 * @returns OSS文件URL数组
 */
export async function uploadImagesToOSS(
  filePaths: string[],
  onProgress?: (current: number, total: number) => void
): Promise<string[]> {
  try {
    // 上传所有图片（每个文件单独获取签名）
    const uploadPromises = filePaths.map(async (filePath, index) => {
      // 提取文件名
      const fileName = filePath.substring(filePath.lastIndexOf('/') + 1)

      // 为每个文件获取签名
      const signature = await getOSSSignature(fileName)

      // 上传文件
      const url = await uploadToOSS(filePath, signature)

      if (onProgress) {
        onProgress(index + 1, filePaths.length)
      }

      return url
    })

    return await Promise.all(uploadPromises)
  } catch (error) {
    throw error
  }
}

/**
 * 选择并上传图片
 * @param options 选择选项
 * @returns 上传后的URL数组
 */
export async function chooseAndUploadImages(options: {
  count: number
  sizeType?: ('original' | 'compressed')[]
  sourceType?: ('album' | 'camera')[]
}): Promise<string[]> {
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count: options.count,
      sizeType: options.sizeType || ['compressed'],
      sourceType: options.sourceType || ['album', 'camera'],
      success: async (res) => {
        try {
          // 显示上传进度
          uni.showLoading({
            title: '上传中...',
            mask: true
          })

          const filePaths = Array.isArray(res.tempFilePaths) ? res.tempFilePaths : [res.tempFilePaths]
          const urls = await uploadImagesToOSS(filePaths, (current, total) => {
            uni.showLoading({
              title: `上传中 ${current}/${total}`,
              mask: true
            })
          })

          uni.hideLoading()
          resolve(urls)
        } catch (error: any) {
          uni.hideLoading()
          uni.showToast({
            title: error.message || '上传失败',
            icon: 'none'
          })
          reject(error)
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}
