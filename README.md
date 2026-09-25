# Freewd 社区 Android 客户端

> 一款由个人开发运营的社区类 Android 应用，使用 Jetpack Compose 从零重构。

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-purple.svg)]()
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.x-blue.svg)]()
[![License](https://img.shields.io/badge/License-暂未指定-lightgrey.svg)]()

---

## 关于项目

Freewd 社区于 **2025 年 8 月 13 日** 开始运营，起初以同名基岩版服务器社区的形式存在，官网：[community.freewd.top](https://community.freewd.top)。

本仓库是 Freewd 社区的 Android 客户端，使用 **Jetpack Compose** 完全从零重构
Freewd 由 **BLOCKCARFT（BCK）** 一人开发与运营。这是一个纯粹“用爱发电”的个人项目：没有团队，没有融资，也没有买量推广。目前因高中学业原因，更新速度会有所放缓，但项目会持续维护。

> 我们希望做一款干净、流畅、没有花里胡哨广告和多余弹窗的社区客户端，仅此而已。

- 官方网站：[community.freewd.top](https://community.freewd.top)
- 基岩版服务器官网(已关服, 寒假再见)：[freewd.top](https://www.freewd.top)
- [下载 Freewd 社区](https://github.com/blockcarft4979/Freewd/releases]
- 联系邮箱：2216368705@qq.com

---

## 功能特性

- **社区信息流**：卡片式 Feed 流，支持下拉刷新与上拉加载更多，图片自适应展示，懒加载与内存复用经过细致打磨。
- **侧滑导航抽屉**：手势侧滑打开菜单，主界面平滑平移，几乎复刻主流 App 的侧滑交互体验。
- **全屏图片预览**：点击 Feed 图片进入沉浸式全屏预览，支持双击缩放、双指缩放、拖动平移，自动隐藏状态栏与导航栏，点击空白或按返回键即可退出。
- **发帖入口**：悬浮按钮快速发帖，让社区动态触手可及。
- **通知系统**：顶部通知铃铛展示未读数，支持通知弹窗与跳转，实时掌握社区动态。
- **网络状态提示**：无网络或请求失败时通过 Snackbar 优雅提示，支持一键重试，不给用户留黑屏。
- **Token 自动校验**：应用回到前台时自动校验登录态，保障账号安全。

> 更多功能（用户主页、私信、评论楼中楼等）正在逐步补齐中，欢迎通过 Issues 提出建议。

---

## 技术栈

| 类别 | 技术 |
|---|---|
| 语言 | Kotlin |
| UI 框架 | Jetpack Compose + Material 3 |
| 架构 | MVVM（ViewModel + StateFlow） |
| 图片加载 | Coil 3 |
| 手势缩放 | net.engawapg.lib:zoomable |
| 导航 | 基于 Compose 的状态驱动 |
| 最低版本 | Android 8.0（API 26） |
| 目标版本 | Android 16（API 36） |

---

## 项目结构（简要）

```text
Freewd/
├── app/                        # 主模块
│   └── src/main/java/com/freewdcmkt/bck/
│       ├── api/                # 网络接口与工具
│       ├── components/         # 通用 Compose 组件
│       ├── data/               # 数据模型与全局状态
│       ├── layout/             # 页面级布局
│       │   ├── nav/            # 导航与主框架
│       │   ├── ui/community/   # 社区 Feed 流
│       │   └── ui/user/        # 用户相关页面
│       └── viewmodel/          # ViewModel 层
├── gradle/                     # Gradle 版本管理
└── build.gradle.kts            # 根构建脚本
```

---

## 构建与运行

1. 克隆仓库：
   ```bash
   git clone https://github.com/blockcarft4979/Freewd.git
   ```

2. 使用 Android Studio（建议 Ladybug 或更高版本）打开项目。

3. 同步 Gradle 后，连接设备或启动模拟器，点击运行即可。

   Debug 构建：
   ```bash
   ./gradlew assembleDebug
   ```

> 注意：项目依赖部分私有 API 或环境配置，如果直接运行遇到接口报错，请检查 `api/` 目录下的基础 URL 配置。本项目使用 `local.properties` 或环境变量管理敏感配置，请勿直接将密钥提交到仓库。

---

## 开发者手记

这个项目几乎陪伴了笔者整个高中阶段。它的前身是一个简单的页面，后来被推倒重来，用 Compose 一行一行地重写。

侧滑抽屉改了无数遍，图片预览从 `Dialog` 到手势冲突，再到 `PredictiveBackHandler` 的取舍……每一次重构，都是在和状态管理、生命周期以及底层 View 的脾气做博弈。

如果你在阅读源码时发现某些写法很有趣，或者某个组件的实现很“土”，请不要笑。这是一个高中生在一堆网课和试卷的缝隙里，能捣鼓出来的最好结果了。

---

## 贡献

这是一个个人项目，但非常欢迎各种形式的参与：

- 提交 Issue 反馈 Bug 或提出功能建议
- 提交 Pull Request 修复问题或优化代码
- 通过邮箱联系作者，加入 Freewd 社区

在提交 PR 前，请尽量保持代码风格与现有代码一致（Kotlin 官方风格，Compose 命名规范）。

---

## 许可

本项目暂未指定开源许可证。在未明确许可前，请勿将代码用于商业用途或二次分发。如需使用，请先联系作者。

---

## 致谢

- 感谢 Freewd 社区所有成员的支持
- 感谢 Jetpack Compose、Coil、zoomable 等开源项目
- 感谢每一个愿意下载并尝试这个“没人下”的小破 App 的你
- 特别致谢D老师(DeepSeek)的指导

---

**Copyright 2025–2026 Freewd Studio**
