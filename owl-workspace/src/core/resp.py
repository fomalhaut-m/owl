# -*- coding: utf-8 -*-
"""
响应模块 (core/resp.py)

为 API 提供统一的响应格式。
所有 REST 接口都返回相同格式的结果。

关联:
- 被所有路由处理器使用
- WorkspaceApi 各方法返回 Resp.success() 或 Resp.error()

响应格式:
{
    "code": 0,           # 0=成功, 其他值=错误码
    "message": "OK",      # 描述信息
    "data": {...}        # 返回的数据(可选)
}

注意事项:
- code 是应用级错误码，不是 HTTP 状态码
- HTTP 状态码由 Resp.error() 第二个返回值决定

用法:
    from src.core import Resp
    
    # 成功响应
    return Resp.success({'name': 'test'})
    return Resp.success({'name': 'test'}, '创建成功')
    
    # 错误响应
    return Resp.error(Resp.NOT_FOUND, '资源不存在')
    return Resp.error(400, '参数错误', {'field': 'name'})
"""

from flask import jsonify


class Resp:
    """
    统一响应格式类
    
    提供静态方法，不需要创建实例。
    
    错误码:
    - OK = 0: 成功
    - INVALID_PARAM = 400: 参数错误
    - NOT_FOUND = 404: 资源不存在
    - SERVER_ERROR = 500: 服务器内部错误
    - TIMEOUT = 504: 执行超时
    """
    
    # 成功
    OK = 0
    # 参数错误
    INVALID_PARAM = 400
    # 资源不存在
    NOT_FOUND = 404
    # 服务器错误
    SERVER_ERROR = 500
    # 超时
    TIMEOUT = 504
    
    @staticmethod
    def success(data=None, message='OK'):
        """
        返回成功响应
        
        Args:
            data: 要返回的数据，可以是任意类型
            message: 成功信息，默认 "OK"
        
        Returns:
            Flask JSON 响应对象
        """
        return jsonify({
            'success': True,
            'message': message,
            'data': data
        })
    
    @staticmethod
    def error(code, message, data=None):
        """
        返回错误响应
        
        Args:
            code: 应用错误码 (如 400, 404, 500)
            message: 错误描述信息
            data: 附加数据(可选)
        
        Returns:
            (响应对象, HTTP状态码) 元组
        """
        return jsonify({
            'success': False,
            'message': message,
            'error': message,
            'data': data
        }), code