* 2026-05-27


## 套餐管理接口文档

---

### 1. 新增套餐

#### 1.1 接口说明

套餐管理 - 新增套餐

#### 1.2 请求信息

| 项目 | 内容 |
|:---|:---|
| 请求方式 | `POST` |
| 请求路径 | `/admin/setmeal` |
| Content-Type | `application/json` |

#### 1.3 请求头 (Headers)

| 参数名 | 必填 | 类型 | 说明 |
|:---|:---|:---|:---|
| token | 是 | string | JWT Token |
| Content-Type | 是 | string | `application/json` |

#### 1.4 请求参数 (Body)

| 参数名 | 必填 | 类型 | 示例值 | 说明 |
|:---|:---|:---|:---|:---|
| name | 是 | string | `KFC经典单人套餐` | 套餐名称，唯一 |
| categoryId | 是 | long | `99` | 套餐分类ID |
| price | 是 | number | `28.9` | 套餐价格 |
| image | 是 | string | `https://xxx.png` | 套餐图片URL |
| description | 否 | string | `啊，真香` | 套餐描述 |
| setmealDishes | 是 | array | | 套餐菜品列表 |
| ├─ dishId | 是 | long | `101` | 菜品ID |
| ├─ name | 是 | string | `香辣鸡腿堡` | 菜品名称（冗余） |
| ├─ price | 是 | number | `15.0` | 菜品单价（冗余） |
| └─ copies | 是 | int | `1` | 菜品份数，最低1份 |

**请求示例：**
```
json
{
"name": "KFC经典单人套餐",
"categoryId": 99,
"price": 28.9,
"image": "https://xxx.png",
"description": "啊，真香",
"setmealDishes": [
{
"dishId": 101,
"name": "香辣鸡腿堡",
"price": 15.0,
"copies": 1
},
{
"dishId": 102,
"name": "中薯条",
"price": 8.0,
"copies": 1
},
{
"dishId": 103,
"name": "中可乐",
"price": 5.9,
"copies": 1
}
]
}
```
#### 1.5 响应信息

##### 响应格式
`application/json`

##### 响应参数

| 参数名 | 类型 | 说明 |
|:---|:---|:---|
| code | integer | 状态码：1-成功，0-失败 |
| msg | string | 提示信息 |
| data | object | 响应数据（成功时为空） |

**响应示例：**

- 成功响应
```
json
{
"code": 1,
"msg": "操作成功",
"data": null
}
```
- 失败响应（套餐名称已存在）
```
json
{
"code": 0,
"msg": "套餐名称已存在",
"data": null
}
```
---

### 2. 套餐分页查询

#### 2.1 接口说明

套餐管理 - 分页查询套餐列表，支持按名称、分类、状态筛选

#### 2.2 请求信息

| 项目 | 内容 |
|:---|:---|
| 请求方式 | `GET` |
| 请求路径 | `/admin/setmeal/page` |
| Content-Type | `application/x-www-form-urlencoded` |

#### 2.3 请求头 (Headers)

| 参数名 | 必填 | 类型 | 说明 |
|:---|:---|:---|:---|
| token | 是 | string | JWT Token |

#### 2.4 请求参数 (Query)

| 参数名 | 必填 | 类型 | 示例值 | 说明 |
|:---|:---|:---|:---|:---|
| page | 是 | int | `1` | 页码 |
| pageSize | 是 | int | `10` | 每页记录数 |
| name | 否 | string | `KFC` | 套餐名称（模糊查询） |
| categoryId | 否 | long | `99` | 套餐分类ID |
| status | 否 | int | `1` | 售卖状态：1-起售，0-停售 |

**请求示例：**
```

GET /admin/setmeal/page?page=1&pageSize=10&name=KFC&status=1
```
#### 2.5 响应信息

##### 响应格式
`application/json`

##### 响应参数

| 参数名 | 类型 | 说明 |
|:---|:---|:---|
| code | integer | 状态码：1-成功，0-失败 |
| msg | string | 提示信息 |
| data | object | 响应数据 |

**data 结构：**

| 参数名 | 类型 | 说明 |
|:---|:---|:---|
| total | long | 总记录数 |
| records | array | 当前页数据列表 |
| ├─ id | long | 套餐ID |
| ├─ name | string | 套餐名称 |
| ├─ categoryId | long | 分类ID |
| ├─ categoryName | string | 分类名称 |
| ├─ price | number | 套餐价格 |
| ├─ image | string | 图片URL |
| ├─ description | string | 套餐描述 |
| ├─ status | int | 售卖状态：1-起售，0-停售 |
| ├─ createTime | string | 创建时间 |
| └─ updateTime | string | 更新时间 |

