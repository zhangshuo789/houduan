# 07 文件上传模块

## 模块概述

文件上传模块负责处理用户头像、帖子图片等文件的上传、存储和访问。文件存储在本地磁盘，通过API提供访问。

## 核心技术

| 技术 | 说明 |
|------|------|
| Spring MultipartFile | 文件上传处理 |
| 本地磁盘存储 | 文件存储到指定目录 |
| UUID | 文件名唯一化 |
| InputStream/OutputStream | 文件读写 |

## 核心文件

```
src/main/java/com/volleyball/volleyballcommunitybackend/
├── controller/
│   └── FileController.java              # 文件接口
├── service/
│   └── FileService.java                 # 文件业务逻辑
├── repository/
│   └── FileRepository.java              # 文件数据访问
├── entity/
│   └── FileEntity.java                  # 文件实体
├── dto/response/
│   └── FileResponse.java                # 文件响应
└── config/
    └── FileProperties.java              # 文件配置
```

## 数据表结构

**file表**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| file_name | VARCHAR(255) | 原始文件名 |
| stored_name | VARCHAR(255) | 存储文件名(UUID) |
| file_path | VARCHAR(500) | 存储路径 |
| file_type | VARCHAR(20) | 文件类型(avatar/post_image) |
| content_type | VARCHAR(100) | MIME类型 |
| file_size | BIGINT | 文件大小(字节) |
| user_id | BIGINT | 上传用户ID |
| created_at | DATETIME | 上传时间 |

## 配置文件

**application.properties**:
```properties
file.base-path=${FILE_BASE_PATH:./data/volleyball-community}
file.base-url=${FILE_BASE_URL:}
file.max-size=10485760  # 10MB
file.allowed-types=avatar,post_image
```

## 代码流转

### 上传文件

```
POST /api/file/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data
    ↓
FileController.uploadFile(file, type, userId)
    ↓
FileService.uploadFile(file, type, userId)
    ↓
FileProperties.getAllowedTypes() 验证type是否允许
    ↓ 非法type → 抛出RuntimeException("不允许的文件类型")
    ↓
file.getSize() <= FileProperties.getMaxSize() 验证大小
    ↓ 超出限制 → 抛出RuntimeException("文件大小超出限制")
    ↓
String originalFilename = file.getOriginalFilename()
String extension = getFileExtension(originalFilename)
String storedName = UUID.randomUUID().toString() + extension
    ↓
Path targetPath = Paths.get(basePath, type, storedName)
Files.createDirectories(targetPath.getParent())
file.transferTo(targetPath) 保存到磁盘
    ↓
FileEntity fileEntity = FileEntity.builder()
    .fileName(originalFilename)
    .storedName(storedName)
    .filePath(targetPath.toString())
    .fileType(type)
    .contentType(file.getContentType())
    .fileSize(file.getSize())
    .userId(userId)
    .build()
    ↓
FileRepository.save(fileEntity)
    ↓
return FileResponse { id, fileName, url, fileType, fileSize }
```

### 获取文件元信息

```
GET /api/file/{fileId}
    ↓
FileService.getFileById(fileId)
    ↓
FileRepository.findById(fileId)
    ↓
return FileResponse
```

### 获取文件访问URL

```
GET /api/file/{fileId}/url
    ↓
FileService.getFileUrl(fileId, file.baseUrl)
    ↓
构建URL: file.baseUrl + "/api/file/" + fileId
    ↓
return { url: "https://..." }
```

### 下载/预览文件

```
GET /api/file/{fileId}/resource
    ↓
FileService.getFileResource(fileId)
    ↓
FileRepository.findById(fileId)
    ↓
Path filePath = Paths.get(fileEntity.getFilePath())
    ↓ 文件不存在 → 抛出RuntimeException("文件不存在")
    ↓
return ResponseEntity
    .ok()
    .contentType(MediaType.parseMediaType(fileEntity.getContentType()))
    .body(new FileSystemResource(filePath))
```

## 存储结构

```
${file.base-path}/
├── avatar/
│   └── a1b2c3d4.png
│   └── e5f6g7h8.jpg
└── post_image/
    └── i9j0k1l2.png
```

## 接口详情

### POST /api/file/upload 上传文件

**请求头**: `Authorization: Bearer <token>`

**请求表单**:
- `file`: 文件（必填）
- `type`: 文件类型 `avatar` 或 `post_image`（必填）

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "fileName": "my-avatar.png",
    "url": "http://localhost:8080/api/file/1/resource",
    "fileType": "avatar",
    "fileSize": 102400
  }
}
```

### GET /api/file/{fileId}/url 获取访问URL

**响应**:
```json
{
  "code": 200,
  "data": {
    "url": "http://localhost:8080/api/file/1/resource"
  }
}
```
