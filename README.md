# 🧩 Tổng quan hệ thống

## 🎯 Mục tiêu
Xây dựng hệ thống đọc truyện gồm:
- 📱 Mobile App (Android - Kotlin)
- 🌐 Backend API (Spring Boot)
- 🗄️ Database (PostgreSQL)

## 🧱 Kiến trúc
- `Mobile App (Kotlin)`
- `↓ REST API`
- `Spring Boot Backend`
- `↓`
- `PostgreSQL Database`
- `↓`
- `Cloud Storage (ảnh truyện - optional)`


---

# 🗂️ Chức năng hệ thống

## 👤 User
- Đăng ký / đăng nhập (JWT)
- Xem thông tin cá nhân
- Lưu truyện yêu thích ❤️
- Lịch sử đọc 📖

## 📚 Truyện
- Xem danh sách truyện
- Tìm kiếm 🔍
- Lọc theo thể loại
- Xem chi tiết truyện
- Xem danh sách chapter

## 📄 Chapter
- Đọc truyện (ảnh hoặc text)
- Ghi nhớ vị trí đọc

## ⭐ Đánh giá
- Rating (1–5 sao)
- Comment

## 🔔 (Optional nâng cao)
- Thông báo khi có chapter mới

---

# 🗄️ Database (PostgreSQL)

## 📌 Bảng chính

### **users**
| Tên cột    | Kiểu dữ liệu | Ràng buộc |
|------------|--------------|-----------|
| id         | PK           |           |
| username   |              |           |
| email      |              |           |
| password   |              |           |
| avatar     |              |           |
| created_at |              |           |

### **stories**
| Tên cột     | Kiểu dữ liệu | Ràng buộc |
|-------------|--------------|-----------|
| id          | PK           |           |
| title       |              |           |
| author      |              |           |
| description |              |           |
| cover_image |              |           |
| status      | ongoing/completed |      |
| created_at  |              |           |

### **genres**
| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---------|--------------|-----------|
| id      | PK           |           |
| name    |              |           |

### **story_genres**
| Tên cột  | Kiểu dữ liệu | Ràng buộc |
|----------|--------------|-----------|
| story_id | FK           |           |
| genre_id | FK           |           |

### **chapters**
| Tên cột        | Kiểu dữ liệu | Ràng buộc |
|----------------|--------------|-----------|
| id             | PK           |           |
| story_id       | FK           |           |
| title          |              |           |
| chapter_number |              |           |
| created_at     |              |           |

### **chapter_images**
| Tên cột     | Kiểu dữ liệu | Ràng buộc |
|-------------|--------------|-----------|
| id          | PK           |           |
| chapter_id  | FK           |           |
| image_url   |              |           |
| order_index |              |           |

### **favorites**
| Tên cột  | Kiểu dữ liệu | Ràng buộc |
|----------|--------------|-----------|
| id       | PK           |           |
| user_id  | FK           |           |
| story_id | FK           |           |

### **reading_history**
| Tên cột      | Kiểu dữ liệu | Ràng buộc |
|--------------|--------------|-----------|
| id           | PK           |           |
| user_id      | FK           |           |
| chapter_id   | FK           |           |
| last_read_at |              |           |

### **comments**
| Tên cột    | Kiểu dữ liệu | Ràng buộc |
|------------|--------------|-----------|
| id         | PK           |           |
| user_id    | FK           |           |
| story_id   | FK           |           |
| content    |              |           |
| created_at |              |           |

### **ratings**
| Tên cột  | Kiểu dữ liệu | Ràng buộc |
|----------|--------------|-----------|
| id       | PK           |           |
| user_id  | FK           |           |
| story_id | FK           |           |
| score    |              |           |

---

# ⚙️ Backend (Spring Boot)

## 📦 Cấu trúc project

com/app/truyen/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
├── security/
├── config/
└── exception/


## 🔐 Authentication (JWT)
- `POST /api/auth/register`
- `POST /api/auth/login`

## 👤 USER PROFILE API
- `GET /api/users/me`
- `PUT /api/users/me`
- `POST /api/users/avatar`
- `PUT /api/users/change-password`

## 📚 Story API
- `GET /api/stories`
- `GET /api/stories/{id}`
- `GET /api/stories/search?q=...`
- `GET /api/stories/genre/{id}`

## 📄 Chapter API
- `GET /api/chapters/{storyId}`
- `GET /api/chapter/{id}`

## ❤️ Favorite API
- `POST /api/favorites/{storyId}`
- `DELETE /api/favorites/{storyId}`
- `GET /api/favorites`

## 📖 History API
- `POST /api/history`
- `GET /api/history`
- `DELETE /api/history/{id}`

## ⭐ Rating API
- `POST /api/ratings`
- `GET /api/ratings/{storyId}`

## 💬 Comment API
- `GET /api/comments/{storyId}`
- `POST /api/comments`

---

# 🎨 Thiết kế Mobile App (UI/UX)

> 👉 Thiết kế theo hướng đơn giản – đẹp – giống app thật

## 🏠 1. Home Screen
Hiển thị:
- Banner truyện nổi bật
- Danh sách:
  - Truyện mới
  - Truyện hot 🔥
  - Truyện full

👉 **Layout:**
- [Search bar 🔍]

- [Banner slider]

- [Truyện Hot][Horizontal list]

- [Truyện mới][Grid list]


## 🔍 2. Search Screen
- [Search input]

- [Filter: Thể loại]

- [List kết quả]


## 📖 3. Story Detail Screen

- [Cover Image][Title][Author]

- [Rating ⭐]

- [Description]

- [Button: Đọc ngay]

- [Danh sách chapter]


## 📄 4. Reading Screen
👉 2 mode:
- 📷 Mode ảnh (phổ biến)  
  - Scroll vertical ảnh
- 📝 Mode text  
  - Font size 
  - Dark mode 🌙

## ❤️ 5. Favorite Screen
- List truyện đã lưu


## 👤 6. Profile Screen
- Avatar
- Username
- History
- Favorite
- Logout


---

# 🎯 UI Style (gợi ý đẹp + dễ làm)
- 🎨 **Màu chính:** `#FF6B6B` (đỏ nhẹ), `#1E1E2E` (dark mode)
- **Font:** Roboto / SF Pro
- **Card bo góc:** 16dp
- **Shadow nhẹ**

---

# 🚀 Công nghệ đề xuất

## Backend
- Spring Boot
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Swagger (OpenAPI)

## Mobile
- Kotlin
- XML hoặc Jetpack Compose
- Retrofit (call API)
- Glide (load ảnh)

---

# 🔥 Nâng cao (nếu muốn điểm cao)
- Cache Redis
- Upload ảnh S3 / Cloudinary
- Recommendation (AI nhẹ)
- Dark mode 🌙