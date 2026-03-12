<template>
  <div class="root">
    <!-- Neural particle canvas -->
    <canvas ref="cvs" class="cvs"></canvas>

    <!-- Aurora blobs -->
    <div class="aurora">
      <div class="blob b1"></div>
      <div class="blob b2"></div>
      <div class="blob b3"></div>
    </div>

    <!-- Grid lines -->
    <div class="grid"></div>

    <!-- Shooting streaks -->
    <div class="streak st1"></div>
    <div class="streak st2"></div>
    <div class="streak st3"></div>

    <!-- Layout -->
    <div class="scene">

      <!-- ── Left brand ─────────────────── -->
      <div class="brand">
        <div class="logo-wrap">
          <div class="ring rg1"></div>
          <div class="ring rg2"></div>
          <div class="ring rg3"></div>
          <div class="logo-core">CL</div>
        </div>

        <div class="brand-text">
          <h1>Campus<br><span class="accent">Link</span></h1>
          <p class="tagline">高校资源共享与问答社区<br>管理员控制台</p>
        </div>

        <ul class="pills">
          <li v-for="(f, i) in features" :key="i" :style="{ '--i': i }">
            <span class="pill-dot"></span>{{ f }}
          </li>
        </ul>

        <div class="stats">
          <div class="stat" v-for="(s, i) in stats" :key="i" :style="{ '--i': i }">
            <b>{{ s.val }}</b>
            <span>{{ s.lbl }}</span>
          </div>
        </div>
      </div>

      <!-- ── Right form card ─────────────── -->
      <div class="card-wrap">
        <div class="card" :class="{ shake: shaking }">
          <!-- Grain texture overlay -->
          <div class="grain"></div>
          <!-- Top glow line -->
          <div class="card-glow-line"></div>

          <div class="card-head">
            <div class="badge">Admin Portal</div>
            <h2>管理员登录</h2>
            <p>使用管理员账号访问后台</p>
          </div>

          <div class="fields">
            <!-- Account field -->
            <div class="field" :class="{ on: focus === 'acc', fill: !!form.account, err: !!errors.account }">
              <label>账号</label>
              <div class="finput">
                <svg class="fic" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"/>
                </svg>
                <input
                  v-model="form.account"
                  type="text"
                  placeholder="用户名 / 邮箱 / 手机号"
                  autocomplete="username"
                  @focus="focus = 'acc'"
                  @blur="focus = ''"
                />
              </div>
              <span class="fbar"></span>
              <p class="ferr" v-if="errors.account">{{ errors.account }}</p>
            </div>

            <!-- Password field -->
            <div class="field" :class="{ on: focus === 'pwd', fill: !!form.password, err: !!errors.password }">
              <label>密码</label>
              <div class="finput">
                <svg class="fic" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd"/>
                </svg>
                <input
                  v-model="form.password"
                  :type="showPwd ? 'text' : 'password'"
                  placeholder="请输入密码"
                  autocomplete="current-password"
                  @focus="focus = 'pwd'"
                  @blur="focus = ''"
                  @keyup.enter="handleLogin"
                />
                <button class="eye" type="button" @click="showPwd = !showPwd" tabindex="-1">
                  <svg v-if="showPwd" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M10 12a2 2 0 100-4 2 2 0 000 4z"/>
                    <path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd"/>
                  </svg>
                  <svg v-else viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.014 10.014 0 0019.542 10C18.268 5.943 14.478 3 10 3a9.958 9.958 0 00-4.512 1.074l-1.78-1.781zm4.261 4.26l1.514 1.515a2.003 2.003 0 012.45 2.45l1.514 1.514a4 4 0 00-5.478-5.478z" clip-rule="evenodd"/>
                    <path d="M12.454 16.697L9.75 13.992a4 4 0 01-3.742-3.741L2.335 6.578A9.98 9.98 0 00.458 10c1.274 4.057 5.064 7 9.542 7 .847 0 1.669-.105 2.454-.303z"/>
                  </svg>
                </button>
              </div>
              <span class="fbar"></span>
              <p class="ferr" v-if="errors.password">{{ errors.password }}</p>
            </div>
          </div>

          <!-- Submit button -->
          <button
            class="go"
            :class="{ loading, done: loginDone }"
            :disabled="loading"
            @click="handleLogin"
            @mousedown="spawnRipple"
          >
            <span class="go-shimmer"></span>
            <span class="go-text">
              <template v-if="loginDone">
                <svg class="check" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                </svg>
                登录成功
              </template>
              <template v-else-if="loading">
                <span class="dots"><i></i><i></i><i></i></span>
              </template>
              <template v-else>登录后台 →</template>
            </span>
            <span class="go-pulse"></span>
          </button>

          <p class="caption">仅限具有管理员权限的账号访问</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const cvs = ref<HTMLCanvasElement>()
