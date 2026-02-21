<template>
  <div class="home-container">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="nav-brand">
        <span class="logo">🏦</span>
        <span class="brand-name">银行服务机器人</span>
      </div>
      <div class="nav-links">
        <router-link to="/login" class="nav-btn login-btn">登录</router-link>
        <router-link to="/register" class="nav-btn register-btn">注册</router-link>
      </div>
    </nav>

    <!-- 主要内容区 -->
    <main class="main-content">
      <div class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title">
            欢迎使用
            <span class="highlight">银行服务机器人</span>
          </h1>
          <p class="hero-subtitle">
            智能、便捷、安全的银行服务体验
          </p>
          <p class="hero-description">
            我们的智能客服机器人为您提供 7×24 小时全天候服务，<br />
            无论是账户查询、转账汇款还是业务咨询，都能快速响应您的需求。
          </p>
          <div class="hero-buttons">
            <router-link to="/login" class="btn btn-primary">
              立即登录
            </router-link>
            <router-link to="/register" class="btn btn-secondary">
              免费注册
            </router-link>
          </div>
        </div>
        <div class="hero-image">
          <div class="robot-illustration">
            <div class="robot-head">
              <div class="robot-eyes">
                <div class="eye left"></div>
                <div class="eye right"></div>
              </div>
              <div class="robot-mouth"></div>
            </div>
            <div class="robot-body"></div>
            <div class="robot-arms">
              <div class="arm left"></div>
              <div class="arm right"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 功能特性 -->
      <section class="features-section">
        <h2 class="section-title">我们的服务</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">💳</div>
            <h3>账户管理</h3>
            <p>随时查看账户余额、交易明细，管理您的个人资产</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">💸</div>
            <h3>转账汇款</h3>
            <p>快速安全的转账服务，支持行内及跨行转账</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">📋</div>
            <h3>交易记录</h3>
            <p>完整的交易历史记录，方便您随时核对账目</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">🤖</div>
            <h3>智能客服</h3>
            <p>AI 智能客服 7×24 小时在线，解答您的各类问题</p>
          </div>
        </div>
      </section>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <p>&copy; 2024 银行服务机器人。All rights reserved.</p>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'HomeView'
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 导航栏样式 */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 50px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo {
  font-size: 32px;
}

.brand-name {
  font-size: 22px;
  font-weight: bold;
  color: white;
}

.nav-links {
  display: flex;
  gap: 15px;
}

.nav-btn {
  padding: 10px 25px;
  border-radius: 25px;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s;
}

.login-btn {
  color: white;
  border: 2px solid white;
}

.login-btn:hover {
  background: white;
  color: #667eea;
}

.register-btn {
  background: white;
  color: #667eea;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.2);
}

/* 主要内容区 */
.main-content {
  padding: 60px 50px;
  max-width: 1200px;
  margin: 0 auto;
}

.hero-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 60px;
  margin-bottom: 80px;
}

.hero-content {
  flex: 1;
  color: white;
}

.hero-title {
  font-size: 48px;
  margin: 0 0 20px 0;
  line-height: 1.2;
}

.hero-title .highlight {
  display: block;
  font-size: 56px;
  background: linear-gradient(90deg, #ffd700, #ffed4e);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: 24px;
  margin: 0 0 15px 0;
  opacity: 0.9;
}

.hero-description {
  font-size: 16px;
  line-height: 1.8;
  opacity: 0.8;
  margin-bottom: 30px;
}

.hero-buttons {
  display: flex;
  gap: 20px;
}

.btn {
  padding: 15px 40px;
  border-radius: 30px;
  text-decoration: none;
  font-weight: 600;
  font-size: 16px;
  transition: all 0.3s;
  display: inline-block;
}

.btn-primary {
  background: white;
  color: #667eea;
}

.btn-primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.btn-secondary {
  background: transparent;
  color: white;
  border: 2px solid white;
}

.btn-secondary:hover {
  background: white;
  color: #667eea;
  transform: translateY(-3px);
}

/* 机器人插图 */
.hero-image {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.robot-illustration {
  position: relative;
  width: 300px;
  height: 350px;
}

.robot-head {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 180px;
  height: 160px;
  background: linear-gradient(180deg, #ffffff 0%, #e8e8e8 100%);
  border-radius: 90px 90px 70px 70px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.robot-eyes {
  display: flex;
  justify-content: center;
  gap: 40px;
  padding-top: 50px;
}

.eye {
  width: 35px;
  height: 35px;
  background: #667eea;
  border-radius: 50%;
  position: relative;
  animation: blink 3s infinite;
}

.eye::after {
  content: '';
  position: absolute;
  width: 12px;
  height: 12px;
  background: white;
  border-radius: 50%;
  top: 8px;
  right: 8px;
}

@keyframes blink {
  0%, 90%, 100% {
    transform: scaleY(1);
  }
  95% {
    transform: scaleY(0.1);
  }
}

.robot-mouth {
  width: 60px;
  height: 20px;
  background: #ff6b6b;
  border-radius: 0 0 30px 30px;
  margin: 25px auto 0;
}

.robot-body {
  position: absolute;
  top: 150px;
  left: 50%;
  transform: translateX(-50%);
  width: 140px;
  height: 120px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  border-radius: 50px 50px 40px 40px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.robot-arms {
  position: absolute;
  top: 160px;
  width: 100%;
}

.arm {
  position: absolute;
  width: 40px;
  height: 100px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
}

.arm.left {
  left: 20px;
  transform: rotate(20deg);
}

.arm.right {
  right: 20px;
  transform: rotate(-20deg);
}

/* 功能特性区域 */
.features-section {
  text-align: center;
}

.section-title {
  font-size: 36px;
  color: white;
  margin: 0 0 50px 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 30px;
}

.feature-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 30px 20px;
  color: white;
  transition: all 0.3s;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.feature-card:hover {
  transform: translateY(-10px);
  background: rgba(255, 255, 255, 0.25);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

.feature-icon {
  font-size: 50px;
  margin-bottom: 15px;
}

.feature-card h3 {
  font-size: 20px;
  margin: 0 0 10px 0;
}

.feature-card p {
  font-size: 14px;
  line-height: 1.6;
  opacity: 0.9;
  margin: 0;
}

/* 页脚 */
.footer {
  text-align: center;
  padding: 30px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .hero-section {
    flex-direction: column;
    text-align: center;
  }

  .hero-title {
    font-size: 36px;
  }

  .hero-title .highlight {
    font-size: 42px;
  }

  .hero-buttons {
    justify-content: center;
  }

  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .navbar {
    padding: 15px 30px;
  }

  .main-content {
    padding: 40px 30px;
  }
}

@media (max-width: 576px) {
  .navbar {
    flex-direction: column;
    gap: 15px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-title .highlight {
    font-size: 32px;
  }

  .hero-subtitle {
    font-size: 18px;
  }

  .hero-buttons {
    flex-direction: column;
    align-items: center;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .robot-illustration {
    transform: scale(0.7);
  }
}
</style>
