# 💰 Vision Ledger - Quản Lý Tài Chính Cá Nhân

Ứng dụng quản lý chi tiêu và ngân sách cá nhân được xây dựng với:
- **Backend**: Spring Boot (Java 21)
- **Frontend**: React + TypeScript + Vite
- **Database**: TiDB Cloud (MySQL compatible)

## 🚀 Quick Start

### Development

**Backend:**
```bash
cd back/ledger
mvn spring-boot:run
```

**Frontend:**
```bash
cd front
npm install
npm run dev
```

### Production Deployment

Xem hướng dẫn chi tiết trong **[DEPLOY.md](./DEPLOY.md)**

Tóm tắt:
1. Setup TiDB Cloud database
2. Deploy backend lên Render
3. Deploy frontend lên Vercel
4. Cập nhật CORS

## 📁 Cấu Trúc Project

```
vision-ledger1812/
├── back/ledger/          # Backend Spring Boot
│   ├── src/main/java/    # Java source code
│   └── src/main/resources/
│       ├── application.properties
│       └── application-prod.properties
├── front/                # Frontend React + Vite
│   └── src/
├── mysql.sql             # Database schema
├── render.yaml           # Render deployment config
├── vercel.json           # Vercel deployment config
└── DEPLOY.md             # Hướng dẫn deploy chi tiết
```

## 🔧 Cấu Hình

### Environment Variables

**Backend (Render):**
- `DATABASE_URL`: TiDB Cloud connection string
- `DATABASE_USERNAME`: TiDB username
- `DATABASE_PASSWORD`: TiDB password
- `JWT_SECRET`: Secret key cho JWT (generate mới!)
- `ALLOWED_ORIGINS`: Frontend URL (Vercel)

**Frontend (Vercel):**
- `VITE_API_URL`: Backend API URL (Render)

Xem chi tiết trong [DEPLOY.md](./DEPLOY.md)

## 📚 Documentation

- **[DEPLOY.md](./DEPLOY.md)** - Hướng dẫn deploy từ đầu đến cuối
- **[mysql.sql](./mysql.sql)** - Database schema

## 🛠️ Tech Stack

**Backend:**
- Spring Boot 3.5.7
- Spring Security + JWT
- Spring Data JPA
- MySQL Connector (TiDB compatible)

**Frontend:**
- React 18
- TypeScript
- Vite
- Tailwind CSS
- shadcn/ui

## 📝 License

Private project