**响应示例：**

```
json
{
"code": 1,
"msg": "操作成功",
"data": {
"total": 15,
"records": [
{
"id": 1,
"name": "KFC经典单人套餐",
"categoryId": 99,
"categoryName": "超值套餐",
"price": 28.9,
"image": "https://xxx.png",
"description": "啊，真香",
"status": 1,
"createTime": "2024-01-01 12:00:00",
"updateTime": "2024-01-01 12:00:00"
}
]
}
}
```
---

### 3. 删除套餐

#### 3.1 接口说明

套餐管理 - 批量删除套餐（支持单个/批量删除，起售中的套餐不能删除）

#### 3.2 请求信息

| 项目 | 内容 |
|:---|:---|
| 请求方式 | `DELETE` |
| 请求路径 | `/admin/setmeal` |
| Content-Type | `application/x-www-form-urlencoded` |

#### 3.3 请求头 (Headers)

| 参数名 | 必填 | 类型 | 说明 |
|:---|:---|:---|:---|
| token | 是 | string | JWT Token |

#### 3.4 请求参数 (Query)

| 参数名 | 必填 | 类型 | 示例值 | 说明 |
|:---|:---|:---|:---|:---|
| ids | 是 | array | `1,2,3` | 套餐ID列表，多个用逗号分隔 |

**请求示例：**
```

DELETE /admin/setmeal?ids=1,2,3
```
#### 3.5 响应信息

##### 响应格式
`application/json`

##### 响应参数

| 参数名 | 类型 | 说明 |
|:---|:---|:---|
| code | integer | 状态码：1-成功，0-失败 |
| msg | string | 提示信息 |
| data | object | 响应数据（成功时为空） |

**响应示例：**

- 成功响应
```
json
{
"code": 1,
"msg": "操作成功",
"data": null
}
```
- 失败响应（起售中的套餐不能删除）
```
json
{
"code": 0,
"msg": "起售中的套餐不能删除",
"data": null
}
```
---

### 4. 根据ID查询套餐

#### 4.1 接口说明

套餐管理 - 根据ID查询套餐详情（用于修改页面回显数据）

#### 4.2 请求信息

| 项目 | 内容 |
|:---|:---|
| 请求方式 | `GET` |
| 请求路径 | `/admin/setmeal/{id}` |
| Content-Type | `application/json` |

#### 4.3 请求头 (Headers)

| 参数名 | 必填 | 类型 | 说明 |
|:---|:---|:---|:---|
| token | 是 | string | JWT Token |

#### 4.4 请求参数 (Path)

| 参数名 | 必填 | 类型 | 示例值 | 说明 |
|:---|:---|:---|:---|:---|
| id | 是 | long | `1` | 套餐ID |

**请求示例：**
```

GET /admin/setmeal/1
```
#### 4.5 响应信息

##### 响应格式
`application/json`

##### 响应参数

| 参数名 | 类型 | 说明 |
|:---|:---|:---|
| code | integer | 状态码：1-成功，0-失败 |
| msg | string | 提示信息 |
| data | object | 套餐详情数据 |

**data 结构：**

| 参数名 | 类型 | 说明 |
|:---|:---|:---|
| id | long | 套餐ID |
| name | string | 套餐名称 |
| categoryId | long | 分类ID |
| price | number | 套餐价格 |
| image | string | 图片URL |
| description | string | 套餐描述 |
| status | int | 售卖状态 |
| setmealDishes | array | 套餐菜品列表 |
| ├─ id | long | 关联记录ID |
| ├─ dishId | long | 菜品ID |
| ├─ name | string | 菜品名称 |
| ├─ price | number | 菜品单价 |
| └─ copies | int | 菜品份数 |

**响应示例：**

```
json
{
"code": 1,
"msg": "操作成功",
"data": {
"id": 1,
"name": "KFC经典单人套餐",
"categoryId": 99,
"price": 28.9,
"image": "https://xxx.png",
"description": "啊，真香",
"status": 0,
"setmealDishes": [
{
"id": 1,
"dishId": 101,
"name": "香辣鸡腿堡",
"price": 15.0,
"copies": 1
},
{
"id": 2,
"dishId": 102,
"name": "中薯条",
"price": 8.0,
"copies": 1
}
]
}
}
```
---

### 5. 修改套餐

#### 5.1 接口说明

套餐管理 - 修改套餐信息（同时更新套餐菜品关联关系）

#### 5.2 请求信息

| 项目 | 内容 |
|:---|:---|
| 请求方式 | `PUT` |
| 请求路径 | `/admin/setmeal` |
| Content-Type | `application/json` |

