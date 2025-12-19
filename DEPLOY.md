# 🚀 Hướng Dẫn Deploy Vision Ledger

Deploy ứng dụng lên:
- **Database**: TiDB Cloud
- **Backend**: Render  
- **Frontend**: Vercel

---

## 📋 Bước 1: Chuẩn Bị TiDB Cloud Database

### 1.1. Tạo Database trên TiDB Cloud

1. Đăng nhập [TiDB Cloud](https://tidbcloud.com/)
2. Tạo cluster mới (chọn free tier nếu cần)
3. **Thông tin kết nối của bạn:**
   ```
   Host: gateway01.ap-southeast-1.prod.aws.tidbcloud.com
   Port: 4000
   Database: pocket_vision_ledger
   Username: 2agRJunKqoxmSFQ.root
   Password: ONi6RSr6Fk4CnEXA
   ```

### 1.2. Tạo Tables

1. Vào TiDB Cloud Console → **SQL Editor**
2. Copy toàn bộ nội dung file `mysql.sql`
3. Paste và chạy trong SQL Editor
4. Kiểm tra các tables đã được tạo thành công

### 1.3. Whitelist IP (Quan trọng!)

1. Vào TiDB Cloud → **Security** → **IP Access List**
2. Thêm IP của Render (hoặc chọn **Allow All** cho development)
3. Lưu lại

---

## 🔧 Bước 2: Deploy Backend lên Render

### 2.1. Tạo Web Service

1. Đăng nhập [Render](https://render.com/)
2. Click **"New +"** → **"Web Service"**
3. Connect GitHub repository của bạn
4. Chọn repository và branch (thường là `main` hoặc `master`)

### 2.2. Cấu Hình Build Settings

Trong Render dashboard, thiết lập:

- **Name**: `vision-ledger-backend`
- **Environment**: `Docker` ⚠️ (Render chỉ hỗ trợ Docker)
- **Region**: `Singapore` (hoặc gần nhất)
- **Branch**: `main` (hoặc branch bạn muốn deploy)
- **Root Directory**: `back/ledger` (quan trọng!)
- **Dockerfile Path**: `Dockerfile` (tự động detect)
- **Docker Build Context**: `back/ledger` (hoặc để trống, Render sẽ tự detect)

**Lưu ý**: Render sẽ tự động build Docker image từ Dockerfile trong `back/ledger/`

### 2.3. Thiết Lập Environment Variables

Vào **Environment** tab, thêm các biến sau:

```bash
# Spring Profile
SPRING_PROFILES_ACTIVE=prod
PORT=8080

# TiDB Cloud Database
DATABASE_URL=jdbc:mysql://gateway01.ap-southeast-1.prod.aws.tidbcloud.com:4000/pocket_vision_ledger?sslMode=VERIFY_IDENTITY
DATABASE_USERNAME=2agRJunKqoxmSFQ.root
DATABASE_PASSWORD=ONi6RSr6Fk4CnEXA

# JWT Security (QUAN TRỌNG: Generate secret mới!)
# Chạy lệnh này để tạo secret:
# openssl rand -base64 64
JWT_SECRET=<paste-secret-key-here>
JWT_EXPIRATION=86400000

# CORS (Sẽ cập nhật sau khi deploy frontend)
ALLOWED_ORIGINS=https://your-frontend.vercel.app

# Database Schema
DDL_AUTO=validate
SHOW_SQL=false
FORMAT_SQL=false

# Logging
SECURITY_LOG_LEVEL=WARN
HIBERNATE_LOG_LEVEL=WARN
WEB_LOG_LEVEL=WARN
```

**⚠️ Lưu ý quan trọng:**
- **JWT_SECRET**: Phải generate secret mới, không dùng default!
  ```bash
  # Linux/Mac
  openssl rand -base64 64
  
  # Windows PowerShell
  [Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
  ```
- **ALLOWED_ORIGINS**: Tạm thời để placeholder, sẽ cập nhật sau khi có URL frontend

### 2.4. Deploy Backend

1. Click **"Create Web Service"**
2. Render sẽ tự động:
   - Build Docker image từ Dockerfile trong `back/ledger/`
   - Deploy container
3. Đợi build hoàn tất (5-10 phút - Docker build lâu hơn)
4. **Lưu lại URL backend** (ví dụ: `https://vision-ledger-backend.onrender.com`)
5. Test endpoint: `https://your-backend.onrender.com/api/auth/login`

**Lưu ý**: 
- Lần đầu build Docker sẽ mất thời gian (download base images)
- Kiểm tra logs nếu có lỗi build

### 2.5. Kiểm Tra Logs

Nếu có lỗi:
- Vào **Logs** tab trong Render
- Kiểm tra lỗi database connection
- Đảm bảo TiDB Cloud đã whitelist IP của Render

---

## 🎨 Bước 3: Deploy Frontend lên Vercel

### 3.1. Tạo Project

1. Đăng nhập [Vercel](https://vercel.com/)
2. Click **"Add New..."** → **"Project"**
3. Import GitHub repository của bạn
4. Chọn repository

### 3.2. Cấu Hình Build Settings

- **Framework Preset**: `Vite`
- **Root Directory**: `front`
- **Build Command**: `npm run build` (tự động)
- **Output Directory**: `dist` (tự động)
- **Install Command**: `npm install` (tự động)

### 3.3. Thiết Lập Environment Variables

Thêm biến môi trường:

```bash
VITE_API_URL=https://your-backend.onrender.com/api
```

**Thay `your-backend.onrender.com` bằng URL backend thực tế từ Render!**

### 3.4. Deploy Frontend

1. Click **"Deploy"**
2. Vercel sẽ tự động build và deploy
3. Đợi build hoàn tất (2-5 phút)
4. **Lưu lại URL frontend** (ví dụ: `https://vision-ledger.vercel.app`)

---

## 🔗 Bước 4: Cập Nhật CORS

Sau khi có URL frontend từ Vercel:

1. Vào **Render Dashboard** → Backend service → **Environment**
2. Tìm biến `ALLOWED_ORIGINS`
3. Cập nhật giá trị:
   ```
   ALLOWED_ORIGINS=https://your-frontend.vercel.app
   ```
4. Render sẽ tự động restart service
5. Đợi service restart (1-2 phút)

---

## ✅ Bước 5: Kiểm Tra Deployment

### 5.1. Test Backend

```bash
# Test health check
curl https://your-backend.onrender.com/api/auth/login

# Hoặc mở trong browser
https://your-backend.onrender.com/api/auth/login
```

### 5.2. Test Frontend

1. Mở URL Vercel trong browser
2. Thử đăng ký/đăng nhập
3. Kiểm tra console (F12) xem có lỗi không
4. Kiểm tra Network tab xem API calls có thành công không

### 5.3. Troubleshooting

**Lỗi CORS:**
- Đảm bảo `ALLOWED_ORIGINS` đúng với URL Vercel
- Không có dấu `/` ở cuối URL
- Kiểm tra service đã restart chưa

**Lỗi Database Connection:**
- Kiểm tra TiDB Cloud đã whitelist IP của Render
- Kiểm tra username/password đúng
- Kiểm tra database name đúng (`pocket_vision_ledger`)

**Backend không start:**
- Kiểm tra logs trong Render
- Đảm bảo JWT_SECRET đã được set
- Kiểm tra Docker build có thành công không
- Kiểm tra Dockerfile path đúng (`back/ledger/Dockerfile`)
- Kiểm tra root directory đúng (`back/ledger`)
- Kiểm tra Dockerfile có trong repository chưa

**Frontend không kết nối được Backend:**
- Kiểm tra `VITE_API_URL` trong Vercel
- Đảm bảo backend đã deploy thành công
- Kiểm tra network tab trong browser console

---

## 📝 Checklist Trước Khi Deploy

- [ ] Đã tạo database và tables trên TiDB Cloud
- [ ] Đã whitelist IP của Render trên TiDB Cloud
- [ ] Đã generate JWT_SECRET mới (không dùng default)
- [ ] Đã set tất cả environment variables trong Render
- [ ] Đã test backend endpoint hoạt động
- [ ] Đã set VITE_API_URL trong Vercel
- [ ] Đã test frontend kết nối được backend
- [ ] Đã cập nhật ALLOWED_ORIGINS sau khi deploy frontend

---

## 🔄 CI/CD Tự Động

Sau khi setup xong:
- **Render**: Tự động deploy khi push code lên branch chính
- **Vercel**: Tự động deploy khi push code lên branch chính

Chỉ cần push code, cả backend và frontend sẽ tự động deploy!

---

## 📞 Hỗ Trợ

Nếu gặp vấn đề:
1. Kiểm tra logs trong Render/Vercel dashboard
2. Kiểm tra environment variables đã đúng chưa
3. Kiểm tra database connection
4. Xem troubleshooting section ở trên

---

**🎉 Chúc mừng! Ứng dụng của bạn đã được deploy thành công!**

