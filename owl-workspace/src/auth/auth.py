# -*- coding: utf-8 -*-
"""
认证模块 (auth/auth.py)

处理 API Key 身份验证。
使用装饰器模式，在请求处理前进行认证。

关联:
- 被 WorkspaceApi._register_routes() 中 app.before_request 调用
- 依赖 Config 的 api_key 和 public_routes

注意事项:
- 如果未配置 OWL_API_KEY，跳过验证(开发模式)
- 支持两种认证头格式:
  1. Authorization: Bearer <api_key>
  2. Authorization: <api_key>
- 使用 secrets.compare_digest 防止时序攻击

用法:
    from src.auth import Auth
    from src.core import Config
    
    config = Config()
    auth = Auth(config)
    
    # 作为装饰器使用
    @auth.require_auth
    def my_view():
        return Resp.success({'data': 'hello'})
"""

import secrets
from functools import wraps
from flask import request, jsonify

from ..core.config import Config
from ..core.logger import get_logger


class Auth:
    """
    认证类
    
    处理 API Key 验证，使用装饰器模式。
    """
    
    def __init__(self, config: Config):
        """
        初始化认证类
        
        Args:
            config: Config 实例，需要 api_key 和 public_routes
        """
        self.config = config
        self.logger = get_logger('Auth')
    
    def require_auth(self, f):
        """
        认证装饰器
        
        在函数执行前检查 API Key。
        
        Args:
            f: 被装饰的函数
        
        Returns:
            装饰后的函数
        """
        @wraps(f)
        def decorated(*args, **kwargs):
            # 开发模式: 未配置 API Key 时跳过验证
            if self.config.api_key is None:
                return f(*args, **kwargs)
            
            # 获取认证头
            auth_header = request.headers.get('Authorization', '')
            
            # 没有认证头
            if not auth_header:
                return jsonify({'error': 'Missing Authorization header'}), 401
            
            # 提取 API Key
            if auth_header.startswith('Bearer '):
                provided_key = auth_header[7:]
            else:
                provided_key = auth_header
            
            # 安全比较 (防止时序攻击)
            if not secrets.compare_digest(provided_key, self.config.api_key):
                return jsonify({'error': 'Invalid API key'}), 401
            
            return f(*args, **kwargs)
        
        return decorated
    
    def check_auth(self):
        """
        请求前检查认证
        
        用于 Flask before_request 钩子。
        返回 None 继续处理，返回响应中断请求。
        """
        # 公开路由跳过验证
        if request.path in self.config.public_routes:
            return None
        
        # 开发模式: 未配置 API Key 时跳过验证
        if self.config.api_key is None:
            return None
        
        # 获取认证头
        auth_header = request.headers.get('Authorization', '')
        if not auth_header:
            self.logger.warning('请求缺少认证头: {} {}', request.method, request.path)
            return jsonify({'error': 'Missing Authorization header'}), 401
        
        # 提取 API Key
        if auth_header.startswith('Bearer '):
            provided_key = auth_header[7:]
        else:
            provided_key = auth_header
        
        # 安全比较 (防止时序攻击)
        if not secrets.compare_digest(provided_key, self.config.api_key):
            self.logger.warning('API Key 无效: {} {}', request.method, request.path)
            return jsonify({'error': 'Invalid API key'}), 401
        
        return None