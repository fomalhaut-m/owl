# -*- coding: utf-8 -*-
"""
执行器 (manager/executor.py)

执行 Python 脚本。

关联:
- 依赖 Config, SecurityValidator
- 返回 ExecuteResult
- 被 MCPServer 使用

注意事项:
- 执行超时强制终止进程
- 验证脚本路径是否在允许目录内
- 需要验证所有参数
"""

import os
import sys
import subprocess

from ..core.config import Config
from ..core.validator import SecurityValidator
from ..model.models import ExecuteResult


class Executor:
    """脚本执行器 - 执行 Python 脚本"""
    
    def __init__(self, config: Config):
        self.config = config
        self.validator = SecurityValidator()
    
    def execute(self, script_path: str, args: list, timeout: int = 30, cwd: str = None) -> ExecuteResult:
        """执行脚本"""
        cmd = [sys.executable, script_path] + args
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=timeout, cwd=cwd)
        return ExecuteResult(
            returncode=result.returncode,
            stdout=result.stdout,
            stderr=result.stderr,
            success=result.returncode == 0
        )
    
    def execute_global(self, script: str, args: list = None, timeout: int = 30) -> tuple:
        """执行全局脚本"""
        args = args or []
        error = self.validator.validate_args(args, self.config.max_args_length)
        if error: return None, error
        if not script.endswith('.py'): return None, 'Script name must end with .py'
        
        script_path = os.path.join(self.config.scripts_dir, script)
        if not os.path.abspath(script_path).startswith(os.path.abspath(self.config.scripts_dir)):
            return None, 'Invalid script path'
        if not os.path.exists(script_path): return None, f'Script not found: {script}'
        
        timeout = min(timeout, self.config.max_timeout)
        result = self.execute(script_path, args, timeout, self.config.scripts_dir)
        return result, None
    
    def execute_user(self, user_id: str, script: str, args: list = None, timeout: int = 30) -> tuple:
        """执行用户脚本"""
        args = args or []
        error = self.validator.validate_user_id(user_id)
        if error: return None, error
        error = self.validator.validate_script_name(script)
        if error: return None, error
        error = self.validator.validate_args(args, self.config.max_args_length)
        if error: return None, error
        if not script.endswith('.py'): return None, 'Script name must end with .py'
        
        scripts_dir = os.path.join(self.config.users_dir, user_id, self.config.user_scripts_dir)
        if not os.path.exists(scripts_dir): os.makedirs(scripts_dir, exist_ok=True)
        
        script_path = os.path.join(scripts_dir, script)
        abs_script_path = os.path.abspath(script_path)
        abs_scripts_dir = os.path.abspath(scripts_dir)
        
        if not abs_script_path.startswith(abs_scripts_dir):
            return None, 'Script path outside user scripts directory'
        if not os.path.exists(abs_script_path): return None, f'Script not found: {script}'
        
        timeout = min(timeout, self.config.max_timeout)
        result = self.execute(script_path, args, timeout, scripts_dir)
        return result, None