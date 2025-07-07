# Микросервисная система Company-service / Microservices System Company-service

---

## Оглавление / Table of Contents
1. [Архитектура / Architecture](#архитектура--architecture)
2. [Описание сервисов / Services Description](#описание-сервисов--services-description)
3. [Инструкция по запуску / Getting Started](#инструкция-по-запуску--getting-started)
4. [Структура каталогов / Project Structure](#структура-каталогов--project-structure)
5. [Описание компонентов / Components Overview](#описание-компонентов--components-overview)
6. [REST-эндпоинты / REST Endpoints](#rest-эндпоинты--rest-endpoints)
7. [Gateway-маршруты / Gateway Routes](#gateway-маршруты--gateway-routes)
8. [Eureka-конфигурация / Eureka Configuration](#eureka-конфигурация--eureka-configuration)
9. [Рекомендации по Feign / Feign Best Practices](#рекомендации-по-feign--feign-best-practices)

---

## Архитектура / Architecture

**RU:**
Микросервисная система состоит из следующих сервисов:
- company_service — управление компаниями
- user_service — управление пользователями
- config_service — централизованное хранение конфигураций
- eureka_service — сервис-реестр (Service Discovery)
- gateway_service — API Gateway для маршрутизации запросов

Взаимодействие между сервисами реализовано через REST и Feign-клиенты. Все сервисы регистрируются в Eureka и получают конфигурацию из config_service.

**EN:**
The microservices system includes the following services:
- company_service — company management
- user_service — user management
- config_service — centralized configuration storage
- eureka_service — service registry (Service Discovery)
- gateway_service — API Gateway for routing requests

Services interact via REST and Feign clients. All services register in Eureka and fetch configuration from config_service.

---

## Описание сервисов / Services Description

**RU:**
- **company_service** — CRUD-компаний, управление сотрудниками компаний
- **user_service** — CRUD-пользователей, связь пользователя с компанией
- **config_service** — централизованное хранение application.yml для всех сервисов
- **eureka_service** — сервис-реестр для обнаружения и балансировки сервисов
- **gateway_service** — единая точка входа, маршрутизация и проксирование запросов

**EN:**
- **company_service** — CRUD for companies, manage company employees
- **user_service** — CRUD for users, user-company association
- **config_service** — centralized storage for application.yml of all services
- **eureka_service** — service registry for discovery and load balancing
- **gateway_service** — single entry point, routing and proxying requests

---

## Инструкция по запуску / Getting Started

**RU:**
1. Убедитесь, что установлен Docker и Docker Compose
2. В корне проекта выполните:
   ```bash
   docker-compose up --build
   ```
3. Сервисы будут доступны на портах:
   - Gateway: http://localhost:8080
   - Eureka: http://localhost:8761

**EN:**
1. Make sure Docker and Docker Compose are installed
2. In the project root, run:
   ```bash
   docker-compose up --build
   ```
3. Services will be available at:
   - Gateway: http://localhost:8080
   - Eureka: http://localhost:8761

---

## Структура каталогов / Project Structure

```
Company-service/
  company_service/         # Company microservice
  user_service/            # User microservice
  config_service/          # Centralized config
  eureka_service/          # Eureka server
  gateway_service/         # API Gateway
  docker-compose.yml       # Compose file for all services
```

---

## Описание компонентов / Components Overview

**RU:**
- `controller/` — REST-контроллеры
- `service/` — бизнес-логика
- `repository/` — доступ к данным (JPA)
- `dto/` — объекты передачи данных
- `entity/` — сущности БД
- `exception/` — обработка ошибок
- `feign/` — Feign-клиенты для межсервисного взаимодействия
- `mapper/` — преобразование между entity и dto

**EN:**
- `controller/` — REST controllers
- `service/` — business logic
- `repository/` — data access (JPA)
- `dto/` — data transfer objects
- `entity/` — database entities
- `exception/` — error handling
- `feign/` — Feign clients for interservice communication
- `mapper/` — entity-dto mapping

---

## REST-эндпоинты / REST Endpoints

### company_service (через gateway: `/api/companies`)

| Метод | Путь | Описание | Пример запроса |
|-------|------|----------|---------------|
| POST | /api/companies | Создать компанию | `POST /api/companies`<br>Body: `{ "name": "ООО Ромашка" }` |
| PUT | /api/companies/{id} | Обновить компанию | `PUT /api/companies/1`<br>Body: `{ "name": "ООО Лотос" }` |
| PATCH | /api/companies/{id} | Частичное обновление | `PATCH /api/companies/1`<br>Body: `{ "name": "ООО Лотос" }` |
| DELETE | /api/companies/{id} | Удалить компанию | `DELETE /api/companies/1` |
| GET | /api/companies/{id} | Получить по id | `GET /api/companies/1` |
| GET | /api/companies | Список компаний (пагинация) | `GET /api/companies?page=0&size=10` |
| POST | /api/companies/{companyId}/employees/add?employeeId=2 | Добавить сотрудника | `POST /api/companies/1/employees/add?employeeId=2` |
| POST | /api/companies/{companyId}/employees/remove?employeeId=2 | Удалить сотрудника | `POST /api/companies/1/employees/remove?employeeId=2` |

### user_service (через gateway: `/api/users`)

| Метод | Путь | Описание | Пример запроса |
|-------|------|----------|---------------|
| POST | /api/users | Создать пользователя | `POST /api/users`<br>Body: `{ "name": "Иван", ... }` |
| PUT | /api/users/{id} | Обновить пользователя | `PUT /api/users/1`<br>Body: `{ ... }` |
| PATCH | /api/users/{id} | Частичное обновление | `PATCH /api/users/1`<br>Body: `{ ... }` |
| DELETE | /api/users/{id} | Удалить пользователя | `DELETE /api/users/1` |
| GET | /api/users/{id} | Получить по id | `GET /api/users/1` |
| GET | /api/users | Список пользователей (пагинация) | `GET /api/users?page=0&size=10` |
| POST | /api/users/by-ids | Получить список пользователей по id | `POST /api/users/by-ids`<br>Body: `[1,2,3]` |
| DELETE | /api/users/by-company/{companyId} | Удалить всех пользователей компании | `DELETE /api/users/by-company/1` |
| POST | /api/users/{userId}/company?companyId=2 | Изменить компанию пользователя | `POST /api/users/1/company?companyId=2` |

**EN:**

### company_service (via gateway: `/api/companies`)

| Method | Path | Description | Example Request |
|--------|------|-------------|-----------------|
| POST | /api/companies | Create company | `POST /api/companies`<br>Body: `{ "name": "Acme Ltd" }` |
| PUT | /api/companies/{id} | Update company | `PUT /api/companies/1`<br>Body: `{ "name": "Lotus Ltd" }` |
| PATCH | /api/companies/{id} | Partial update | `PATCH /api/companies/1`<br>Body: `{ "name": "Lotus Ltd" }` |
| DELETE | /api/companies/{id} | Delete company | `DELETE /api/companies/1` |
| GET | /api/companies/{id} | Get by id | `GET /api/companies/1` |
| GET | /api/companies | List companies (pagination) | `GET /api/companies?page=0&size=10` |
| POST | /api/companies/{companyId}/employees/add?employeeId=2 | Add employee | `POST /api/companies/1/employees/add?employeeId=2` |
| POST | /api/companies/{companyId}/employees/remove?employeeId=2 | Remove employee | `POST /api/companies/1/employees/remove?employeeId=2` |

### user_service (via gateway: `/api/users`)

| Method | Path | Description | Example Request |
|--------|------|-------------|-----------------|
| POST | /api/users | Create user | `POST /api/users`<br>Body: `{ "name": "John", ... }` |
| PUT | /api/users/{id} | Update user | `PUT /api/users/1`<br>Body: `{ ... }` |
| PATCH | /api/users/{id} | Partial update | `PATCH /api/users/1`<br>Body: `{ ... }` |
| DELETE | /api/users/{id} | Delete user | `DELETE /api/users/1` |
| GET | /api/users/{id} | Get by id | `GET /api/users/1` |
| GET | /api/users | List users (pagination) | `GET /api/users?page=0&size=10` |
| POST | /api/users/by-ids | Get users by ids | `POST /api/users/by-ids`<br>Body: `[1,2,3]` |
| DELETE | /api/users/by-company/{companyId} | Delete all users of company | `DELETE /api/users/by-company/1` |
| POST | /api/users/{userId}/company?companyId=2 | Change user's company | `POST /api/users/1/company?companyId=2` |

---

## Gateway-маршруты / Gateway Routes

**RU:**
- `/api/companies/**` → company-service (без префикса /api)
- `/api/users/**` → user-service (без префикса /api)

**EN:**
- `/api/companies/**` → company-service (without /api prefix)
- `/api/users/**` → user-service (without /api prefix)

---

## Eureka-конфигурация / Eureka Configuration

**RU:**
- Порт: 8761
- Адрес: http://localhost:8761
- Не регистрируется сам в себе
- Все сервисы регистрируются в Eureka для обнаружения друг друга

**EN:**
- Port: 8761
- Address: http://localhost:8761
- Does not self-register
- All services register in Eureka for discovery

---

## Рекомендации по Feign / Feign Best Practices

**RU:**
- Feign-клиенты используйте только в сервисном слое (`service`), не вызывайте их напрямую из контроллеров
- Это повышает тестируемость, гибкость и централизует бизнес-логику

**EN:**
- Use Feign clients only in the service layer, do not call them directly from controllers
- This improves testability, flexibility, and centralizes business logic 