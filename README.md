# Distributed Product Management System

This project implements a simple **Distributed Product Management System** using Java. It follows a **client-server architecture** where:
- A **NameServer** keeps track of different product tables and their respective servers.
- Multiple **Database Servers** store product data dynamically.
- A **Client** interacts with the system to insert, delete, and query products.

---

## 🛠 Setup & Compilation

### Setup the Project
#### Go to this GitHub repository and clone the project: https://github.com/Akatzz12/Distributed-Product-Management-System
#### cd Distributed Product Management System
#### mkdir bin

### Compilation

### 1️⃣ Compile the Java files
```sh
javac -d bin src/client/*.java src/server/*.java
```

### 2️⃣ Start the NameServer  
This server keeps track of all product servers.
```sh
java -cp bin server.NameServer
```

### 3️⃣ Start a Database Server  
Each product category (table) has its own server. To start a new server for a category (e.g., `electronics`):
```sh
java -cp bin server.DatabaseServer electronics
```

To start a server for another category (e.g., `grocery`):
```sh
java -cp bin server.DatabaseServer grocery
```

### 4️⃣ Run the Client  
The client interacts with the servers to add, remove, and fetch product details.
```sh
java -cp bin client.Client
```

### Note: Run NameServer, Database Server and Client on different terminal.
---

## 📝 Supported Commands

Inside the Client program, you can use the following commands:

### Insert a product (PUT)
```sh
PUT electronics Laptop Dell
```
*Adds a Laptop to the electronics database.*

### Query a product (GET)
```sh
GET electronics Laptop
```
*Fetches the details of the Laptop in the electronics category.*

### Delete a product (DEL)
```sh
DEL electronics Laptop
```
*Removes the Laptop from the electronics database.*

---


