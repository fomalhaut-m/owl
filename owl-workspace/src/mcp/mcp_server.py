# -*- coding: utf-8 -*-
"""
MCP 服务器 (mcp/mcp_server.py)

提供 MCP 协议兼容的工具接口。

关联:
- 使用 manager 模块执行脚本
- workspace_api.py 中通过 /mcp 路由调用

注意事项:
- 使用 JSON-RPC 2.0 格式
- 必须在 tools/list 后才能调用 tools/call
- 提供 execute_script 和 list_scripts 两个工具
"""

import json

from ..core.config import Config
from ..manager.executor import Executor
from ..manager.script_manager import ScriptManager
from ..manager.user_script_manager import UserScriptManager
from .mcp_protocol import (
    MCPProtocol, MCPServerInfo, MCPCapabilities,
    MCPTool, MCPToolResult, MCPToolRegistry,
    MCPError, create_jsonrpc_response, create_jsonrpc_error,
    create_text_content
)


class MCPToolHandler:
    def __init__(self, executor: Executor): self.executor = executor
    def execute(self, arguments: dict) -> MCPToolResult:
        script = arguments.get('script')
        args = arguments.get('args', [])
        timeout = arguments.get('timeout', 30)
        user_id = arguments.get('userId')
        
        if user_id: result, error = self.executor.execute_user(user_id, script, args, timeout)
        else: result, error = self.executor.execute_global(script, args, timeout)
        
        if error: return MCPToolResult(content=[create_text_content(f"Error: {error}")])
        
        output = result.stdout
        if result.stderr: output += f"\nStderr: {result.stderr}"
        return MCPToolResult(content=[create_text_content(output)])


class ListScriptsHandler:
    def __init__(self, script_mgr: ScriptManager, user_script_mgr: UserScriptManager):
        self.script_mgr = script_mgr
        self.user_script_mgr = user_script_mgr
    def execute(self, arguments: dict) -> MCPToolResult:
        user_id = arguments.get('userId')
        if user_id: scripts, _ = self.user_script_mgr.list_scripts(user_id)
        else: scripts = self.script_mgr.list_scripts()
        content = json.dumps([s.__dict__ for s in scripts], indent=2, ensure_ascii=False)
        return MCPToolResult(content=[create_text_content(content)])


class MCPServer:
    def __init__(self, config: Config):
        self.config = config
        self.protocol = MCPProtocol()
        self.server_info = MCPServerInfo()
        self.capabilities = MCPCapabilities(tools={}, resources={}, prompts={}, logging={})
        
        executor = Executor(config)
        script_mgr = ScriptManager(config)
        user_script_mgr = UserScriptManager(config)
        
        self.tool_registry = MCPToolRegistry()
        self._register_tools(executor, script_mgr, user_script_mgr)
    
    def _register_tools(self, executor, script_mgr, user_script_mgr):
        self.tool_registry.register(
            MCPTool(name="execute_script", description="Execute a Python script",
                    input_schema={"type": "object", "properties": {
                        "script": {"type": "string"}, "args": {"type": "array"},
                        "timeout": {"type": "integer"}, "userId": {"type": "string"}
                    }, "required": ["script"]}),
            MCPToolHandler(executor))
        
        self.tool_registry.register(
            MCPTool(name="list_scripts", description="List available scripts",
                    input_schema={"type": "object", "properties": {"userId": {"type": "string"}}}),
            ListScriptsHandler(script_mgr, user_script_mgr))
    
    def handle_jsonrpc(self, data: dict) -> dict:
        method = data.get('method')
        params = data.get('params', {})
        req_id = data.get('id')
        
        handlers = {'initialize': self._handle_initialize, 'tools/list': self._handle_tools_list,
                    'tools/call': self._handle_tools_call, 'notifications/initialized': self._handle_initialized}
        
        handler = handlers.get(method)
        if not handler: return create_jsonrpc_error(MCPError.MethodNotFound, f"Method not found: {method}", req_id)
        try: return handler(params, req_id)
        except Exception as e: return create_jsonrpc_error(MCPError.InternalError, str(e), req_id)
    
    def _handle_initialize(self, params: dict, req_id):
        return create_jsonrpc_response(req_id, {"protocolVersion": str(self.protocol),
            "capabilities": self.capabilities.__dict__, "serverInfo": self.server_info.__dict__, "instructions": "Owl Workspace MCP Server"})
    
    def _handle_initialized(self, params: dict, req_id): return create_jsonrpc_response(req_id, {})
    
    def _handle_tools_list(self, params: dict, req_id):
        tools = [{"name": t.name, "description": t.description, "inputSchema": t.input_schema}
                for t in self.tool_registry.list_tools()]
        return create_jsonrpc_response(req_id, {"tools": tools})
    
    def _handle_tools_call(self, params: dict, req_id):
        try:
            result = self.tool_registry.call_tool(params.get('name'), params.get('arguments', {}))
            return create_jsonrpc_response(req_id, result.to_json())
        except ValueError as e: return create_jsonrpc_error(MCPError.MethodNotFound, str(e), req_id)
        except Exception as e: return create_jsonrpc_error(MCPError.InternalError, str(e), req_id)