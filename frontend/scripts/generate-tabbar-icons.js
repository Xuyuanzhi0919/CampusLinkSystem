/**
 * 生成 TabBar PNG 图标
 * 微信小程序要求 PNG 格式，建议尺寸 81x81px
 *
 * 这个脚本生成简洁的线条风格图标
 */

const fs = require('fs')
const path = require('path')

const iconDir = path.join(__dirname, '../src/static/tabbar')

if (!fs.existsSync(iconDir)) {
  fs.mkdirSync(iconDir, { recursive: true })
}

// 图标配置
const ICON_SIZE = 81
const GRAY_COLOR = '#999999'
const ACTIVE_COLOR = '#2563EB'

/**
 * 创建简单的 PNG 文件
 * 使用纯 Node.js 实现，无需依赖 canvas 库
 */
function createPNG(width, height, drawFunc) {
  // PNG 签名
  const signature = Buffer.from([0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A])

  // 创建像素数据 (RGBA)
  const pixels = Buffer.alloc(width * height * 4, 0)

  // 调用绘制函数填充像素
  drawFunc(pixels, width, height)

  // 创建 IHDR chunk
  const ihdr = createIHDR(width, height)

  // 创建 IDAT chunk (原始像素数据，使用无压缩)
  const idat = createIDAT(pixels, width, height)

  // 创建 IEND chunk
  const iend = createIEND()

  return Buffer.concat([signature, ihdr, idat, iend])
}

function createIHDR(width, height) {
  const data = Buffer.alloc(13)
  data.writeUInt32BE(width, 0)
  data.writeUInt32BE(height, 4)
  data[8] = 8  // bit depth
  data[9] = 6  // color type: RGBA
  data[10] = 0 // compression
  data[11] = 0 // filter
  data[12] = 0 // interlace

  return createChunk('IHDR', data)
}

function createIDAT(pixels, width, height) {
  const zlib = require('zlib')

  // 添加 filter byte (0 = None) 到每行开头
  const filtered = Buffer.alloc(height * (1 + width * 4))
  for (let y = 0; y < height; y++) {
    filtered[y * (1 + width * 4)] = 0 // filter type: None
    pixels.copy(
      filtered,
      y * (1 + width * 4) + 1,
      y * width * 4,
      (y + 1) * width * 4
    )
  }

  // 使用 zlib 压缩
  const compressed = zlib.deflateSync(filtered, { level: 9 })

  return createChunk('IDAT', compressed)
}

function createIEND() {
  return createChunk('IEND', Buffer.alloc(0))
}

function createChunk(type, data) {
  const length = Buffer.alloc(4)
  length.writeUInt32BE(data.length, 0)

  const typeBuffer = Buffer.from(type, 'ascii')
  const crcData = Buffer.concat([typeBuffer, data])
  const crc = crc32(crcData)

  const crcBuffer = Buffer.alloc(4)
  crcBuffer.writeUInt32BE(crc >>> 0, 0)

  return Buffer.concat([length, typeBuffer, data, crcBuffer])
}

// CRC32 实现
function crc32(buffer) {
  let crc = 0xFFFFFFFF
  const table = getCRC32Table()

  for (let i = 0; i < buffer.length; i++) {
    crc = table[(crc ^ buffer[i]) & 0xFF] ^ (crc >>> 8)
  }

  return crc ^ 0xFFFFFFFF
}

let crc32Table = null
function getCRC32Table() {
  if (crc32Table) return crc32Table

  crc32Table = new Uint32Array(256)
  for (let i = 0; i < 256; i++) {
    let c = i
    for (let j = 0; j < 8; j++) {
      c = (c & 1) ? (0xEDB88320 ^ (c >>> 1)) : (c >>> 1)
    }
    crc32Table[i] = c
  }
  return crc32Table
}

// 解析颜色
function parseColor(color) {
  const hex = color.replace('#', '')
  return {
    r: parseInt(hex.substr(0, 2), 16),
    g: parseInt(hex.substr(2, 2), 16),
    b: parseInt(hex.substr(4, 2), 16)
  }
}

// 设置像素
function setPixel(pixels, width, x, y, r, g, b, a = 255) {
  if (x < 0 || x >= width || y < 0 || y >= width) return
  const i = (y * width + x) * 4
  pixels[i] = r
  pixels[i + 1] = g
  pixels[i + 2] = b
  pixels[i + 3] = a
}

// 绘制粗线条（抗锯齿）
function drawLine(pixels, width, x1, y1, x2, y2, color, thickness = 3) {
  const { r, g, b } = parseColor(color)
  const dx = Math.abs(x2 - x1)
  const dy = Math.abs(y2 - y1)
  const sx = x1 < x2 ? 1 : -1
  const sy = y1 < y2 ? 1 : -1
  let err = dx - dy

  let x = x1, y = y1
  while (true) {
    // 绘制粗点
    for (let tx = -Math.floor(thickness/2); tx <= Math.floor(thickness/2); tx++) {
      for (let ty = -Math.floor(thickness/2); ty <= Math.floor(thickness/2); ty++) {
        if (tx*tx + ty*ty <= (thickness/2)*(thickness/2)) {
          setPixel(pixels, width, Math.round(x + tx), Math.round(y + ty), r, g, b)
        }
      }
    }

    if (x === x2 && y === y2) break
    const e2 = 2 * err
    if (e2 > -dy) { err -= dy; x += sx }
    if (e2 < dx) { err += dx; y += sy }
  }
}

// 绘制圆形
function drawCircle(pixels, width, cx, cy, radius, color, thickness = 3) {
  const { r, g, b } = parseColor(color)

  for (let angle = 0; angle < 360; angle += 0.5) {
    const rad = angle * Math.PI / 180
    const x = cx + radius * Math.cos(rad)
    const y = cy + radius * Math.sin(rad)

    for (let tx = -Math.floor(thickness/2); tx <= Math.floor(thickness/2); tx++) {
      for (let ty = -Math.floor(thickness/2); ty <= Math.floor(thickness/2); ty++) {
        if (tx*tx + ty*ty <= (thickness/2)*(thickness/2)) {
          setPixel(pixels, width, Math.round(x + tx), Math.round(y + ty), r, g, b)
        }
      }
    }
  }
}

// 绘制填充圆形
function fillCircle(pixels, width, cx, cy, radius, color) {
  const { r, g, b } = parseColor(color)

  for (let y = cy - radius; y <= cy + radius; y++) {
    for (let x = cx - radius; x <= cx + radius; x++) {
      if ((x - cx) * (x - cx) + (y - cy) * (y - cy) <= radius * radius) {
        setPixel(pixels, width, Math.round(x), Math.round(y), r, g, b)
      }
    }
  }
}

// 绘制圆角矩形
function drawRoundRect(pixels, width, x, y, w, h, radius, color, thickness = 3) {
  // 四条边
  drawLine(pixels, width, x + radius, y, x + w - radius, y, color, thickness)
  drawLine(pixels, width, x + radius, y + h, x + w - radius, y + h, color, thickness)
  drawLine(pixels, width, x, y + radius, x, y + h - radius, color, thickness)
  drawLine(pixels, width, x + w, y + radius, x + w, y + h - radius, color, thickness)

  // 四个角（弧线）
  const { r: cr, g: cg, b: cb } = parseColor(color)
  for (let angle = 180; angle <= 270; angle += 0.5) {
    const rad = angle * Math.PI / 180
    const px = x + radius + radius * Math.cos(rad)
    const py = y + radius + radius * Math.sin(rad)
    for (let tx = -Math.floor(thickness/2); tx <= Math.floor(thickness/2); tx++) {
      for (let ty = -Math.floor(thickness/2); ty <= Math.floor(thickness/2); ty++) {
        if (tx*tx + ty*ty <= (thickness/2)*(thickness/2)) {
          setPixel(pixels, width, Math.round(px + tx), Math.round(py + ty), cr, cg, cb)
        }
      }
    }
  }
  for (let angle = 270; angle <= 360; angle += 0.5) {
    const rad = angle * Math.PI / 180
    const px = x + w - radius + radius * Math.cos(rad)
    const py = y + radius + radius * Math.sin(rad)
    for (let tx = -Math.floor(thickness/2); tx <= Math.floor(thickness/2); tx++) {
      for (let ty = -Math.floor(thickness/2); ty <= Math.floor(thickness/2); ty++) {
        if (tx*tx + ty*ty <= (thickness/2)*(thickness/2)) {
          setPixel(pixels, width, Math.round(px + tx), Math.round(py + ty), cr, cg, cb)
        }
      }
    }
  }
  for (let angle = 0; angle <= 90; angle += 0.5) {
    const rad = angle * Math.PI / 180
    const px = x + w - radius + radius * Math.cos(rad)
    const py = y + h - radius + radius * Math.sin(rad)
    for (let tx = -Math.floor(thickness/2); tx <= Math.floor(thickness/2); tx++) {
      for (let ty = -Math.floor(thickness/2); ty <= Math.floor(thickness/2); ty++) {
        if (tx*tx + ty*ty <= (thickness/2)*(thickness/2)) {
          setPixel(pixels, width, Math.round(px + tx), Math.round(py + ty), cr, cg, cb)
        }
      }
    }
  }
  for (let angle = 90; angle <= 180; angle += 0.5) {
    const rad = angle * Math.PI / 180
    const px = x + radius + radius * Math.cos(rad)
    const py = y + h - radius + radius * Math.sin(rad)
    for (let tx = -Math.floor(thickness/2); tx <= Math.floor(thickness/2); tx++) {
      for (let ty = -Math.floor(thickness/2); ty <= Math.floor(thickness/2); ty++) {
        if (tx*tx + ty*ty <= (thickness/2)*(thickness/2)) {
          setPixel(pixels, width, Math.round(px + tx), Math.round(py + ty), cr, cg, cb)
        }
      }
    }
  }
}