const focus = ref('')
const showPwd = ref(false)
const loading = ref(false)
const loginDone = ref(false)
const shaking = ref(false)

const form = reactive({ account: '', password: '' })
const errors = reactive({ account: '', password: '' })

const features = ['全平台数据实时监控', '用户与内容精细化管理', '社团活动一键运营']
const stats = [
  { val: '10K+', lbl: '活跃用户' },
  { val: '50K+', lbl: '资源总数' },
  { val: '200+', lbl: '社团组织' },
]

// ─── Canvas particle network ───────────────────
let raf = 0
onMounted(() => {
  const canvas = cvs.value!
  const ctx = canvas.getContext('2d')!

  const resize = () => {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
  }
  resize()
  window.addEventListener('resize', resize)

  const N = 70
  const pts = Array.from({ length: N }, () => ({
    x: Math.random() * canvas.width,
    y: Math.random() * canvas.height,
    vx: (Math.random() - 0.5) * 0.4,
    vy: (Math.random() - 0.5) * 0.4,
    r: Math.random() * 1.5 + 0.5,
    a: Math.random() * 0.5 + 0.15,
  }))

  const draw = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    const D = 130

    for (let i = 0; i < pts.length; i++) {
      for (let j = i + 1; j < pts.length; j++) {
        const dx = pts[i].x - pts[j].x
        const dy = pts[i].y - pts[j].y
        const d = Math.sqrt(dx * dx + dy * dy)
        if (d < D) {
          ctx.beginPath()
          ctx.strokeStyle = `rgba(139,92,246,${0.18 * (1 - d / D)})`
          ctx.lineWidth = 0.6
          ctx.moveTo(pts[i].x, pts[i].y)
          ctx.lineTo(pts[j].x, pts[j].y)
          ctx.stroke()
        }
      }
    }

    for (const p of pts) {
      ctx.beginPath()
      ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(167,139,250,${p.a})`
      ctx.fill()
      p.x += p.vx
      p.y += p.vy
      if (p.x < 0 || p.x > canvas.width) p.vx *= -1
      if (p.y < 0 || p.y > canvas.height) p.vy *= -1
    }

    raf = requestAnimationFrame(draw)
  }
  draw()

  onUnmounted(() => {
    cancelAnimationFrame(raf)
    window.removeEventListener('resize', resize)
  })
})

// ─── Ripple effect ────────────────────────────
function spawnRipple(e: MouseEvent) {
  const btn = e.currentTarget as HTMLElement
  const rect = btn.getBoundingClientRect()
  const size = Math.max(rect.width, rect.height) * 2
  const x = e.clientX - rect.left - size / 2
  const y = e.clientY - rect.top - size / 2
  const ripple = document.createElement('span')
  // Inline styles: @keyframes rippleAnim is defined in scoped CSS but keyframes are NOT scoped by Vue
  ripple.style.cssText = `
    position:absolute;width:${size}px;height:${size}px;
    left:${x}px;top:${y}px;border-radius:50%;
    background:rgba(255,255,255,.22);transform:scale(0);
    animation:rippleAnim .6s ease-out forwards;pointer-events:none;z-index:0;
  `
  btn.appendChild(ripple)
  setTimeout(() => ripple.remove(), 700)
}

// ─── Shake ───────────────────────────────────
function triggerShake() {
  shaking.value = true
  setTimeout(() => (shaking.value = false), 500)
}

// ─── Login ───────────────────────────────────
async function handleLogin() {
  errors.account = ''
  errors.password = ''
  let hasErr = false
  if (!form.account.trim()) { errors.account = '请输入账号'; hasErr = true }
  if (!form.password.trim()) { errors.password = '请输入密码'; hasErr = true }
  if (hasErr) { triggerShake(); return }

  loading.value = true
  try {
    const result = await login(form)
    if (result.user.role !== 'admin') {
      ElMessage.error('无管理员权限，请使用管理员账号登录')
      triggerShake()
      return
    }
    authStore.setAuth({
      uId: result.user.uId,
      username: result.user.username,
      nickname: result.user.nickname,
      role: result.user.role,
      avatarUrl: result.user.avatarUrl,
      token: result.token,
      refreshToken: result.refreshToken,
    })
    loginDone.value = true
    setTimeout(() => router.push('/dashboard'), 800)
  } catch {
    triggerShake()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ══════════════════════════════════════════════
   Root
══════════════════════════════════════════════ */
.root {
  min-height: 100vh;
  background: #07041a;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
  font-family: 'Nunito', sans-serif;
}

/* ══════════════════════════════════════════════
   Canvas
══════════════════════════════════════════════ */
.cvs {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}

/* ══════════════════════════════════════════════
   Aurora blobs
══════════════════════════════════════════════ */
.aurora { position: fixed; inset: 0; z-index: 1; pointer-events: none; }

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
}
.b1 {
  width: 700px; height: 700px;
  background: radial-gradient(circle, rgba(109,40,217,.32) 0%, transparent 70%);
  top: -200px; left: -150px;
  animation: drift 11s ease-in-out infinite;
}
.b2 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, rgba(236,72,153,.2) 0%, transparent 70%);
  bottom: -100px; left: 35%;
  animation: drift 14s ease-in-out infinite reverse 2s;
}
.b3 {
  width: 350px; height: 350px;
  background: radial-gradient(circle, rgba(56,189,248,.18) 0%, transparent 70%);
  top: 20%; right: 5%;
  animation: drift 9s ease-in-out infinite 4s;
}
@keyframes drift {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -40px) scale(1.05); }
  66% { transform: translate(-20px, 20px) scale(0.97); }
}

/* ══════════════════════════════════════════════
   Grid
══════════════════════════════════════════════ */
.grid {
  position: fixed;
  inset: 0;
  z-index: 2;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(139,92,246,.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(139,92,246,.06) 1px, transparent 1px);
  background-size: 60px 60px;
  animation: gridPulse 6s ease-in-out infinite;
}
@keyframes gridPulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

/* ══════════════════════════════════════════════
   Shooting streaks
══════════════════════════════════════════════ */
.streak {
  position: fixed;
  z-index: 3;
  pointer-events: none;
  width: 180px; height: 1px;
  background: linear-gradient(90deg, transparent, rgba(167,139,250,.8), transparent);
  transform-origin: left center;
}
.st1 { top: 18%; left: -200px; transform: rotate(-20deg); animation: shoot 7s linear infinite 0s; }
.st2 { top: 52%; left: -200px; transform: rotate(-12deg); animation: shoot 9s linear infinite 3s; }
.st3 { top: 78%; left: -200px; transform: rotate(-8deg);  animation: shoot 11s linear infinite 5.5s; }

@keyframes shoot {
  0%   { left: -200px; opacity: 0; }
  5%   { opacity: 1; }
  90%  { opacity: 1; }
  100% { left: 110vw; opacity: 0; }
}

/* ══════════════════════════════════════════════
   Scene layout
══════════════════════════════════════════════ */
.scene {
  position: relative;
  z-index: 10;
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 1100px;
  padding: 40px 48px;
  gap: 80px;
}

/* ══════════════════════════════════════════════
   Brand side
══════════════════════════════════════════════ */
.brand {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* Logo orbital rings */
.logo-wrap {
  position: relative;
  width: 90px; height: 90px;
  margin-bottom: 36px;
  animation: logoIn 0.8s cubic-bezier(.34,1.56,.64,1) both;
}
@keyframes logoIn {
  from { opacity: 0; transform: scale(0.5) rotate(-90deg); }
  to   { opacity: 1; transform: scale(1) rotate(0deg); }
}

.ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 1.5px solid transparent;
}
.rg1 {
  inset: -8px;
  border-color: rgba(139,92,246,.5);
  animation: spinCW 8s linear infinite;
}
.rg2 {
  inset: -18px;
  border-color: rgba(236,72,153,.3);
  border-style: dashed;
  animation: spinCCW 12s linear infinite;
}
.rg3 {
  inset: -30px;
  border-color: rgba(139,92,246,.15);
  animation: spinCW 20s linear infinite;
}
@keyframes spinCW  { to { transform: rotate(360deg); } }
@keyframes spinCCW { to { transform: rotate(-360deg); } }

.logo-core {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #7c3aed, #ec4899);
  border-radius: 22px;
  font-family: 'Outfit', sans-serif;
  font-size: 26px; font-weight: 900;
  color: #fff;
  box-shadow: 0 0 40px rgba(124,58,237,.7), 0 0 15px rgba(236,72,153,.5);
}

.brand-text {
  animation: fadeUp 0.6s ease 0.2s both;
}
.brand-text h1 {
  font-family: 'Outfit', sans-serif;
  font-size: 50px; font-weight: 900;
  line-height: 1.05;
  color: #fff;
  letter-spacing: -2px;
  margin: 0 0 12px;
}
.accent {
  background: linear-gradient(135deg, #a78bfa, #f472b6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.tagline {
  font-size: 15px;
  color: rgba(255,255,255,.45);
  line-height: 1.6;
  font-weight: 500;
  margin: 0 0 36px;
}

/* Feature pills */
.pills {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin: 0 0 40px;
  padding: 0;
}
.pills li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 600;
  color: rgba(255,255,255,.65);
  animation: fadeUp 0.5s ease calc(0.4s + var(--i) * 0.1s) both;
}
.pill-dot {
  width: 7px; height: 7px;
  background: linear-gradient(135deg, #8b5cf6, #ec4899);
  border-radius: 50%;
  flex-shrink: 0;
  box-shadow: 0 0 8px rgba(139,92,246,.8);
  animation: dotPulse 2s ease-in-out infinite;
}
@keyframes dotPulse {
  0%, 100% { box-shadow: 0 0 6px rgba(139,92,246,.7); }
  50% { box-shadow: 0 0 14px rgba(139,92,246,1), 0 0 6px rgba(236,72,153,.6); }
}

/* Stats row */
.stats {
  display: flex;
  gap: 0;
  background: rgba(255,255,255,.04);
  border: 1px solid rgba(139,92,246,.2);
  border-radius: 16px;
  padding: 18px 24px;
  backdrop-filter: blur(8px);
  width: fit-content;
  animation: fadeUp 0.5s ease 0.7s both;
}
.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 0 22px;
  border-right: 1px solid rgba(255,255,255,.1);
}
.stat:first-child { padding-left: 0; }
.stat:last-child  { padding-right: 0; border: none; }
.stat b {
  font-family: 'Outfit', sans-serif;
  font-size: 22px; font-weight: 900;
  color: #fff;
  letter-spacing: -0.5px;
}
.stat span {
  font-size: 11px;
  color: rgba(255,255,255,.4);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* ══════════════════════════════════════════════
   Card
══════════════════════════════════════════════ */
.card-wrap {
  width: 420px;
  flex-shrink: 0;
  animation: cardIn 0.7s cubic-bezier(.34,1.2,.64,1) 0.1s both;
}
@keyframes cardIn {
  from { opacity: 0; transform: translateY(30px) scale(0.97); }
  to   { opacity: 1; transform: translateY(0) scale(1); }
}

.card {
  position: relative;
  background: rgba(15,10,40,.7);
  backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid rgba(139,92,246,.25);
  border-radius: 24px;
  padding: 40px 36px 32px;
  box-shadow:
    0 0 0 1px rgba(139,92,246,.1),
    0 40px 80px rgba(0,0,0,.5),
    inset 0 1px 0 rgba(255,255,255,.06);
  overflow: hidden;
  transition: transform 0.15s;
}
.card.shake {
  animation: shake 0.45s cubic-bezier(.36,.07,.19,.97) both;
}
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  15% { transform: translateX(-8px); }
  30% { transform: translateX(7px); }
  45% { transform: translateX(-6px); }
  60% { transform: translateX(5px); }
  75% { transform: translateX(-4px); }
  90% { transform: translateX(3px); }
}

/* Grain texture */
.grain {
  position: absolute;
  inset: 0;
  border-radius: 24px;
  pointer-events: none;
  opacity: 0.03;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  background-size: 128px;
}

/* Top glow line */
.card-glow-line {
  position: absolute;
  top: 0; left: 20%; right: 20%;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(139,92,246,.8), rgba(236,72,153,.6), transparent);
  border-radius: 2px;
  animation: glowPulse 3s ease-in-out infinite;
}
@keyframes glowPulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; filter: drop-shadow(0 0 6px rgba(139,92,246,.9)); }
}

/* Card header */
.card-head {
  margin-bottom: 32px;
  animation: fadeUp 0.5s ease 0.3s both;
}
.badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  background: rgba(139,92,246,.2);
  color: #a78bfa;
  border: 1px solid rgba(139,92,246,.3);
  margin-bottom: 14px;
}
.card-head h2 {
  font-family: 'Outfit', sans-serif;
  font-size: 28px; font-weight: 800;
  color: #fff;
  margin: 0 0 6px;
  letter-spacing: -0.5px;
}
.card-head p {
  font-size: 13px;
  color: rgba(255,255,255,.4);
  margin: 0;
}

/* ══════════════════════════════════════════════
   Form fields
══════════════════════════════════════════════ */
.fields {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 28px;
}

.field {
  position: relative;
  animation: fadeUp 0.5s ease calc(0.4s + var(--fd, 0) * 0.1s) both;
}
.fields .field:nth-child(1) { --fd: 0; }
.fields .field:nth-child(2) { --fd: 1; }

.field label {
  display: block;
  font-size: 11px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: rgba(255,255,255,.4);
  margin-bottom: 8px;
  transition: color 0.2s;
}
.field.on label  { color: #a78bfa; }
.field.err label { color: #f87171; }

.finput {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(255,255,255,.05);
  border: 1px solid rgba(255,255,255,.1);
  border-radius: 12px;
  transition: border-color 0.25s, background 0.25s, box-shadow 0.25s;
}
.field.on .finput {
  border-color: rgba(139,92,246,.6);
  background: rgba(139,92,246,.08);
  box-shadow: 0 0 0 3px rgba(139,92,246,.12), 0 0 20px rgba(139,92,246,.1);
}
.field.err .finput {
  border-color: rgba(248,113,113,.5);
  box-shadow: 0 0 0 3px rgba(248,113,113,.1);
}

.fic {
  width: 16px; height: 16px;
  flex-shrink: 0;
  margin-left: 14px;
  color: rgba(255,255,255,.25);
  transition: color 0.2s;
}
.field.on .fic  { color: #8b5cf6; }
.field.err .fic { color: #f87171; }

.finput input {
  flex: 1;
  background: none;
  border: none;
  outline: none;
  padding: 14px 12px;
  font-family: 'Nunito', sans-serif;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  caret-color: #8b5cf6;
}
.finput input::placeholder { color: rgba(255,255,255,.2); font-weight: 500; }

/* Animated bottom bar */
.fbar {
  position: absolute;
  bottom: -1px; left: 50%;
  width: 0; height: 2px;
  background: linear-gradient(90deg, #7c3aed, #ec4899);
  border-radius: 2px;
  transform: translateX(-50%);
  transition: width 0.3s cubic-bezier(.4,0,.2,1);
}
.field.on  .fbar { width: calc(100% - 24px); }
.field.err .fbar { width: calc(100% - 24px); background: #f87171; }

.ferr {
  font-size: 11px; color: #f87171; margin: 6px 0 0; font-weight: 600;
}

/* Eye button */
.eye {
  background: none; border: none; outline: none;
  cursor: pointer;
  padding: 0 14px;
  color: rgba(255,255,255,.3);
  display: flex; align-items: center;
  transition: color 0.2s;
}
.eye:hover { color: rgba(255,255,255,.7); }
.eye svg { width: 16px; height: 16px; }

/* ══════════════════════════════════════════════
   Submit button
══════════════════════════════════════════════ */
.go {
  position: relative;
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 14px;
  cursor: pointer;
  overflow: hidden;
  background: linear-gradient(135deg, #6d28d9 0%, #7c3aed 40%, #a855f7 70%, #ec4899 100%);
  background-size: 200% 200%;
  animation: gradShift 4s ease infinite;
  box-shadow:
    0 4px 24px rgba(124,58,237,.5),
    0 1px 0 rgba(255,255,255,.1) inset;
  transition: transform 0.15s, box-shadow 0.15s;
  font-family: 'Outfit', sans-serif;
  font-size: 16px; font-weight: 700;
  color: #fff;
  letter-spacing: 0.5px;
  margin-bottom: 20px;
}
@keyframes gradShift {
  0%   { background-position: 0% 50%; }
  50%  { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
.go:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(124,58,237,.65), 0 1px 0 rgba(255,255,255,.1) inset;
}
.go:active:not(:disabled) { transform: translateY(0); }
.go:disabled { cursor: not-allowed; opacity: 0.7; animation: none; }
.go.done { background: linear-gradient(135deg, #059669, #10b981); animation: none; }

/* Shimmer sweep */
.go-shimmer {
  position: absolute;
  top: 0; left: -100%;
  width: 60%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,.18), transparent);
  transform: skewX(-20deg);
  animation: shimmer 3s ease-in-out infinite 1s;
}
@keyframes shimmer {
  0%, 100% { left: -100%; }
  50% { left: 160%; }
}

.go-text {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.check { width: 18px; height: 18px; }

/* Outer pulse ring */
.go-pulse {
  position: absolute;
  inset: 0;
  border-radius: 14px;
  border: 2px solid rgba(139,92,246,.6);
  animation: btnPulse 2.5s ease-in-out infinite;
  pointer-events: none;
}
@keyframes btnPulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.04); opacity: 0; }
}

/* Ripple keyframe — NOT scoped by Vue, so dynamically created elements can use it */
@keyframes rippleAnim {
  to { transform: scale(1); opacity: 0; }
}

/* Dot loader */
.dots { display: flex; gap: 5px; align-items: center; }
.dots i {
  display: inline-block;
  width: 6px; height: 6px;
  background: rgba(255,255,255,.9);
  border-radius: 50%;
  animation: bounce 1.2s ease-in-out infinite;
}
.dots i:nth-child(2) { animation-delay: .15s; }
.dots i:nth-child(3) { animation-delay: .3s; }
@keyframes bounce {
  0%, 80%, 100% { transform: translateY(0); opacity: .5; }
  40% { transform: translateY(-5px); opacity: 1; }
}

.caption {
  text-align: center;
  font-size: 12px;
  color: rgba(255,255,255,.2);
  font-weight: 600;
  letter-spacing: 0.3px;
}

/* ══════════════════════════════════════════════
   Shared animations
══════════════════════════════════════════════ */
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* ══════════════════════════════════════════════
   Responsive
══════════════════════════════════════════════ */
@media (max-width: 860px) {
  .scene { flex-direction: column; padding: 32px 20px; gap: 40px; }
  .brand { align-items: center; text-align: center; }
  .brand-text h1 { font-size: 36px; }
  .pills li { justify-content: center; }
  .card-wrap { width: 100%; max-width: 420px; }
}
</style>
