# -*- coding: utf-8 -*-
"""
验证模块 (core/validator.py)

负责验证用户输入的安全性，防止恶意攻击。
这是应用的第一道防线。

关联:
- 被 ScriptManager, UserScriptManager, Executor 使用
- 每个管理器在处理请求前调用验证方法

注意事项:
- 验证失败返回错误信息字符串，成功返回 None
- 不要在错误信息中泄露敏感路径信息
- 必须同时验证 user_id 和 script_name

用法:
    from src.core import SecurityValidator
    
    # 验证用户ID
    error = SecurityValidator.validate_user_id('user123')
    if error:
        return Resp.error(Resp.INVALID_PARAM, error)
    
    # 验证脚本名
    error = SecurityValidator.validate_script_name('test.py')
    if error:
        return Resp.error(Resp.INVALID_PARAM, error)
    
    # 验证参数
    error = SecurityValidator.validate_args(['arg1', 'arg2'], 10)
    if error:
        return Resp.error(Resp.INVALID_PARAM, error)
"""

from typing import Optional


class SecurityValidator:
    """
    安全验证类
    
    提供静态验证方法，不需要创建实例。
    所有方法都是线程安全的。
    """
    
    @staticmethod
    def validate_user_id(user_id: str) -> Optional[str]:
        """
        验证用户ID
        
        检查:
        - 不能为空
        - 不能包含路径穿越字符 (.., /, \\)
        
        Args:
            user_id: 用户ID字符串
        
        Returns:
            None: 验证通过
            str: 错误信息
        """
        if not user_id:
            return 'Missing userId'
        if '..' in user_id:
            return 'Invalid userId'
        if '/' in user_id:
            return 'Invalid userId'
        if '\\' in user_id:
            return 'Invalid userId'
        return None
    
    @staticmethod
    def validate_script_name(script_name: str) -> Optional[str]:
        """
        验证脚本名
        
        防止通过脚本名进行路径穿越攻击
        
        Args:
            script_name: 脚本文件名
        
        Returns:
            None: 验证通过
            str: 错误信息
        """
        if not script_name:
            return 'Missing script name'
        if '..' in script_name or '/' in script_name or '\\' in script_name:
            return 'Invalid script name'
        return None
    
    @staticmethod
    def validate_args(args: list, max_length: int) -> Optional[str]:
        """
        验证命令行参数
        
        防止通过参数进行注入攻击
        
        Args:
            args: 参数列表
            max_length: 最大参数数量
        
        Returns:
            None: 验证通过
            str: 错误信息
        """
        if not isinstance(args, list):
            return 'args must be an array'
        if len(args) > max_length:
            return f'Too many arguments, max {max_length}'
        for arg in args:
            if not isinstance(arg, str):
                return 'All arguments must be strings'
        return None
    
    @staticmethod
    def validate_script_content(content: str, max_size: int) -> Optional[str]:
        """
        验证脚本内容
        
        防止过大的脚本内容导致内存问题
        
        Args:
            content: 脚本内容
            max_size: 最大字节数
        
        Returns:
            None: 验证通过
            str: 错误信息
        """
        if content is None:
            return 'Missing content'
        if len(content) > max_size:
            return f'Script content too large, max {max_size} bytes'
        return None