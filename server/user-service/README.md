# User Service 用户服务
## 功能描述
用户账号和个性化配置管理服务，负责所有用户相关的业务逻辑处理
## 核心功能
- 👤 **用户账号管理**
  - 邮箱+密码注册功能
  - 邮箱+密码登录验证登录功能
  - 用户信息修改、密码重置
- ⚙️ **AI模型配置管理**
  - 保存用户自定义的AI平台配置
  - 支持多平台API Key加密存储
  - 用户默认对话模型偏好设置
  - 模型配置的增删改查
- 🔑 **API Key安全管理**
  - 敏感信息加密存储
  - 访问权限控制
## 技术栈
- Java + Spring Boot
- MyBatis Plus
- PostgreSQL 数据库
- BCrypt 密码加密
## 服务端口
- 默认启动端口：9202

## 接口定义
- **用户账号管理接口**
  - 用户业务 - 注册接口: POST `/api/auth/register`
  - 用户业务 - 登录验证接口: POST `/api/auth/login`
  - 用户业务 - 信息更新接口: POST `/api/auth/update`   
  - 用户业务 - 密码重置接口: POST `/api/auth/reset-password`
  - 用户业务 - 邮箱验证接口: POST `/api/auth/verify-email`
  - 用户管理 - 用户管理接口: POST `/api/user/query`
  - Admin 管理接口: GET `/api/auth/admin`
- **权限管理接口**  
  - 用户权限更新接口: POST `/api/auth/permissions`
  - 用户权限查询接口: GET `/api/auth/permissions`
