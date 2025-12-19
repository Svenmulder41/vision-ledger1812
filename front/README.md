# Vision Ledger Frontend

Frontend application cho Vision Ledger - Quản lý tài chính cá nhân.

## 🚀 Development

### Prerequisites

- Node.js 18+ và npm (hoặc yarn/pnpm)
- [Install Node.js với nvm](https://github.com/nvm-sh/nvm#installing-and-updating)

### Setup

```bash
# 1. Clone repository
git clone <YOUR_GIT_URL>
cd vision-ledger1812

# 2. Navigate to frontend directory
cd front

# 3. Install dependencies
npm install

# 4. Start development server
npm run dev
```

Development server sẽ chạy tại `http://localhost:5173` (hoặc port khác nếu 5173 đã được sử dụng)

### Build for Production

```bash
npm run build
```

Build output sẽ ở trong thư mục `dist/`

### Preview Production Build

```bash
npm run preview
```

## 🛠️ Tech Stack

- **Vite** - Build tool và dev server
- **React 18** - UI framework
- **TypeScript** - Type safety
- **shadcn/ui** - UI components
- **Tailwind CSS** - Styling
- **React Router** - Routing
- **Axios** - HTTP client
- **TanStack Query** - Data fetching

## 📁 Project Structure

```
front/
├── src/
│   ├── components/     # React components
│   ├── pages/          # Page components
│   ├── lib/            # Utilities và helpers
│   ├── hooks/          # Custom React hooks
│   ├── contexts/       # React contexts
│   └── services/       # API services
├── public/             # Static assets
└── package.json        # Dependencies
```

## 🔧 Configuration

### Environment Variables

Tạo file `.env` trong thư mục `front/`:

```bash
VITE_API_URL=http://localhost:8080/api
```

Cho production, set trong Vercel dashboard:
```bash
VITE_API_URL=https://your-backend.onrender.com/api
```

## 📝 Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint

