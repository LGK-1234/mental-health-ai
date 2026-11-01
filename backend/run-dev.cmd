@echo off
chcp 65001 >nul
title 心理健康 AI 助手 - 后端
cd /d "%~dp0"

if "%AI_API_KEY%"=="" (
  echo 未检测到 AI_API_KEY 环境变量。
  set /p AI_API_KEY=请输入硅基流动 API Key（直接回车则沿用 yml 中的占位值）: 
)

if not "%AI_API_KEY%"=="" (
  echo 使用 API Key: %AI_API_KEY:~0,8%...
)

echo 正在启动 Spring Boot（端口 1236）...
mvnw.cmd spring-boot:run
pause
