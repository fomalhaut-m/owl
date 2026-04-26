# -*- coding: utf-8 -*-
"""
数据模型 (model/models.py)

使用 dataclass 定义数据结构，比普通类更简洁。

关联:
- 被 ScriptManager, UserScriptManager 使用
- 用于在模块间传递数据

用法:
    from src.model import ScriptInfo, ExecuteResult
    
    # 创建实例
    script = ScriptInfo(name='test.py', path='/path/to/test.py', size=1024, type='global')
    
    # 访问属性
    print(script.name)  # 'test.py'
    print(script.type)  # 'global'
    
    # 转换为字典
    print(script.__dict__)  # {'name': 'test.py', ...}
"""

from dataclasses import dataclass


@dataclass
class ScriptInfo:
    """
    脚本信息
    
    属性:
    - name: 脚本文件名
    - path: 完整路径
    - size: 文件大小(字节)
    - type: 类型 ('global' 全局 或 'user' 用户)
    """
    name: str
    path: str
    size: int
    type: str


@dataclass
class ExecuteResult:
    """
    脚本执行结果
    
    属性:
    - returncode: 进程退出码 (0=成功)
    - stdout: 标准输出
    - stderr: 错误输出
    - success: 是否成功 (returncode == 0)
    """
    returncode: int
    stdout: str
    stderr: str
    success: bool