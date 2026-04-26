# -*- coding: utf-8 -*-
"""
日志工具模块 (core/logger.py)

提供统一的彩色日志输出功能。
支持不同级别、不同模块的日志格式化输出。

用法:
    from src.core.logger import Logger, LogLevel
    
    logger = Logger('Api')
    logger.info('收到请求')
    logger.success('操作成功')
    logger.warning('注意: 参数为空')
    logger.error('操作失败')
"""

import sys
import time
from datetime import datetime
from enum import Enum
from typing import Optional


class LogLevel(Enum):
    """日志级别"""
    DEBUG = 0
    INFO = 1
    SUCCESS = 2
    WARNING = 3
    ERROR = 4


class Logger:
    """彩色日志记录器"""
    
    # ANSI 颜色码
    COLORS = {
        'reset': '\033[0m',
        'bold': '\033[1m',
        'dim': '\033[2m',
        
        # 前景色
        'black': '\033[30m',
        'red': '\033[31m',
        'green': '\033[32m',
        'yellow': '\033[33m',
        'blue': '\033[34m',
        'magenta': '\033[35m',
        'cyan': '\033[36m',
        'white': '\033[37m',
        
        # 亮色
        'bright_red': '\033[91m',
        'bright_green': '\033[92m',
        'bright_yellow': '\033[93m',
        'bright_blue': '\033[94m',
        'bright_magenta': '\033[95m',
        'bright_cyan': '\033[96m',
    }
    
    # 日志级别配置: (前缀文字, 前景色, 背景色)
    LEVEL_CONFIG = {
        LogLevel.DEBUG: ('DBG', 'dim', None),
        LogLevel.INFO: ('INFO', 'cyan', None),
        LogLevel.SUCCESS: (' OK ', 'bright_green', None),
        LogLevel.WARNING: ('WARN', 'bright_yellow', None),
        LogLevel.ERROR: ('ERR!', 'bright_red', None),
    }
    
    def __init__(self, module: str = 'App', level: LogLevel = LogLevel.INFO):
        """
        初始化日志记录器
        
        Args:
            module: 模块名称，用于日志前缀
            level: 日志级别，低于此级别的日志不输出
        """
        self.module = module
        self.level = level
        self._start_time = datetime.now()
    
    def _color(self, text: str, color: str) -> str:
        """为文本添加颜色"""
        return f"{self.COLORS.get(color, '')}{text}{self.COLORS['reset']}"
    
    def _format(self, level: LogLevel, message: str) -> str:
        """格式化日志消息"""
        if level.value < self.level.value:
            return None
        
        prefix, fg_color, bg_color = self.LEVEL_CONFIG.get(level, ('???', 'white', None))
        
        # 时间戳 (相对启动时间)
        elapsed = (datetime.now() - self._start_time).total_seconds()
        timestamp = f"[{elapsed:>8.3f}s]"
        
        # 模块名
        module_str = f"[{self.module:>12}]"
        
        # 构建消息
        msg_parts = []
        
        # 时间戳
        msg_parts.append(self._color(timestamp, 'dim'))
        
        # 级别标签
        msg_parts.append(self._color(f" {prefix} ", fg_color))
        
        # 模块名
        msg_parts.append(self._color(module_str, 'dim'))
        
        # 消息内容
        msg_parts.append(message)
        
        return ' '.join(msg_parts)
    
    def _print(self, level: LogLevel, message: str, *args, **kwargs):
        """打印日志到控制台"""
        formatted = self._format(level, message.format(*args, **kwargs))
        if formatted:
            print(formatted, file=sys.stderr)
    
    def debug(self, message: str, *args, **kwargs):
        """调试级别日志"""
        self._print(LogLevel.DEBUG, message, *args, **kwargs)
    
    def info(self, message: str, *args, **kwargs):
        """信息级别日志"""
        self._print(LogLevel.INFO, message, *args, **kwargs)
    
    def success(self, message: str, *args, **kwargs):
        """成功级别日志"""
        self._print(LogLevel.SUCCESS, message, *args, **kwargs)
    
    def warning(self, message: str, *args, **kwargs):
        """警告级别日志"""
        self._print(LogLevel.WARNING, message, *args, **kwargs)
    
    def error(self, message: str, *args, **kwargs):
        """错误级别日志"""
        self._print(LogLevel.ERROR, message, *args, **kwargs)
    
    def request(self, method: str, path: str, status_code: int = None, duration_ms: float = None):
        """
        记录 HTTP 请求日志
        
        Args:
            method: HTTP 方法
            path: 请求路径
            status_code: HTTP 状态码 (可选)
            duration_ms: 请求耗时毫秒 (可选)
        """
        elapsed = (datetime.now() - self._start_time).total_seconds()
        timestamp = f"[{elapsed:>8.3f}s]"
        
        method_color = {
            'GET': 'bright_blue',
            'POST': 'bright_green',
            'PUT': 'bright_yellow',
            'DELETE': 'bright_red',
            'PATCH': 'bright_magenta',
        }.get(method.upper(), 'white')
        
        parts = [
            self._color(timestamp, 'dim'),
            self._color(f" {method:8} ", method_color),
            self._color(f"[{self.module:>12}]", 'dim'),
            path,
        ]
        
        if status_code is not None:
            if 200 <= status_code < 300:
                status_str = self._color(f" {status_code} ", 'bright_green')
            elif 300 <= status_code < 400:
                status_str = self._color(f" {status_code} ", 'bright_blue')
            elif 400 <= status_code < 500:
                status_str = self._color(f" {status_code} ", 'bright_yellow')
            else:
                status_str = self._color(f" {status_code} ", 'bright_red')
            parts.append(status_str)
        
        if duration_ms is not None:
            parts.append(self._color(f"({duration_ms:.1f}ms)", 'dim'))
        
        print(' '.join(parts), file=sys.stderr)


# 全局日志工厂
_loggers = {}


def get_logger(module: str, level: LogLevel = LogLevel.INFO) -> Logger:
    """
    获取日志记录器实例 (单例模式)
    
    Args:
        module: 模块名称
        level: 日志级别
    
    Returns:
        Logger 实例
    """
    if module not in _loggers:
        _loggers[module] = Logger(module, level)
    return _loggers[module]


def log_banner(title: str, subtitle: str = None):
    """
    打印漂亮的 Banner 信息
    
    Args:
        title: 主标题
        subtitle: 副标题 (可选)
    """
    border = '═' * 50
    reset = Logger.COLORS['reset']
    bright_cyan = Logger.COLORS['bright_cyan']
    bright_green = Logger.COLORS['bright_green']
    bright_yellow = Logger.COLORS['bright_yellow']
    
    print(file=sys.stderr)
    print(f"{bright_cyan}╔{border}╗{reset}", file=sys.stderr)
    print(f"{bright_cyan}║{reset} {bright_green}{title:^48}{bright_cyan} ║{reset}", file=sys.stderr)
    if subtitle:
        print(f"{bright_cyan}║{reset} {bright_yellow}{subtitle:^48}{bright_cyan} ║{reset}", file=sys.stderr)
    print(f"{bright_cyan}╚{border}╝{reset}", file=sys.stderr)
    print(file=sys.stderr)


def log_section(title: str):
    """
    打印分隔标题
    
    Args:
        title: 分隔标题文字
    """
    reset = Logger.COLORS['reset']
    bright_magenta = Logger.COLORS['bright_magenta']
    
    print(file=sys.stderr)
    print(f"{bright_magenta}━━━ {title} {'━' * (50 - len(title) - 4)} ━━━{reset}", file=sys.stderr)