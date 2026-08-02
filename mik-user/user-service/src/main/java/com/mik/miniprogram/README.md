# 小程序登录模块

## 功能说明

本模块实现了小程序端的登录功能，包括：

1. **小程序登录** - `POST /auth/login`

## 接口说明

### 1. 小程序登录

**请求**: `POST /auth/login`

**请求体**:
```json
{
  "code": "081abc..."
}
```

**返回（扁平，非信封）**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "openid": "oABC123...",
  "unionid": "oXYZ..."
}
```

**说明**:
- 前端通过 `wx.login()` 获取 `code`
- 后端调用微信接口获取 `openid` 和 `session_key`
- 根据 `openid` 查询或创建用户
- 生成 JWT token 返回给前端
- 用户信息存储到 Redis 中，兼容现有的认证逻辑

## 微信小程序配置

在 `application-dev.yml` 中配置微信小程序的 appid 和 secret：

```yaml
wx:
  appid: your_appid_here
  secret: your_secret_here
```

## 数据库表

- `wx_user` - 微信用户表，存储微信用户信息和关联的系统用户ID

## 认证流程

1. 前端调用 `wx.login()` 获取 `code`
2. 前端将 `code` 发送到后端 `/auth/login` 接口
3. 后端调用微信接口获取 `openid`
4. 后端根据 `openid` 查询或创建用户
5. 后端生成 JWT token 并返回给前端
6. 前端将 token 存储到本地
7. 后续请求在 Header 中携带 `Authorization: Bearer <token>`

## 注意事项

1. 登录接口不需要认证（已添加到白名单）
2. token 有效期为 7 天
3. 新用户会自动创建系统用户
4. 用户信息存储到 Redis 中，兼容现有的认证逻辑
