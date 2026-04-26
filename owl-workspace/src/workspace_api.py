# -*- coding: utf-8 -*-
"""
工作空间 API 主类

提供 REST API 端点，支持用户脚本管理、脚本执行等功能。
使用统一的日志系统记录请求和响应信息。
"""

import os
import sys
import secrets
import time
from flask import Flask, request, jsonify

from .core import Config, Resp, SecurityValidator
from .core.logger import Logger, get_logger, log_banner, log_section
from .auth import Auth
from .manager import UserScriptManager, Executor
from .mcp import MCPServer
from .utils import get_dir_size, format_size


# 全局日志器
logger = get_logger('API')


class WorkspaceApi:
    
    def __init__(self):
        self.config = Config()
        self.app = Flask(__name__)
        
        # 初始化各模块
        self.auth = Auth(self.config)
        self.user_script_manager = UserScriptManager(self.config)
        self.executor = Executor(self.config)
        self.validator = SecurityValidator()
        self.mcp_server = MCPServer(self.config)
        
        # 注册路由和请求钩子
        self._register_routes()
        self._register_hooks()
    
    def _register_routes(self):
        """注册所有路由"""
        # 公开端点 (无需认证)
        self.app.add_url_rule('/health', view_func=self._health_check, methods=['GET'])
        self.app.add_url_rule('/api/key', view_func=self._generate_key, methods=['POST'])
        
        # 用户脚本操作
        self.app.add_url_rule('/execute-user', view_func=self._execute_user_script, methods=['POST'])
        
        self.app.add_url_rule('/scripts-user', view_func=self._list_user_scripts, methods=['GET'])
        self.app.add_url_rule('/scripts-user', view_func=self._create_user_script, methods=['POST'])
        self.app.add_url_rule('/scripts-user', view_func=self._update_user_script, methods=['PUT'])
        self.app.add_url_rule('/scripts-user', view_func=self._delete_user_script, methods=['DELETE'])
        self.app.add_url_rule('/scripts-user/content', view_func=self._get_user_script_content, methods=['GET'])
        
        self.app.add_url_rule('/user-space', view_func=self._get_user_space, methods=['GET'])
        self.app.add_url_rule('/user-space', view_func=self._create_user_space, methods=['POST'])
        
        # MCP 协议路由
        self.app.add_url_rule('/mcp', view_func=self._mcp_rpc, methods=['POST'])
    
    def _register_hooks(self):
        """注册请求/响应钩子"""
        @self.app.before_request
        def before_request():
            """请求开始前记录"""
            request._start_time = time.time()
            logger.info('收到请求: {} {}', request.method, request.path)
        
        @self.app.after_request
        def after_request(response):
            """请求完成后记录"""
            duration_ms = (time.time() - request._start_time) * 1000
            logger.request(request.method, request.path, response.status_code, duration_ms)
            return response
    
    # ==================== 公开端点 ====================
    
    def _health_check(self):
        """健康检查端点"""
        logger.debug('健康检查请求')
        return jsonify({
            'status': 'ok',
            'authenticated': self.config.api_key is not None,
            'python_version': sys.version[:5]
        })
    
    def _generate_key(self):
        """生成 API Key 端点"""
        if self.config.api_key is not None:
            logger.warning('尝试生成 API Key，但已存在')
            return jsonify({'error': 'API key already configured'}), 403
        
        new_key = secrets.token_urlsafe(32)
        config_path = os.path.join(os.path.dirname(__file__), '..', '.api_key')
        with open(config_path, 'w') as f:
            f.write(new_key)
        
        logger.success('生成新的 API Key')
        return jsonify({
            'success': True,
            'api_key': new_key,
            'message': 'API key generated. Set OWL_API_KEY env var for production.'
        }), 201
    
    # ==================== 用户脚本执行 ====================
    
    def _execute_user_script(self):
        """执行用户脚本"""
        start_time = time.time()
        data = request.get_json()
        
        if not data:
            logger.warning('用户脚本执行失败: 请求体为空')
            return Resp.error(Resp.INVALID_PARAM, 'Missing request body')
        
        user_id = data.get('userId')
        script = data.get('script')
        
        if not user_id:
            logger.warning('用户脚本执行失败: 缺少 userId')
            return Resp.error(Resp.INVALID_PARAM, 'Missing userId')
        if not script:
            logger.warning('用户脚本执行失败: 缺少 script')
            return Resp.error(Resp.INVALID_PARAM, 'Missing script name')
        
        args = data.get('args', [])
        timeout = data.get('timeout', 30)
        
        logger.info('执行用户脚本: userId={}, script={}, args={}, timeout={}', 
                    user_id, script, args, timeout)
        
        result, error = self.executor.execute_user(user_id, script, args, timeout)
        
        if error:
            if 'not found' in error.lower():
                logger.error('用户脚本执行失败 [404]: {}', error)
                return Resp.error(Resp.NOT_FOUND, error)
            logger.error('用户脚本执行失败 [400]: {}', error)
            return Resp.error(Resp.INVALID_PARAM, error)
        
        response = {'returncode': result.returncode, 'stdout': result.stdout, 'stderr': result.stderr}
        
        elapsed = (time.time() - start_time) * 1000
        
        if result.success:
            logger.success('用户脚本执行成功 [{}ms]: returncode={}, stdout={}', 
                           elapsed, result.returncode, result.stdout[:100] if result.stdout else '(empty)')
            return Resp.success(response, 'Script executed successfully')
        
        logger.warning('用户脚本执行完成但有错误: returncode={}', result.returncode)
        return Resp.error(Resp.SERVER_ERROR, f'Script execution failed with code {result.returncode}', response)
    
    # ==================== 用户脚本管理 ====================
    
    def _list_user_scripts(self):
        """列出用户脚本"""
        user_id = request.args.get('userId')
        
        if not user_id:
            logger.warning('列出脚本失败: 缺少 userId')
            return Resp.error(Resp.INVALID_PARAM, 'Missing userId')
        
        logger.debug('列出用户脚本: userId={}', user_id)
        
        scripts, error = self.user_script_manager.list_scripts(user_id)
        
        if error:
            logger.error('列出脚本失败: {}', error)
            return Resp.error(Resp.INVALID_PARAM, error)
        
        logger.success('列出脚本成功: 找到 {} 个脚本', len(scripts))
        return Resp.success([s.__dict__ for s in scripts])
    
    def _create_user_script(self):
        """创建用户脚本"""
        data = request.get_json()
        
        if not data:
            logger.warning('创建脚本失败: 请求体为空')
            return Resp.error(Resp.INVALID_PARAM, 'Missing request body')
        
        user_id = data.get('userId')
        name = data.get('name')
        
        if not user_id:
            logger.warning('创建脚本失败: 缺少 userId')
            return Resp.error(Resp.INVALID_PARAM, 'Missing userId')
        if not name:
            logger.warning('创建脚本失败: 缺少 name')
            return Resp.error(Resp.INVALID_PARAM, 'Missing script name')
        
        content = data.get('content', '')
        content_size = len(content)
        
        logger.info('创建脚本: userId={}, name={}, size={} bytes', user_id, name, content_size)
        
        script, error = self.user_script_manager.create_script(user_id, name, content)
        
        if error:
            logger.error('创建脚本失败: {}', error)
            return Resp.error(Resp.INVALID_PARAM, error)
        
        logger.success('脚本创建成功: {} -> {}', user_id, name)
        return Resp.success(script.__dict__, 'Script created')
    
    def _update_user_script(self):
        """更新用户脚本"""
        data = request.get_json()
        
        if not data:
            logger.warning('更新脚本失败: 请求体为空')
            return Resp.error(Resp.INVALID_PARAM, 'Missing request body')
        
        user_id = data.get('userId')
        name = data.get('name')
        content = data.get('content')
        
        if not user_id:
            logger.warning('更新脚本失败: 缺少 userId')
            return Resp.error(Resp.INVALID_PARAM, 'Missing userId')
        if not name:
            logger.warning('更新脚本失败: 缺少 name')
            return Resp.error(Resp.INVALID_PARAM, 'Missing script name')
        
        content_size = len(content) if content else 0
        
        logger.info('更新脚本: userId={}, name={}, size={} bytes', user_id, name, content_size)
        
        script, error = self.user_script_manager.update_script(user_id, name, content)
        
        if error:
            if 'not found' in error.lower():
                logger.error('更新脚本失败 [404]: {}', error)
                return Resp.error(Resp.NOT_FOUND, error)
            logger.error('更新脚本失败: {}', error)
            return Resp.error(Resp.INVALID_PARAM, error)
        
        logger.success('脚本更新成功: {} -> {}', user_id, name)
        return Resp.success(script.__dict__, 'Script updated')
    
    def _delete_user_script(self):
        """删除用户脚本"""
        user_id = request.args.get('userId')
        name = request.args.get('name')
        
        if not user_id:
            logger.warning('删除脚本失败: 缺少 userId')
            return Resp.error(Resp.INVALID_PARAM, 'Missing userId')
        if not name:
            logger.warning('删除脚本失败: 缺少 name')
            return Resp.error(Resp.INVALID_PARAM, 'Missing script name')
        
        logger.info('删除脚本: userId={}, name={}', user_id, name)
        
        success, error = self.user_script_manager.delete_script(user_id, name)
        
        if error:
            if 'not found' in error.lower():
                logger.error('删除脚本失败 [404]: {}', error)
                return Resp.error(Resp.NOT_FOUND, error)
            logger.error('删除脚本失败: {}', error)
            return Resp.error(Resp.INVALID_PARAM, error)
        
        logger.success('脚本删除成功: {} -> {}', user_id, name)
        return Resp.success({'name': name, 'userId': user_id}, 'Script deleted')
    
    def _get_user_script_content(self):
        """获取用户脚本内容"""
        user_id = request.args.get('userId')
        name = request.args.get('name')
        
        if not user_id:
            logger.warning('获取脚本内容失败: 缺少 userId')
            return Resp.error(Resp.INVALID_PARAM, 'Missing userId')
        if not name:
            logger.warning('获取脚本内容失败: 缺少 name')
            return Resp.error(Resp.INVALID_PARAM, 'Missing script name')
        
        logger.debug('获取脚本内容: userId={}, name={}', user_id, name)
        
        content, error = self.user_script_manager.get_content(user_id, name)
        
        if error:
            if 'not found' in error.lower():
                logger.error('获取脚本内容失败 [404]: {}', error)
                return Resp.error(Resp.NOT_FOUND, error)
            logger.error('获取脚本内容失败: {}', error)
            return Resp.error(Resp.INVALID_PARAM, error)
        
        logger.success('获取脚本内容成功: {} -> {}, size={} bytes', user_id, name, len(content))
        return Resp.success({'name': name, 'userId': user_id, 'content': content, 'size': len(content)})
    
    # ==================== 用户空间管理 ====================
    
    def _get_user_space(self):
        """获取用户空间信息"""
        user_id = request.args.get('userId')
        
        if not user_id:
            logger.warning('获取空间信息失败: 缺少 userId')
            return Resp.error(Resp.INVALID_PARAM, 'Missing userId')
        
        error = self.validator.validate_user_id(user_id)
        if error:
            logger.error('获取空间信息失败: {}', error)
            return Resp.error(Resp.INVALID_PARAM, error)
        
        logger.debug('获取用户空间信息: userId={}', user_id)
        
        workspace_dir = os.path.join(self.config.users_dir, user_id)
        
        if not os.path.exists(workspace_dir):
            logger.info('用户空间不存在: {}', user_id)
            return Resp.success({
                'userId': user_id,
                'workspace_path': workspace_dir,
                'exists': False,
                'size_bytes': 0,
                'size_formatted': '0 B'
            })
        
        size_bytes = get_dir_size(workspace_dir)
        logger.success('获取用户空间信息成功: {}, size={}', user_id, format_size(size_bytes))
        return Resp.success({
            'userId': user_id,
            'workspace_path': workspace_dir,
            'exists': True,
            'size_bytes': size_bytes,
            'size_formatted': format_size(size_bytes)
        })
    
    def _create_user_space(self):
        """创建用户空间"""
        data = request.get_json()
        
        if not data:
            logger.warning('创建空间失败: 请求体为空')
            return Resp.error(Resp.INVALID_PARAM, 'Missing request body')
        
        user_id = data.get('userId')
        
        if not user_id:
            logger.warning('创建空间失败: 缺少 userId')
            return Resp.error(Resp.INVALID_PARAM, 'Missing userId')
        
        error = self.validator.validate_user_id(user_id)
        if error:
            logger.error('创建空间失败: {}', error)
            return Resp.error(Resp.INVALID_PARAM, error)
        
        logger.info('创建用户空间: userId={}', user_id)
        
        workspace_dir = os.path.join(self.config.users_dir, user_id)
        scripts_dir = os.path.join(workspace_dir, self.config.user_scripts_dir)
        data_dir = os.path.join(workspace_dir, 'workspace')
        
        if os.path.exists(workspace_dir):
            logger.warning('用户空间已存在: {}', user_id)
            return Resp.success({
                'userId': user_id,
                'workspace_path': workspace_dir,
                'created': False,
                'message': 'User workspace already exists'
            })
        
        os.makedirs(scripts_dir, exist_ok=True)
        os.makedirs(data_dir, exist_ok=True)
        
        logger.success('用户空间创建成功: {}, dirs=[{}, {}, {}]', 
                       user_id, workspace_dir, scripts_dir, data_dir)
        return Resp.success({
            'userId': user_id,
            'workspace_path': workspace_dir,
            'created': True,
            'directories': [workspace_dir, scripts_dir, data_dir]
        }, 'User workspace created'), 201
    
    # ==================== MCP 协议 ====================
    
    def _mcp_rpc(self):
        """MCP JSON-RPC 端点"""
        data = request.get_json()
        
        if not data:
            logger.warning('MCP 请求无效: 请求体为空')
            return jsonify({"jsonrpc": "2.0", "error": {"code": -32600, "message": "Invalid Request"}}), 400
        
        logger.debug('收到 MCP 请求: {}', data.get('method', 'unknown'))
        
        result = self.mcp_server.handle_jsonrpc(data)
        
        if 'error' in result:
            logger.error('MCP 请求失败: {}', result['error'])
        else:
            logger.success('MCP 请求成功: {}', result.get('result', {}).get('name', 'ok'))
        
        return jsonify(result)
    
    # ==================== 服务启动 ====================
    
    def run(self, **kwargs):
        """启动服务"""
        # 确保目录存在
        os.makedirs(self.config.scripts_dir, exist_ok=True)
        os.makedirs(self.config.users_dir, exist_ok=True)
        
        # 打印启动 Banner
        print(file=sys.stderr)
        log_banner('OWL WORKSPACE API', 'Python Flask Server')
        
        log_section('配置信息')
        print(f"   工作空间: {self.config.workspace_root}")
        print(f"   用户目录:  {self.config.users_dir}")
        print(f"   API Key:   {'已配置' if self.config.api_key else '未配置 (开发模式)'}")
        
        log_section('路由列表')
        print(f"   {'方法':<8} {'路径':<40} {'说明'}")
        print(f"   {'-'*8} {'-'*40} {'-'*20}")
        
        routes = [
            ('GET',  '/health',              '健康检查'),
            ('POST', '/api/key',             '生成 API Key'),
            ('POST', '/execute-user',        '执行用户脚本'),
            ('GET',  '/scripts-user',        '列出用户脚本'),
            ('POST', '/scripts-user',        '创建用户脚本'),
            ('PUT',  '/scripts-user',        '更新用户脚本'),
            ('DELETE','/scripts-user',       '删除用户脚本'),
            ('GET',  '/scripts-user/content','获取脚本内容'),
            ('GET',  '/user-space',          '获取用户空间信息'),
            ('POST', '/user-space',          '创建用户空间'),
            ('POST', '/mcp',                 'MCP JSON-RPC'),
        ]
        
        for method, path, desc in routes:
            print(f"   {method:<8} {path:<40} {desc}")
        
        print(file=sys.stderr)
        
        # 提取启动参数
        host = kwargs.pop('host', '0.0.0.0')
        port = kwargs.pop('port', 8000)
        debug = kwargs.pop('debug', False)
        
        log_section('服务启动')
        print(f"   监听地址: http://{host}:{port}")
        print(f"   调试模式: {'开启' if debug else '关闭'}")
        print(file=sys.stderr)
        
        logger.success('服务已启动，等待请求...')
        
        self.app.run(host=host, port=port, debug=debug)