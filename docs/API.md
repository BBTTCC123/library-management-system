# API 使用示例

所有接口返回统一结构：

```json
{ "code": 200, "message": "success", "data": {}, "timestamp": "..." }
```

除 `公开` 接口外，其余接口都需要在请求头携带令牌：

```
Authorization: Bearer <accessToken>
```

---

## 1. 认证

### 注册

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "charlie",
    "password": "charlie123",
    "fullName": "Charlie",
    "email": "charlie@library.local"
  }'
```

### 登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"alice123"}'
```

响应中的 `data.accessToken` 即为后续请求所需的令牌。

---

## 2. 图书

### 分页检索（公开）

```bash
# 关键字 + 仅看有库存 + 分页 + 按书名排序
curl "http://localhost:8080/api/books?keyword=java&availableOnly=true&page=0&size=10&sort=title,asc"
```

### 图书详情（公开）

```bash
curl http://localhost:8080/api/books/1
```

### 新增图书（ADMIN / LIBRARIAN）

```bash
curl -X POST http://localhost:8080/api/books \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "isbn": "9787111213826",
    "title": "Effective Java",
    "author": "Joshua Bloch",
    "publisher": "机械工业出版社",
    "publishYear": 2018,
    "categoryId": 1,
    "totalCopies": 3,
    "description": "Java 编程最佳实践"
  }'
```

### 更新图书（ADMIN / LIBRARIAN）

```bash
curl -X PUT http://localhost:8080/api/books/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"isbn":"9787111213826","title":"Effective Java (3rd)","author":"Joshua Bloch","totalCopies":5}'
```

### 删除图书（ADMIN / LIBRARIAN）

```bash
curl -X DELETE http://localhost:8080/api/books/1 -H "Authorization: Bearer $TOKEN"
```

---

## 3. 分类

```bash
# 列表（公开）
curl http://localhost:8080/api/categories

# 新增（ADMIN / LIBRARIAN）
curl -X POST http://localhost:8080/api/categories \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"艺术","description":"艺术与设计"}'
```

---

## 4. 借阅

### 借书

```bash
curl -X POST http://localhost:8080/api/borrows \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"bookId": 1}'
```

### 我的借阅记录

```bash
curl "http://localhost:8080/api/borrows/me?status=BORROWED&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

### 续借

```bash
curl -X POST http://localhost:8080/api/borrows/1/renew -H "Authorization: Bearer $TOKEN"
```

### 还书

```bash
curl -X POST http://localhost:8080/api/borrows/1/return -H "Authorization: Bearer $TOKEN"
```

### 全部借阅记录（ADMIN / LIBRARIAN）

```bash
curl "http://localhost:8080/api/borrows?status=OVERDUE&page=0&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## 5. 用户与统计

```bash
# 当前用户
curl http://localhost:8080/api/users/me -H "Authorization: Bearer $TOKEN"

# 用户列表（ADMIN）
curl http://localhost:8080/api/users -H "Authorization: Bearer $ADMIN_TOKEN"

# 修改角色（ADMIN）
curl -X PATCH http://localhost:8080/api/users/2/role \
  -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" \
  -d '{"role":"LIBRARIAN"}'

# 禁用用户（ADMIN）
curl -X PATCH "http://localhost:8080/api/users/2/enabled?enabled=false" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 统计信息
curl http://localhost:8080/api/stats -H "Authorization: Bearer $TOKEN"
```

---

## 6. 常见状态码

| 状态码 | 含义 |
| --- | --- |
| 200 | 成功 |
| 201 | 创建成功 |
| 400 | 参数校验失败 / 业务规则不满足 |
| 401 | 未认证或令牌无效 |
| 403 | 已认证但无权限 |
| 404 | 资源不存在 |
| 409 | 资源冲突（用户名/ISBN/分类重复，删除受保护） |
| 500 | 服务器内部错误 |
