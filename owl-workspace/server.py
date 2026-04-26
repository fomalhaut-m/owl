"""
Python 远程脚本执行服务
支持通过 HTTP API 远程调用 Python 脚本
支持多用户工作空间隔离
支持 API Key 身份验证
"""

import argparse
import os
import sys

from src.workspace_api import WorkspaceApi


def parse_args():
    """解析命令行参数"""
    parser = argparse.ArgumentParser(description='工作空间服务')
    parser.add_argument('--test', action='store_true', help='启用测试模式（apiKey = test）')
    parser.add_argument('--host', default='0.0.0.0', help='监听地址')
    parser.add_argument('--port', type=int, default=8000, help='监听端口')
    return parser.parse_args()


if __name__ == '__main__':
    args = parse_args()
    
    # 测试模式配置
    if args.test or os.environ.get('TEST_MODE') == '1':
        os.environ['OWL_API_KEY'] = 'test'
        print('[测试模式] API Key 已设置为 "test"')
    
    api = WorkspaceApi()
    api.run(host=args.host, port=args.port, debug=args.test)