# 18 CSS样式系统

## 目录结构

```
src/assets/styles/
├── variables.css    # CSS自定义属性（设计令牌）
├── base.css         # 基础样式重置
├── components.css   # 通用组件样式
├── animations.css   # 动画关键帧
└── main.css         # 统一入口
```

## main.css 入口

```css
@import './variables.css';
@import './base.css';
@import './components.css';
@import './animations.css';
```

## variables.css 设计令牌

### 颜色系统

```css
:root {
  /* 主色调 */
  --color-primary: #FF6B35;      /* 橙色 - 排球主题 */
  --color-secondary: #1A365D;    /* 深蓝色 - 稳重 */
  --color-accent: #6366F1;       /* 靛蓝色 - 强调 */

  /* 语义色 */
  --color-success: #22C55E;
  --color-error: #EF4444;
  --color-warning: #F59E0B;
  --color-info: #3B82F6;

  /* 文字颜色 */
  --color-text-primary: #1F2937;
  --color-text-secondary: #6B7280;
  --color-text-tertiary: #9CA3AF;
  --color-text-inverse: #FFFFFF;

  /* 背景色 */
  --color-bg-primary: #FFFFFF;
  --color-bg-secondary: #F9FAFB;
  --color-bg-tertiary: #F3F4F6;
  --color-bg-dark: #111827;

  /* 边框色 */
  --color-border: #E5E7EB;
  --color-border-dark: #D1D5DB;
}
```

### 字体系统

```css
:root {
  /* 字体族 */
  --font-sans: 'Noto Sans SC', 'Source Sans Pro', system-ui, sans-serif;
  --font-mono: 'JetBrains Mono', 'Fira Code', monospace;

  /* 字号 */
  --font-size-xs: 0.75rem;    /* 12px */
  --font-size-sm: 0.875rem;   /* 14px */
  --font-size-base: 1rem;     /* 16px */
  --font-size-lg: 1.125rem;   /* 18px */
  --font-size-xl: 1.25rem;    /* 20px */
  --font-size-2xl: 1.5rem;    /* 24px */
  --font-size-3xl: 1.875rem;  /* 30px */

  /* 行高 */
  --line-height-tight: 1.25;
  --line-height-normal: 1.5;
  --line-height-relaxed: 1.75;

  /* 字重 */
  --font-weight-normal: 400;
  --font-weight-medium: 500;
  --font-weight-semibold: 600;
  --font-weight-bold: 700;
}
```

### 间距系统

```css
:root {
  --spacing-0: 0;
  --spacing-1: 0.25rem;   /* 4px */
  --spacing-2: 0.5rem;    /* 8px */
  --spacing-3: 0.75rem;   /* 12px */
  --spacing-4: 1rem;      /* 16px */
  --spacing-5: 1.25rem;   /* 20px */
  --spacing-6: 1.5rem;     /* 24px */
  --spacing-8: 2rem;       /* 32px */
  --spacing-10: 2.5rem;    /* 40px */
  --spacing-12: 3rem;      /* 48px */
}
```

### 圆角系统

```css
:root {
  --radius-none: 0;
  --radius-sm: 0.25rem;    /* 4px */
  --radius-md: 0.5rem;     /* 8px */
  --radius-lg: 0.75rem;    /* 12px */
  --radius-xl: 1rem;       /* 16px */
  --radius-full: 9999px;
}
```

### 阴影系统

```css
:root {
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}
```

### 过渡系统

```css
:root {
  --transition-fast: 150ms ease;
  --transition-normal: 250ms ease;
  --transition-slow: 350ms ease;
}
```

## base.css 基础样式

```css
/* Box sizing */
*,
*::before,
*::after {
  box-sizing: border-box;
}

/* Reset margins */
body,
h1, h2, h3, h4, h5, h6 {
  margin: 0;
}

/* Font smoothing */
body {
  font-family: var(--font-sans);
  font-size: var(--font-size-base);
  line-height: var(--line-height-normal);
  color: var(--color-text-primary);
  background-color: var(--color-bg-primary);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* Links */
a {
  color: var(--color-primary);
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}

/* Images */
img {
  max-width: 100%;
  height: auto;
}

/* Form elements */
input,
textarea,
button,
select {
  font-family: inherit;
  font-size: inherit;
}
```

## components.css 组件样式

### 按钮

```css
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-2) var(--spacing-4);
  border-radius: var(--radius-md);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-fast);
  cursor: pointer;
  border: none;
}

.btn-primary {
  background-color: var(--color-primary);
  color: white;
}

.btn-primary:hover {
  background-color: #E55A2B;
}

.btn-secondary {
  background-color: var(--color-secondary);
  color: white;
}

.btn-outline {
  background-color: transparent;
  border: 1px solid var(--color-border);
}

.btn-outline:hover {
  background-color: var(--color-bg-secondary);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
```

### 卡片

```css
.card {
  background-color: var(--color-bg-primary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: var(--spacing-6);
  box-shadow: var(--shadow-sm);
}

.card-hover:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
```

### 输入框

```css
.input {
  width: 100%;
  padding: var(--spacing-2) var(--spacing-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
  transition: border-color var(--transition-fast);
}

.input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
}

.input-error {
  border-color: var(--color-error);
}

.textarea {
  min-height: 120px;
  resize: vertical;
}
```

### 头像

```css
.avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  object-fit: cover;
}

.avatar-sm { width: 32px; height: 32px; }
.avatar-lg { width: 56px; height: 56px; }
.avatar-xl { width: 80px; height: 80px; }
```

### 徽章

```css
.badge {
  display: inline-flex;
  align-items: center;
  padding: var(--spacing-1) var(--spacing-2);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
  border-radius: var(--radius-full);
  background-color: var(--color-bg-tertiary);
}

.badge-primary {
  background-color: var(--color-primary);
  color: white;
}

.badge-success {
  background-color: var(--color-success);
  color: white;
}

.badge-error {
  background-color: var(--color-error);
  color: white;
}
```

## animations.css 动画

```css
/* 淡入 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 淡出 */
@keyframes fadeOut {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}

/* 滑入 */
@keyframes slideIn {
  from {
    transform: translateY(-20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 缩放 */
@keyframes scaleIn {
  from {
    transform: scale(0.9);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

/* 旋转 */
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* Toast 动画 */
.toast-enter-active {
  animation: slideIn 0.3s ease;
}

.toast-leave-active {
  animation: fadeOut 0.3s ease;
}
```

## 使用示例

```html
<template>
  <div class="card card-hover">
    <h2 class="title">帖子标题</h2>
    <p class="content">帖子内容...</p>
    <div class="actions">
      <button class="btn btn-primary">点赞</button>
      <button class="btn btn-outline">评论</button>
    </div>
  </div>
</template>

<style scoped>
.card {
  max-width: 600px;
  margin: 0 auto;
}

.title {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  margin-bottom: var(--spacing-4);
}

.content {
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-6);
}

.actions {
  display: flex;
  gap: var(--spacing-3);
}
</style>
```
