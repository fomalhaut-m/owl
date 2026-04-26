# -*- coding: utf-8 -*-
"""
配置模块 (core/config.py)

管理应用程序的所有配置项。
配置优先级: 环境变量 > 默认值

关联:
- 被 Auth, Executor, ScriptManager, UserScriptManager, MCPServer 引用
- WorkspaceApi 需要创建 Config 实例

注意事项:
- WORKSPACE_ROOT 是必需的，必须确保目录存在
- SCRIPTS_DIR 如果不存在，启动时会自动创建

用法:
    from src.core import Config
    
    # 默认配置
    config = Config()
    
    # 自定义配置 (通过环境变量)
    import os
    os.environ['WORKSPACE_ROOT'] = '/custom/workspace'
    config = Config()
"""

import os
from typing import Optional


class Config:
    """配置类 - 存储应用配置项"""
    
    def __init__(self):
        # API 密钥 - 用于 API 认证
        # 设置方式: export OWL_API_KEY=your_key
        self.api_key: Optional[str] = os.environ.get('OWL_API_KEY')
        
        # 工作空间根目录 - 存放所有用户的数据
        # 默认: target/workspace
        self.workspace_root: str = self._get_workspace_root()
        
        # 用户目录 - workspace_root/users
        self.users_dir: str = os.path.join(self.workspace_root, 'users')
        
        # 全局脚本目录 - 存放全局可执行脚本
        # 默认: target/scripts
        self.scripts_dir: str = self._get_scripts_dir()
        
        # 用户工作空间中的脚本目录名
        self.user_scripts_dir: str = 'scripts'
        
        # 脚本执行最大超时时间(秒)
        self.max_timeout: int = 300
        
        # 命令行参数最大数量
        self.max_args_length: int = 10
        
        # 脚本文件最大大小 (1MB)
        self.max_script_size: int = 1024 * 1024
        
        # 无需认证的路由 (提高查询效率使用 set)
        self.public_routes: set = {'/health', '/api/key'}
    
    def _get_workspace_root(self) -> str:
        """
        获取工作空间根目录
        
        优先级:
        1. WORKSPACE_ROOT 环境变量
        2. WORKSPACE_DIR 环境变量 (兼容旧名称)
        3. 默认值 target/workspace
        """
        # 优先从环境变量读取
        if 'WORKSPACE_ROOT' in os.environ:
            return os.environ['WORKSPACE_ROOT']
        # 兼容旧名称
        if 'WORKSPACE_DIR' in os.environ:
            return os.environ['WORKSPACE_DIR']
        # 默认值: 项目根目录/target/workspace
        # __file__ = core/config.py, dirname = core, dirname = src, dirname = 项目根
        project_root = os.path.dirname(os.path.dirname(os.path.dirname(__file__)))
        return os.path.join(project_root, 'target', 'workspace')
    
    def _get_scripts_dir(self) -> str:
        """
        获取全局脚本目录
        
        优先级:
        1. SCRIPTS_DIR 环境变量
        2. 默认值 target/scripts
        """
        if 'SCRIPTS_DIR' in os.environ:
            return os.environ['SCRIPTS_DIR']
        project_root = os.path.dirname(os.path.dirname(os.path.dirname(__file__)))
        return os.path.join(project_root, 'target', 'scripts')