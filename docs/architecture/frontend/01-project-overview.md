# 01 项目概述

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| **Vue** | 3.5.31 | 渐进式JavaScript框架 |
| **Vue Router** | 4.6.4 | Vue官方路由管理 |
| **Vite** | 5.4.0 | 下一代前端构建工具 |
| **@vitejs/plugin-vue** | 5.2.1 | Vite的Vue插件 |
| **vite-plugin-vue-devtools** | 7.6.0 | Vue DevTools集成 |

## 依赖配置

**package.json**:
```json
{
  "name": "volleyball-community-frontend",
  "version": "0.0.0",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.5.31",
    "vue-router": "^4.6.4"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.2.1",
    "vite": "^5.4.0",
    "vite-plugin-vue-devtools": "^7.6.0"
  },
  "engines": {
    "node": "^20.19.0 || >=22.12.0"
  }
}
```

## 环境配置

**vite.config.js**:
```javascript
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5000,
    proxy: {
      '/api': {
        target: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    base: './'
  }
})
```

## 环境变量

**.env.example**:
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_API_PREFIX=/api
VITE_FILE_BASE_URL=http://localhost:8080
VITE_FILE_UPLOAD_PATH=/api/file/upload
VITE_FILE_DOWNLOAD_PATH=/api/file/download
VITE_SSE_URL=http://localhost:8080
```

## 入口文件

**index.html**:
```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <title>排球社区</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+SC&display=swap" rel="stylesheet">
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.js"></script>
</body>
</html>
```

## 字体配置

- **Noto Sans SC**: 中文正文
- **Source Sans Pro**: 英文正文
- **JetBrains Mono**: 代码/数字

## 样式系统

使用CSS自定义属性定义设计系统，参见 [18-CSS样式系统](18-css-system.md)

## 构建产物

```
dist/
├── index.html
├── assets/
│   └── *.css, *.js
└── (静态资源)
```
