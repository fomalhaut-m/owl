# -*- coding: utf-8 -*-
"""
管理器模块 (manager/script_manager.py)

管理全局脚本的增删改查。

关联:
- 依赖 Config (scripts_dir, max_script_size)
- 依赖 SecurityValidator
- 返回 ScriptInfo
- 被 MCPServer 使用

注意事项:
- 文件名必须以 .py 结尾
- 文件名不能包含路径穿越字符
- 文件大小不能超过 max_script_size
- 脚本路径必须验证在 scripts_dir 内

用法:
    from src.manager import ScriptManager
    from src.core import Config
    
    config = Config()
    mgr = ScriptManager(config)
    
    # 列出脚本
    scripts = mgr.list_scripts()
    
    # 创建脚本
    script, error = mgr.create_script('test.py', 'print("hello")')
    
    # 更新脚本
    script, error = mgr.update_script('test.py', 'print("world")')
    
    # 删除脚本
    success, error = mgr.delete_script('test.py')
    
    # 获取内容
    content, error = mgr.get_content('test.py')
"""

import os
from typing import Optional

from ..core.config import Config
from ..core.validator import SecurityValidator
from ..model.models import ScriptInfo


class ScriptManager:
    """
    全局脚本管理器
    
    管理全局可执行脚本的 CRUD 操作。
    脚本存放在 scripts_dir 目录下。
    """
    
    def __init__(self, config: Config):
        """
        初始化脚本管理器
        
        Args:
            config: Config 实例
        """
        self.config = config
        self.validator = SecurityValidator()
    
    def list_scripts(self):
        """
        列出所有全局脚本
        
        Returns:
            ScriptInfo 列表
        """
        scripts = []
        if not os.path.exists(self.config.scripts_dir):
            return scripts
        
        for filename in os.listdir(self.config.scripts_dir):
            if filename.endswith('.py'):
                filepath = os.path.join(self.config.scripts_dir, filename)
                scripts.append(ScriptInfo(
                    name=filename,
                    path=filepath,
                    size=os.path.getsize(filepath),
                    type='global'
                ))
        return scripts
    
    def create_script(self, name: str, content: str = '') -> tuple:
        """
        创建新脚本
        
        Args:
            name: 脚本文件名 (必须是 .py 结尾)
            content: 脚本内容
        
        Returns:
            (ScriptInfo, error): 成功返回 ScriptInfo，失败返回 error
        """
        error = self.validator.validate_script_name(name)
        if error:
            return None, error
        
        if not name.endswith('.py'):
            return None, 'Script name must end with .py'
        
        error = self.validator.validate_script_content(content, self.config.max_script_size)
        if error:
            return None, error
        
        script_path = os.path.join(self.config.scripts_dir, name)
        if os.path.exists(script_path):
            return None, f'Script already exists: {name}'
        
        with open(script_path, 'w', encoding='utf-8') as f:
            f.write(content)
        
        return ScriptInfo(name=name, path=script_path, size=len(content), type='global'), None
    
    def update_script(self, name: str, content: str) -> tuple:
        """
        更新脚本内容
        
        Args:
            name: 脚本文件名
            content: 新脚本内容
        
        Returns:
            (ScriptInfo, error)
        """
        error = self.validator.validate_script_name(name)
        if error:
            return None, error
        
        error = self.validator.validate_script_content(content, self.config.max_script_size)
        if error:
            return None, error
        
        script_path = os.path.join(self.config.scripts_dir, name)
        if not os.path.exists(script_path):
            return None, f'Script not found: {name}'
        
        with open(script_path, 'w', encoding='utf-8') as f:
            f.write(content)
        
        return ScriptInfo(name=name, path=script_path, size=len(content), type='global'), None
    
    def delete_script(self, name: str) -> tuple:
        """
        删除脚本
        
        Args:
            name: 脚本文件名
        
        Returns:
            (success, error)
        """
        error = self.validator.validate_script_name(name)
        if error:
            return False, error
        
        script_path = os.path.join(self.config.scripts_dir, name)
        if not os.path.exists(script_path):
            return False, f'Script not found: {name}'
        
        os.remove(script_path)
        return True, None
    
    def get_content(self, name: str) -> tuple:
        """
        获取脚本内容
        
        Args:
            name: 脚本文件名
        
        Returns:
            (content, error)
        """
        error = self.validator.validate_script_name(name)
        if error:
            return None, error
        
        script_path = os.path.join(self.config.scripts_dir, name)
        if not os.path.exists(script_path):
            return None, f'Script not found: {name}'
        
        with open(script_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        return content, None