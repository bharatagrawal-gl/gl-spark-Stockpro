<h1 align="center">🚀 StockPro - Inventory Management System</h1>

<p align="center">
  📦 Track • Control • Optimize • Grow
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Backend-Java%20SpringBoot-green?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Architecture-Microservices-blue?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Database-MySQL-orange?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Cache-Redis-red?style=for-the-badge"/>
</p>

---

## 📌 Overview

StockPro is a **microservices-based inventory management system** designed for mid-size businesses to efficiently manage stock, procurement, and warehouse operations.

It provides:
- 📊 Real-time inventory visibility
- 🔄 Complete stock movement tracking
- 🧾 Structured purchase workflows
- 📢 Automated alerting system
- 📈 Analytical reporting for decision-making

---

## 🧩 Core Functional Modules

---

### 📦 Product Management
- ➕ Create, update, deactivate products
- 🏷️ Unique SKU & barcode support
- 📂 Categorization (brand, category, unit)
- 💰 Cost & selling price tracking
- ⚙️ Reorder level & max stock configuration
- 🔍 Search by name, SKU, category, brand, barcode

---

### 🏭 Warehouse & Stock Management
- 🏢 Multi-warehouse support
- 📊 Track:
    - Total quantity
    - Reserved quantity
    - Available quantity
- 🔁 Inter-warehouse stock transfer (atomic)
- 📉 Low-stock detection

---

### 🔄 Stock Movement (Audit Trail)
Every inventory change is recorded with full traceability.

#### Supported Movements:
- 📥 Stock In (GRN)
- 📤 Stock Out
- 🔁 Transfer
- ✏️ Adjustment
- ❌ Write-Off
- 🔄 Return

Each record includes:
- 📦 Product & warehouse
- 🔢 Quantity & cost
- 🔗 Reference (PO / order)
- 👤 User
- 🕒 Timestamp
- 📊 Balance after movement

> 🔒 Immutable system — corrections via reverse entries only

---

### 🧾 Purchase Order Management
- 📝 Create purchase orders with line items
- 🔄 Workflow:Draft → Pending → Approved → Received / Cancelled
- ✅ Approval & rejection system
- 📥 Partial goods receipt support
- 🔍 Filter by supplier, status, warehouse, date

---

### 🤝 Supplier Management
- 🧑‍💼 Supplier profile management
- 📍 Contact & location details
- 💳 Payment terms & lead time
- ⭐ Performance rating system
- 🔍 Search by name, city, country
- 🚫 Deactivation support

---

### 📢 Alert & Notification System
Automatic alerts for:
- ⚠️ Low stock
- 📅 Overdue deliveries


---

### 📊 Reporting & Analytics
- 💰 Inventory valuation
- 🔄 Inventory turnover
- 📈 Product movement insights:
- Top-moving
- Slow-moving
- Dead stock
- 📊 Purchase order summaries
- 🗓️ Daily inventory snapshots

---

### 🔐 User & Role Management
- 🔑 Email/password authentication
- 👥 Role-based access:
- 👨‍🏭 Warehouse Staff
- 📊 Inventory Manager
- 🛠️ Admin

- ✏️ Profile management
- 🔄 Role assignment (Admin)
- 🚫 User activation/deactivation

---

## 🏗️ Architecture

- ⚙️ Microservices architecture
- 🔗 REST API communication
- 🧱 Layered design:
- Entity
- Repository
- Service
- Controller

### 🔧 Services
- 🔐 Auth Service
- 📦 Product Service
- 🏭 Warehouse Service
- 🧾 Purchase Service
- 🤝 Supplier Service
- 🔄 Movement Service
- 📢 Alert Service
- 📊 Report Service

---

## 🛠️ Tech Stack

### ⚙️ Backend
- ☕ Java (Spring Boot)
- 🔐 Spring Security (JWT)
- 🗄️ Spring Data JPA

### 🗄️ Database & Cache
- 🐬 MySQL
- ⚡ Redis

### 📬 Messaging & Jobs
- 🐰 RabbitMQ
- ⏰ Spring Scheduler


---

## ⚙️ Key System Properties

- ⚡ Fast queries (<1s response time)
- 🔄 Atomic stock transfers
- 🔒 Secure (JWT + bcrypt)
- 📉 No negative stock allowed
- 🧾 Full audit trail
- 🔁 Optimistic locking for concurrency

---

## 📡 API Structure

| 🔧 Module | 📍 Endpoint |
|----------|-----------|
| 🔐 Auth | `/auth/*` |
| 📦 Products | `/products/*` |
| 🏭 Warehouse | `/warehouses/*` |
| 🧾 Purchase | `/purchase-orders/*` |
| 🤝 Supplier | `/suppliers/*` |
| 🔄 Movements | `/movements/*` |
| 📢 Alerts | `/alerts/*` |
| 📊 Reports | `/reports/*` |

---

## 📘 Glossary

| 📌 Term | 📖 Meaning |
|--------|----------|
| 📦 SKU | Unique product identifier |
| 📥 GRN | Goods Received Note |
| 🧾 PO | Purchase Order |
| ⚠️ Reorder Level | Minimum stock threshold |
| 💤 Dead Stock | No movement for long time |
| 📊 Snapshot | Daily inventory record |

---

## 👨‍💻 Author

**Kartik Arora**

🔗 LinkedIn: https://www.linkedin.com/in/kartik-arora-1b9671244/  
💻 GitHub: https://github.com/Kartik6123

---

## ⭐ Project Value

✔ Real-world microservices architecture  
✔ Enterprise-level inventory workflows  
✔ Strong backend system design

---

<p align="center">
⭐ If you like this project, consider starring the repo!
</p>