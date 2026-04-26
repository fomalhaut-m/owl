# OWL Python 工作空间服务

提供 HTTP API 供 Java Agent 远程执行 Python 脚本和管理用户工作空间。

## 功能特性

- ✅ **全局脚本**：公共脚本，所有用户可执行
- ✅ **用户工作空间**：每个用户独立的脚本目录
- ✅ **安全隔离**：防止路径穿越攻击
- ✅ **自动创建**：用户目录不存在时自动创建

## 快速开始

### 1. 构建镜像
```bash
docker build -t owl-workspace .
```

### 2. 运行容器
```bash
# 基本运行
docker run --rm owl-workspace

# 挂载本地代码目录（开发模式）
docker run --rm -v ${PWD}:/app owl-workspace

# 指定端口
docker run --rm -p 8000:8000 owl-workspace
```

## 目录结构

```
owl-workspace/
└── users/
    ├── user1/
    │   ├── data_analysis.py
    │   └── output.csv
    ├── user2/
    │   └── report.py
    └── ...
```

## 说明

- 基于 Python 3.11 slim 镜像
- 默认工作目录：`/app`
- 默认启动命令：`python server.py` (Flask Web 服务)
- 暴露端口：8000
- 使用非 root 用户运行（安全性）
- 脚本目录：`/app/scripts`

## 自定义

1. 在 `requirements.txt` 中添加 Python 依赖
2. 将 Python 脚本放入 `scripts/` 目录
3. Java 端使用 `PythonScriptTools` 调用远程脚本