#### 5.3 请求头 (Headers)

| 参数名 | 必填 | 类型 | 说明 |
|:---|:---|:---|:---|
| token | 是 | string | JWT Token |
| Content-Type | 是 | string | `application/json` |

#### 5.4 请求参数 (Body)

| 参数名 | 必填 | 类型 | 示例值 | 说明 |
|:---|:---|:---|:---|:---|
| id | 是 | long | `1` | 套餐ID |
| name | 是 | string | `KFC经典单人套餐-改` | 套餐名称 |
| categoryId | 是 | long | `99` | 套餐分类ID |
| price | 是 | number | `29.9` | 套餐价格 |
| image | 是 | string | `https://xxx.png` | 套餐图片URL |
| description | 否 | string | `更新后的描述` | 套餐描述 |
| setmealDishes | 是 | array | | 套餐菜品列表（全量覆盖） |
| ├─ dishId | 是 | long | `101` | 菜品ID |
| ├─ name | 是 | string | `香辣鸡腿堡` | 菜品名称（冗余） |
| ├─ price | 是 | number | `15.0` | 菜品单价（冗余） |
| └─ copies | 是 | int | `2` | 菜品份数 |

**请求示例：**
```
json
{
"id": 1,
"name": "KFC经典单人套餐-改",
"categoryId": 99,
"price": 29.9,
"image": "https://xxx.png",
"description": "更新后的描述",
"setmealDishes": [
{
"dishId": 101,
"name": "香辣鸡腿堡",
"price": 15.0,
"copies": 2
},
{
"dishId": 104,
"name": "蛋挞",
"price": 6.0,
"copies": 1
}
]
}
```
#### 5.5 响应信息

##### 响应格式
`application/json`

##### 响应参数

| 参数名 | 类型 | 说明 |
|:---|:---|:---|
| code | integer | 状态码：1-成功，0-失败 |
| msg | string | 提示信息 |
| data | object | 响应数据（成功时为空） |

**响应示例：**

```
json
{
"code": 1,
"msg": "操作成功",
"data": null
}
```
---

### 6. 起售停售套餐

#### 6.1 接口说明

套餐管理 - 修改套餐售卖状态（起售/停售）

#### 6.2 请求信息

| 项目 | 内容 |
|:---|:---|
| 请求方式 | `POST` |
| 请求路径 | `/admin/setmeal/status/{status}` |
| Content-Type | `application/x-www-form-urlencoded` |

#### 6.3 请求头 (Headers)

| 参数名 | 必填 | 类型 | 说明 |
|:---|:---|:---|:---|
| token | 是 | string | JWT Token |

#### 6.4 请求参数

**Path 参数：**

| 参数名 | 必填 | 类型 | 示例值 | 说明 |
|:---|:---|:---|:---|:---|
| status | 是 | int | `1` | 售卖状态：1-起售，0-停售 |

**Query 参数：**

| 参数名 | 必填 | 类型 | 示例值 | 说明 |
|:---|:---|:---|:---|:---|
| id | 是 | long | `1` | 套餐ID |

**请求示例：**
```

POST /admin/setmeal/status/1?id=1
```
#### 6.5 响应信息

##### 响应格式
`application/json`

##### 响应参数

| 参数名 | 类型 | 说明 |
|:---|:---|:---|
| code | integer | 状态码：1-成功，0-失败 |
| msg | string | 提示信息 |
| data | object | 响应数据（成功时为空） |

**响应示例：**

- 成功响应
```
json
{
"code": 1,
"msg": "操作成功",
"data": null
}
```
- 失败响应（套餐内包含停售菜品，无法起售）
```
json
{
"code": 0,
"msg": "套餐内包含未启售菜品，无法启售",
"data": null
}
```
---

## 状态码说明

| 状态码 | 说明 |
|:---|:---|
| 1 | 请求成功 |
| 0 | 请求失败（具体原因见msg） |

---

## 注意事项

1. 新增套餐时，套餐名称必须唯一
2. 新增套餐默认为**停售**状态
3. 起售中的套餐**不能删除**
4. 起售套餐时，如果套餐内包含停售菜品，则**无法起售**
5. 修改套餐时，`setmealDishes` 字段为**全量覆盖**（先删除旧关联，再插入新关联）
6. 所有价格字段单位为元，保留两位小数


---

**说明：** 以上 6 个接口覆盖了套餐管理的完整功能。另外 2 个前置接口（根据分类ID查询菜品、图片上传）在之前的分类/菜品模块已完成，这里不再重复。