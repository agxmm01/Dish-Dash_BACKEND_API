🍽️ Dish-Dash Backend API

Dish-Dash is a Spring Boot backend service for an online food ordering system.
It manages users, restaurants, food items, and orders with MongoDB as the database.
This backend is designed to integrate seamlessly with a React (Vite) frontend and supports secure authentication.

🚀 Features

User authentication & role-based access control (Spring Security + sessions)

Register & manage multiple restaurants

CRUD operations for food items

Order placement and status tracking

MongoDB integration for persistence

AWS S3 integration (for food/restaurant images)

RESTful APIs for frontend integration

📂 Project Structure
src/main/java/in/agampal/dishdashapi/
│── config/        # Security & application configuration
│── controller/    # REST API controllers
│── dto/           # Data Transfer Objects
│── entity/        # MongoDB entities (User, Restaurant, FoodItem, Order)
│── exception/     # Custom exception handling
│── filters/       # Security filters (session/JWT if used)
│── io/            # Request/response models
│── repository/    # MongoDB repositories
│── service/       # Business logic
│── util/          # Utility classes (helpers, token utils, etc.)
│── FoodiesapiApplication.java  # Main Spring Boot application

🛠️ Tech Stack

Java 17

Spring Boot 3.x

Spring Security (session-based authentication)

Spring Data MongoDB

AWS SDK (S3)

Lombok

Maven

⚙️ Setup & Installation
1️⃣ Clone the repository
git clone https://github.com/agxmm01/Dish-Dash_BACKEND_API.git
cd Dish-Dash_BACKEND_API

2️⃣ Configure Environment Variables

Create a .env file (or update application.properties) with your values:

# MongoDB
spring.data.mongodb.uri=mongodb+srv://<username>:<password>@cluster-url/dbname

# AWS S3
AWS_ACCESS_KEY=your-access-key
AWS_SECRET_KEY=your-secret-key
AWS_BUCKET_NAME=your-bucket-name

# Server
server.port=8080

3️⃣ Build & Run
mvn clean install
mvn spring-boot:run


The backend will start at 👉 http://localhost:8080

📡 API Endpoints (Sample)
Auth

POST /auth/register → Register new user (Customer / Restaurant Owner)

POST /auth/login → Login (session-based)

POST /auth/logout → Logout

Restaurants

POST /restaurants → Create restaurant (Owner only)

GET /restaurants → Get all restaurants

GET /restaurants/{id} → Get restaurant by ID

Food Items

POST /restaurants/{id}/food → Add food item (Owner only)

GET /restaurants/{id}/food → Get food items of a restaurant

PUT /food/{id} → Update food item

DELETE /food/{id} → Delete food item

Orders

POST /orders → Place new order

GET /orders/{id} → Get order details

PUT /orders/{id}/status → Update order status (Owner/Admin)

🧪 Testing

Run unit tests with:

mvn test

📌 Next Steps

Integrate with Dish-Dash Frontend (React + Vite)

Deploy backend to AWS/GCP with MongoDB Atlas

👨‍💻 Author

Developed by Agampal Singh
📌 GitHub: @agxmm01
