# Anime TV (anime-br) 需求文档

## 1. 项目概述

### 1.1 项目名称
Anime TV (anime-br)

### 1.2 项目描述
面向巴西用户的 Android 动漫流媒体应用，提供动漫浏览、搜索、收藏和在线播放功能。
动漫/剧集数据来自本地预置 SQLite 数据库，播放源通过 API 实时获取。

### 1.3 技术栈
- Kotlin + Jetpack Compose
- MVVM + Hilt DI
- Room (预置 anime_digital.db)
- Retrofit (仅获取播放源)
- ExoPlayer/Media3 (视频播放)
- Coil (图片加载)
- Firebase Remote Config (评分弹窗配置)

---

## 2. 页面结构

### 2.1 侧边栏菜单 (Drawer)
| 菜单项 | 功能 |
|--------|------|
| Inicio | 首页 - 最近更新 |
| Buscar | 搜索页 |
| Animes | 全部动漫 |
| Gêneros | 分类选择 |
| Populares | 热门动漫 |
| Filmes | 电影列表 |
| Favoritos | 收藏列表 |
| Histórico | 播放历史 |
| Compartilhar | 系统分享 |
| Versão | 显示版本号 |

---

## 3. 各页面详细需求

### 3.1 闪屏页
- 显示 App Icon + "Anime TV"
- 停留最多 2 秒后进入首页
- 首页数据来自本地 DB，加载几乎即时

### 3.2 首页 (Lançamentos)
- TitleBar: "Lançamentos" + 搜索 icon
- 网格展示最近 50 个动漫更新
- 数据逻辑: Group by animeId → 取最大集数 → 按 createdAt 倒序
- 卡片: 横图 5:3 + 左上角集数标 + 动漫名称(2行)
- 图片未加载显示灰色占位图 (#333333)

### 3.3 详情页
- 顶部封面 16:9 + 播放按钮(点击进入剧集列表)
- 动漫名称(2行) + 收藏按钮
- 元数据: 年份 | 类型(TV/Movie) | 评分
- Gêneros: 显示全部类型
- Detalhes: 显示简介

### 3.4 剧集列表页 (Episódios)
- Title: "Episódios" + 动漫名称(1行滚动)
- List 形式，默认按 episode id 降序
- 每项: 横图 16:9 + title + 发布日期(aired/createdAt)
- 右下角浮动按钮切换升序/降序
- 点击进入播放选择页

### 3.5 播放选择页
- 显示集数 + 动漫名称
- 免责声明文字
- 从 API 获取播放源列表，显示 loading/error 状态
- 每个源显示 "Assistir - {server}"

### 3.6 视频播放器
- 横屏全屏播放
- 支持 MP4 直接播放 + Blogger iframe 解析
- 自定义控制: 返回/标题 + 快退10s/播放暂停/快进10s + 进度条/时间
- 3秒无操作隐藏控制
- 播放完成自动停止
- 支持续播(上次进度>95%则从头开始)

### 3.7 搜索页
- 搜索框 + 清空按钮
- 搜索范围: name, nameAlternative, genres
- 结果按评分(visitas)降序
- 网格展示: 竖图 2:3 + 名称(2行)

### 3.8 全部动漫 (Animes)
- 按字母顺序显示全部动漫
- 网格展示: 竖图 2:3 + 名称(2行)

### 3.9 分类 (Gêneros)
- 从 animes.genres 聚合所有分类
- 色块+文本，每行2个
- 点击进入分类动漫列表(网格 2:3)

### 3.10 热门 (Populares)
- 评分最高50部(按 visitas 降序)
- 网格展示: 竖图 2:3 + 评分角标 + 名称(2行)

### 3.11 电影 (Filmes)
- 筛选 type="Movie"
- 网格展示: 竖图 2:3 + 名称(2行)
- 无数据显示空提示

### 3.12 收藏 (Favoritos)
- 无收藏显示 "lista vazia!"
- 网格展示: 竖图 2:3 + 名称(2行)

### 3.13 播放历史 (Histórico)
- 无历史显示空提示
- 列表展示: 横图 16:9 + 进度条 + 名称(1行) + 集数
- 清空按钮 + 确认弹窗
- 最多保留100条

### 3.14 评分引导
- 触发: 成功播放视频退出播放器回到剧集列表时
- Firebase 配置展现次数(rateTime, 默认1)
- 评分状态:
  - 0星: 😃 + 引导文案 + 按钮置灰
  - 1-3星: 😢😞😮 + "Ah, sentimos muito…"
  - 4星: 😊 + "Muito obrigado!"
  - 5星: 🥰 + "Avaliar no Google Play" → 跳转 Google Play
- ≤4星提交: toast "Obrigado pelo seu feedback."
- 点击外部区域关闭

---

## 4. 数据库结构 (anime_digital.db)

### animes 表 (661条)
id, name, nameAlternative, slug, imagen, overview, aired, type, status, genres, rating, trailer, voteAverage, visitas, isDubbing, nums, isTopic, createdAt

### episodes 表 (12859条)
id, animeId, title, imagen, overview, url, visitas, nums, aired, status, createdAt

### players 表 (通过API填充)
id, animeId, episodeId, link, server, embed, status, createdAt

### watch_history 表 (本地生成)
id, animeId, episodeId, episodeNumber, progress, duration, lastWatchedAt

### favorites 表 (本地生成)
id, animeId, createdAt

---

## 5. API

### 获取播放源
```
GET https://api.jkanimeflv.com/digital/episodes/{episodeId}
```
返回播放源列表，缓存到本地 players 表。
