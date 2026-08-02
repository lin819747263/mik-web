# 小程序工单模块

## 功能说明

本模块实现了小程序端的工单管理功能，包括：

1. **创建工单** - `POST /orders`
2. **查询工单列表** - `GET /orders`
3. **查询工单详情** - `GET /orders/{id}`
4. **评价工单** - `POST /orders/{id}/rate`

## 接口说明

### 1. 创建工单

**请求**: `POST /orders`

**请求体**:
```json
{
  "categoryId": "light",
  "categoryName": "路灯不亮",
  "subName": "整条街不亮",
  "desc": "一整排路灯连续三晚不亮",
  "address": "山阴县岱岳镇广武街与永康路交叉口东200米",
  "latitude": 39.53,
  "longitude": 112.46,
  "gridName": "岱岳镇 · 城内社区网格",
  "gridCode": "140621001001",
  "photos": ["https://cdn.xxx/1.jpg"],
  "urgent": false,
  "anonymous": false,
  "phone": "13800138000",
  "dept": "城市照明所"
}
```

**返回**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "id": "SY202608011234",
    "status": "pending",
    "createdAt": "2026-08-01 15:30",
    "timeline": [
      {
        "t": "2026-08-01 15:30",
        "title": "市民提交",
        "desc": "诉求已进入12345受理池，等待话务中心分派",
        "status": "pending"
      }
    ]
  }
}
```

### 2. 查询工单列表

**请求**: `GET /orders?status=all`

**Query参数**:
- `status`: 工单状态过滤
  - `all` - 全部工单
  - `doing` - 处理中（pending/accepted/dispatched/processing）
  - `finished` - 已办结（done/rated）
  - 具体状态值 - 精确匹配

**返回**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": [
    {
      "id": "SY202608011234",
      "categoryName": "路灯不亮",
      "address": "山阴县岱岳镇广武街...",
      "dept": "城市照明所",
      "status": "processing",
      "createdAt": "2026-08-01 15:30",
      "timeline": [...]
    }
  ]
}
```

### 3. 查询工单详情

**请求**: `GET /orders/{id}`

**路径参数**:
- `id`: 工单号（如 SY202608011234）

**返回**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "id": "SY202608011234",
    "categoryId": "light",
    "categoryName": "路灯不亮",
    "subName": "整条街不亮",
    "desc": "一整排路灯连续三晚不亮...",
    "address": "山阴县岱岳镇广武街与永康路交叉口东200米",
    "latitude": 39.53,
    "longitude": 112.46,
    "gridName": "岱岳镇 · 城内社区网格",
    "gridCode": "140621001001",
    "photos": ["https://cdn.xxx/1.jpg"],
    "urgent": false,
    "anonymous": false,
    "phone": "138********",
    "dept": "城市照明所",
    "status": "processing",
    "score": 0,
    "comment": "",
    "createdAt": "2026-08-01 15:30",
    "timeline": [
      {"t": "2026-08-01 15:30", "title": "市民提交", "desc": "...", "status": "pending"},
      {"t": "2026-08-01 15:45", "title": "话务中心受理", "desc": "...", "status": "accepted"}
    ]
  }
}
```

### 4. 评价工单

**请求**: `POST /orders/{id}/rate`

**路径参数**:
- `id`: 工单号

**请求体**:
```json
{
  "score": 5,
  "comment": "处理很快，点赞"
}
```

**返回**:
```json
{
  "code": 0,
  "msg": "ok",
  "data": {
    "id": "SY202608011234",
    "status": "rated",
    "score": 5,
    "comment": "处理很快，点赞",
    "timeline": [
      ...,
      {
        "t": "2026-08-01 18:00",
        "title": "市民评价",
        "desc": "5星 处理很快，点赞",
        "status": "rated"
      }
    ]
  }
}
```

## 工单状态机

```
pending(待受理) → accepted(已受理) → dispatched(已派单) → processing(处理中) → done(已办结) → rated(已评价)
                                                                          ↘ rejected(已退回，可选)
```

## 数据库表

- `order` - 工单主表
- `order_timeline` - 工单时间线表

## 注意事项

1. 所有接口都需要登录认证（Authorization: Bearer token）
2. 用户只能查看和操作自己的工单
3. 评价只能在工单状态为"done"时进行
4. 评分范围为1-5