// 首页图标 - 简洁房子
function drawHomeIcon(color) {
  return createPNG(ICON_SIZE, ICON_SIZE, (pixels, width, height) => {
    const cx = width / 2
    const cy = height / 2
    const size = width * 0.35

    // 屋顶 (三角形)
    const roofTop = cy - size * 0.9
    const roofBottom = cy - size * 0.1
    const roofLeft = cx - size
    const roofRight = cx + size

    drawLine(pixels, width, cx, roofTop, roofLeft, roofBottom, color, 4)
    drawLine(pixels, width, cx, roofTop, roofRight, roofBottom, color, 4)

    // 房子主体
    const bodyTop = roofBottom
    const bodyBottom = cy + size * 0.9
    const bodyLeft = cx - size * 0.75
    const bodyRight = cx + size * 0.75

    drawLine(pixels, width, bodyLeft, bodyTop, bodyLeft, bodyBottom, color, 4)
    drawLine(pixels, width, bodyRight, bodyTop, bodyRight, bodyBottom, color, 4)
    drawLine(pixels, width, bodyLeft, bodyBottom, bodyRight, bodyBottom, color, 4)

    // 门
    const doorWidth = size * 0.4
    const doorHeight = size * 0.6
    const doorLeft = cx - doorWidth / 2
    const doorRight = cx + doorWidth / 2
    const doorTop = bodyBottom - doorHeight

    drawLine(pixels, width, doorLeft, doorTop, doorLeft, bodyBottom, color, 3)
    drawLine(pixels, width, doorRight, doorTop, doorRight, bodyBottom, color, 3)
    drawLine(pixels, width, doorLeft, doorTop, doorRight, doorTop, color, 3)
  })
}

// 资源图标 - 文件夹
function drawResourceIcon(color) {
  return createPNG(ICON_SIZE, ICON_SIZE, (pixels, width, height) => {
    const cx = width / 2
    const cy = height / 2
    const w = width * 0.6
    const h = height * 0.45

    const left = cx - w / 2
    const right = cx + w / 2
    const top = cy - h / 2
    const bottom = cy + h / 2

    // 文件夹主体
    drawLine(pixels, width, left, top + h * 0.2, left, bottom, color, 4)
    drawLine(pixels, width, right, top + h * 0.2, right, bottom, color, 4)
    drawLine(pixels, width, left, bottom, right, bottom, color, 4)
    drawLine(pixels, width, left, top + h * 0.2, left + w * 0.35, top + h * 0.2, color, 4)
    drawLine(pixels, width, left + w * 0.35, top + h * 0.2, left + w * 0.45, top, color, 4)
    drawLine(pixels, width, left + w * 0.45, top, right, top, color, 4)
    drawLine(pixels, width, right, top, right, top + h * 0.2, color, 4)

    // 折叠线
    drawLine(pixels, width, left, top + h * 0.35, right, top + h * 0.35, color, 3)
  })
}

