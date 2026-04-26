# -*- coding: utf-8 -*-
"""
用户脚本管理器 (manager/user_script_manager.py)

管理用户工作空间中的脚本。

关联:
- 依赖 Config (users_dir, user_scripts_dir, max_script_size)
- 依赖 SecurityValidator
- 返回 ScriptInfo (type='user')

注意事项:
- 每个用户的脚本存放在 users/<user_id>/scripts/ 目录下
- 用户目录不存在时会自动创建
"""

import os

from ..core.config import Config
from ..core.validator import SecurityValidator
from ..model.models import ScriptInfo


class UserScriptManager:
    """用户脚本管理器"""
    
    def __init__(self, config: Config):
        self.config = config
        self.validator = SecurityValidator()
    
    def _get_user_scripts_dir(self, user_id: str) -> str:
        return os.path.join(self.config.users_dir, user_id, self.config.user_scripts_dir)
    
    def _get_script_path(self, user_id: str, name: str) -> str:
        return os.path.join(self._get_user_scripts_dir(user_id), name)
    
    def list_scripts(self, user_id: str) -> tuple:
        error = self.validator.validate_user_id(user_id)
        if error: return [], error
        
        scripts_dir = self._get_user_scripts_dir(user_id)
        if not os.path.exists(scripts_dir): return [], None
        
        scripts = []
        for filename in os.listdir(scripts_dir):
            if filename.endswith('.py'):
                filepath = os.path.join(scripts_dir, filename)
                relative_path = os.path.join(user_id, self.config.user_scripts_dir, filename)
                scripts.append(ScriptInfo(
                    name=filename, path=relative_path,
                    size=os.path.getsize(filepath), type='user'
                ))
        return scripts, None
    
    def create_script(self, user_id: str, name: str, content: str = '') -> tuple:
        error = self.validator.validate_user_id(user_id)
        if error: return None, error
        error = self.validator.validate_script_name(name)
        if error: return None, error
        if not name.endswith('.py'): return None, 'Script name must end with .py'
        
        scripts_dir = self._get_user_scripts_dir(user_id)
        if not os.path.exists(scripts_dir): os.makedirs(scripts_dir, exist_ok=True)
        
        script_path = self._get_script_path(user_id, name)
        if os.path.exists(script_path): return None, f'Script already exists: {name}'
        
        with open(script_path, 'w', encoding='utf-8') as f: f.write(content)
        relative_path = os.path.join(user_id, self.config.user_scripts_dir, name)
        return ScriptInfo(name=name, path=relative_path, size=len(content), type='user'), None
    
    def update_script(self, user_id: str, name: str, content: str) -> tuple:
        error = self.validator.validate_user_id(user_id)
        if error: return None, error
        error = self.validator.validate_script_name(name)
        if error: return None, error
        
        script_path = self._get_script_path(user_id, name)
        if not os.path.exists(script_path): return None, f'Script not found: {name}'
        
        with open(script_path, 'w', encoding='utf-8') as f: f.write(content)
        relative_path = os.path.join(user_id, self.config.user_scripts_dir, name)
        return ScriptInfo(name=name, path=relative_path, size=len(content), type='user'), None
    
    def delete_script(self, user_id: str, name: str) -> tuple:
        error = self.validator.validate_user_id(user_id)
        if error: return False, error
        error = self.validator.validate_script_name(name)
        if error: return False, error
        
        script_path = self._get_script_path(user_id, name)
        if not os.path.exists(script_path): return False, f'Script not found: {name}'
        
        os.remove(script_path)
        return True, None
    
    def get_content(self, user_id: str, name: str) -> tuple:
        error = self.validator.validate_user_id(user_id)
        if error: return None, error
        error = self.validator.validate_script_name(name)
        if error: return None, error
        
        script_path = self._get_script_path(user_id, name)
        if not os.path.exists(script_path): return None, f'Script not found: {name}'
        
        with open(script_path, 'r', encoding='utf-8') as f: content = f.read()
        return content, None