# -*- coding: utf-8 -*-
"""
MCP 协议 (mcp/mcp_protocol.py)

MCP (Model Context Protocol) 协议定义。

关联:
- 被 MCPServer 使用
- JSON-RPC 2.0 标准

注意事项:
- 使用静态方法，不需要实例化
- 错误码参照 JSON-RPC 2.0 标准
"""

from typing import Any, Optional
from dataclasses import dataclass, field


@dataclass
class MCPProtocol:
    """MCP 协议版本"""
    major: int = 2024
    minor: int = 11
    patch: int = 5
    
    def __str__(self):
        return f"{self.major}-{self.minor:02d}-{self.patch:02d}"


@dataclass
class MCPServerInfo:
    name: str = "owl-workspace"
    version: str = "1.0.0"


@dataclass
class MCPCapabilities:
    tools: dict = field(default_factory=dict)
    resources: dict = field(default_factory=dict)
    prompts: dict = field(default_factory=dict)
    logging: dict = field(default_factory=dict)


@dataclass
class MCPTool:
    name: str
    description: str
    input_schema: dict


@dataclass
class MCPToolResult:
    content: list = field(default_factory=list)
    
    def to_json(self):
        return {"content": self.content}


class MCPToolRegistry:
    """工具注册表"""
    
    def __init__(self):
        self._tools: dict = {}
        self._handlers: dict = {}
    
    def register(self, tool: MCPTool, handler):
        self._tools[tool.name] = tool
        self._handlers[tool.name] = handler
    
    def get_tool(self, name: str):
        return self._tools.get(name)
    
    def list_tools(self):
        return list(self._tools.values())
    
    def call_tool(self, name: str, arguments: dict):
        handler = self._handlers.get(name)
        if not handler: raise ValueError(f"Tool not found: {name}")
        return handler.execute(arguments)


class MCPError:
    """MCP 错误码 (JSON-RPC 2.0 标准)"""
    ParseError = -32700
    InvalidRequest = -32600
    MethodNotFound = -32601
    InvalidParams = -32602
    InternalError = -32603


def create_jsonrpc_response(id: Any, result: dict = None, error: dict = None):
    response = {"jsonrpc": "2.0", "id": id}
    if error: response["error"] = error
    else: response["result"] = result
    return response


def create_jsonrpc_error(code: int, message: str, id: Any = None):
    return create_jsonrpc_response(id, error={"code": code, "message": message})


def create_text_content(text: str):
    """创建 MCP 文本内容块"""
    return {"type": "text", "text": text}