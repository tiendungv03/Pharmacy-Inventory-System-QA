# 🧪 Pharmacy Inventory System – QA Automation

Dự án chứa **Selenium + TestNG** cho PIS. Mỗi module (vd **PIS-2: Quản lý danh mục thuốc**) theo quy trình **feature → develop → main**.

## 🏗️ Cấu trúc thư mục

├── 📁 .github
│ └── 📁 workflows # CI (GitHub Actions)
├── 📁 reports # Báo cáo (Surefire/Allure)
├── 📁 testdata # Dữ liệu test
├── 📁 ui-tests # UI automation (Selenium + TestNG)
│ ├── 📁 src/test/java/vn/pis/ui
│ │ ├── 📁 base
│ │ │ └── BaseTest.java
│ │ ├── 📁 pages
│ │ │ ├── CategoriesPage.java
│ │ │ └── LoginPage.java
│ │ ├── 📁 tests
│ │ │ ├── PIS2_Categories.java
│ │ │ └── PIS2_ConsoleLogger.java
│ │ └── 📁 util
│ │ ├── Config.java
│ │ └── TestEnv.java
│ └── 📁 src/test/resources
│ ├── 📁 config
│ │ └── common.properties
│ └── testng.xml
└── .gitignore

## 💻 Yêu cầu
- Java **17/21**, Maven **3.9+**, IntelliJ **2023.3+**, Selenium **4.x**, TestNG **7.x**, Chrome mới.

## ⚙️ Chạy test (IntelliJ)
1. **File → Open…** → mở thư mục `ui-tests/`
2. Mở `src/test/resources/testng.xml` → **Run 'testng.xml'**
3. Hoặc Maven (tại `ui-tests/`): `mvn clean test`

Cấu hình môi trường: `ui-tests/src/test/resources/config/common.properties`
```properties
base.url=https://<your-env>
admin.username=admin
admin.password=123456
browser=chrome
implicit.wait=10
explicit.wait=5

Quy trình nhánh

feature/PIS-# → PR vào develop

develop → tích hợp hằng ngày

main → release (tag)

Không push trực tiếp main/develop; dùng PR + squash.

