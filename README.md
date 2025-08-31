# 🚀 Bank System - Proyecto Bootcamp Java

![GitHub repo size](https://img.shields.io/github/repo-size/usuario/bank-sys?color=blue)
![GitHub contributors](https://img.shields.io/github/contributors/usuario/bank-sys?color=green)
![GitHub last commit](https://img.shields.io/github/last-commit/usuario/bank-sys?color=red)
![GitHub stars](https://img.shields.io/github/stars/usuario/bank-sys?style=social)

---

## ✨ Entregable 2
Sistema bancario para el banco **XYZ** que permite gestionar **clientes y cuentas bancarias**, incluyendo **depósitos y retiros**.  
El proyecto está dividido en **microservicios independientes** que se comunican entre sí y se documenta con **OpenAPI**.  

Parte del **Bootcamp Tech Girls Power - Proyecto II**.  

---


![Demo](https://media.giphy.com/media/26AHONQ79FdWZhAI0/giphy.gif)

---

## ⚙️ Tecnologías
- ☕ Java 17  
- 🗄️ MySQL  
- 🌱 Spring Boot  
- 📜 Spring Data JPA (Hibernate)  
- 📡 OpenAPI (Contract First)  
- 🏗️ Microservicios (OpenFeing, CustomerMs y AccountMs)  
- 🛠️ Maven  
- 💻 IntelliJ IDEA / STS  

---

## 📂 Estructura del Proyecto
## Account-MS

![ACCOUNT_API](./images/AccountAPI.png)

```bash
bank-sys/account-ms
│── src/
│   ├── main/java/com/xyz/bank/account_ms
│   │    ├── controller/
│   │    ├── entity/
│   │    ├── mapper/
│   │    ├── model/
│   │    ├── repository/
│   │    ├── service/
│   │    ├──── impl/
│   ├── resources/
│   │    ├── account-api-yaml (Contrato)
│── pom.xml
│── README.md
```

## Customer-MS

![CUSTOMER_API](./images/CustomerAPI.png)

```bash
bank-sys/customer-ms
│── src/
│   ├── main/java/com/xyz/bank/customer_ms
│   │    ├── controller/
│   │    ├── entity/
│   │    ├── mapper/
│   │    ├── model/
│   │    ├── repository/
│   │    ├── service/
│   │    ├──── impl/
│   ├── resources/
│   │    ├── customer-api-yaml (Contrato)
│── pom.xml
│── README.md