// 问答图标 - 对话气泡
function drawQuestionIcon(color) {
  return createPNG(ICON_SIZE, ICON_SIZE, (pixels, width, height) => {
    const cx = width / 2
    const cy = height / 2
    const size = width * 0.35

    // 主气泡
    drawCircle(pixels, width, cx, cy - size * 0.15, size, color, 4)

    // 气泡尾巴
    const tailX = cx - size * 0.3
    const tailY = cy + size * 0.6
    drawLine(pixels, width, cx - size * 0.5, cy + size * 0.3, tailX, tailY, color, 4)
    drawLine(pixels, width, tailX, tailY, cx - size * 0.1, cy + size * 0.4, color, 4)

    // 问号
    const qcx = cx
    const qcy = cy - size * 0.2
    const qsize = size * 0.3

    // 问号上半部分弧线
    for (let angle = 180; angle <= 360 + 45; angle += 1) {
      const rad = angle * Math.PI / 180
      const px = qcx + qsize * Math.cos(rad)
      const py = qcy - qsize * 0.3 + qsize * 0.7 * Math.sin(rad)
      setPixel(pixels, width, Math.round(px), Math.round(py), ...Object.values(parseColor(color)))
      setPixel(pixels, width, Math.round(px + 1), Math.round(py), ...Object.values(parseColor(color)))
      setPixel(pixels, width, Math.round(px), Math.round(py + 1), ...Object.values(parseColor(color)))
    }

    // 问号点
    fillCircle(pixels, width, qcx, qcy + qsize * 0.9, 3, color)
  })
}

// 用户图标 - 人形
function drawUserIcon(color) {
  return createPNG(ICON_SIZE, ICON_SIZE, (pixels, width, height) => {
    const cx = width / 2
    const cy = height / 2
    const size = width * 0.3

    // 头部
    const headRadius = size * 0.55
    const headY = cy - size * 0.55
    drawCircle(pixels, width, cx, headY, headRadius, color, 4)

    // 身体（弧形）
    const bodyY = cy + size * 0.35
    const bodyWidth = size * 1.3
    const bodyHeight = size * 0.8

    for (let angle = 0; angle <= 180; angle += 0.5) {
      const rad = angle * Math.PI / 180
      const px = cx + bodyWidth * Math.cos(rad)
      const py = bodyY + bodyHeight * Math.sin(rad) * 0.5
      const { r, g, b } = parseColor(color)
      for (let t = -2; t <= 2; t++) {
        setPixel(pixels, width, Math.round(px + t), Math.round(py), r, g, b)
        setPixel(pixels, width, Math.round(px), Math.round(py + t), r, g, b)
      }
    }

    // 连接头部和身体
    drawLine(pixels, width, cx - bodyWidth, bodyY, cx - bodyWidth * 0.3, headY + headRadius + 5, color, 4)
    drawLine(pixels, width, cx + bodyWidth, bodyY, cx + bodyWidth * 0.3, headY + headRadius + 5, color, 4)
  })
}

// 生成所有图标
const icons = [
  { name: 'home.png', draw: () => drawHomeIcon(GRAY_COLOR) },
  { name: 'home-active.png', draw: () => drawHomeIcon(ACTIVE_COLOR) },
  { name: 'resource.png', draw: () => drawResourceIcon(GRAY_COLOR) },
  { name: 'resource-active.png', draw: () => drawResourceIcon(ACTIVE_COLOR) },
  { name: 'question.png', draw: () => drawQuestionIcon(GRAY_COLOR) },
  { name: 'question-active.png', draw: () => drawQuestionIcon(ACTIVE_COLOR) },
  { name: 'user.png', draw: () => drawUserIcon(GRAY_COLOR) },
  { name: 'user-active.png', draw: () => drawUserIcon(ACTIVE_COLOR) },
]

icons.forEach(({ name, draw }) => {
  const filepath = path.join(iconDir, name)
  const buffer = draw()
  fs.writeFileSync(filepath, buffer)
  console.log(`Generated: ${name} (${buffer.length} bytes)`)
})

console.log('\n✅ All tabbar icons generated successfully!')
console.log(`📁 Icons saved to: ${iconDir}`)
