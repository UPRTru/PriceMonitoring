# PriceMonitoring — Система мониторинга цен на валюты и драгоценные металлы

## Описание проекта

**PriceMonitoring** — это распределённое приложение для отслеживания курсов валют и цен на драгоценные металлы в режиме реального времени. Система автоматически собирает данные с банковских сайтов, хранит историю изменений и уведомляет пользователей о достижении целевых цен.

### Основные возможности

- **Автоматический сбор данных** — парсинг актуальных курсов с сайтов банков (SberBank и др.)
- **Мониторинг драгоценных металлов** — золото, серебро, платина, палладий
- **Мониторинг валют** — USD, EUR, CNY, и другие популярные валюты
- **Целевые уведомления** — установка желаемой цены, при достижении которой отправляется email-уведомление
- **История изменений** — хранение и просмотр истории изменения цен
- **Мультипользовательская система** — регистрация, аутентификация, персональные настройки
- **Распределённая архитектура** — микросервисная структура с разделением ответственности

---

## Архитектура проекта

Проект состоит из 4 модулей:

| Модуль | Описание |
|--------|----------|
| **shared** | Общие DTO, enum-ы, исключения и утилиты для всех модулей |
| **bank-agent** | Агент сбора данных с банковских сайтов (Web scraping) |
| **user-service** | Управление пользователями, аутентификация, запланированные цены |
| **general-service** | Шлюз для проверки цен, отправка email-уведомлений |

---

## Принципы Effective Java в проекте

При разработке проекта были применены следующие принципы из книги Джошуа Блоха **"Effective Java"**:

### Item 1 — Статические фабричные методы
Вместо публичных конструкторов используются статические методы создания объектов (`of()`, `createWithCurrentTime()`, `createWithTimestamp()`). Это улучшает читаемость кода и даёт гибкость в возвращаемых типах.

### Item 2 — Builder для объектов с множеством параметров
Для DTO с большим количеством полей (`Price`, `CheckPrice`) реализован паттерн Builder. Это упрощает создание объектов и делает код более выразительным.

### Item 3 — Enum Singleton
Все перечисления (`Banks`, `Currency`, `Metal`, `TypePrice`, `CurrentPrice`) реализованы как enum, что гарантирует singleton для каждого значения и обеспечивает типобезопасность.

### Item 5 — Внедрение зависимостей (Dependency Injection)
Все сервисы, контроллеры и клиенты используют **конструкторную инъекцию** зависимостей. Никакого field injection — зависимости явно объявляются в конструкторе.

### Item 9 — try-with-resources
Ресурсы, требующие закрытия (`WebDriverSupport`, `HttpURLConnection`), реализуют `AutoCloseable` и корректно освобождаются через try-with-resources или finally-блоки.

### Item 10–12 — Переопределение equals, hashCode, toString
- **JPA Entity** — `equals()` и `hashCode()` реализованы только по ID (как рекомендуется для сущностей с persistence context)
- **DTO и Records** — используют автоматическую генерацию через record или переопределяют методы корректно
- **toString()** — реализован для отладки и логирования

### Item 17 — Неизменяемость (Immutability)
- **Records** (`AuthDto`, `CheckPrice`, `Price`, `Message`, `RegistrationForm`) — обеспечивают неизменяемость на уровне языка
- **Коллекции** — возвращаются как `unmodifiableList`/`unmodifiableMap` для защиты от внешних модификаций
- **Final поля** — все поля в DTO и entity помечены как final где это возможно

### Item 19 — Классы и методы final
Классы, не предназначенные для наследования, объявлены как `final` (контроллеры, сервисы, утилиты). Это защищает от непреднамеренного наследования и улучшает безопасность.

### Item 44 — Стандартные функциональные интерфейсы
Вместо создания собственных функциональных интерфейсов используются стандартные из `java.util.function` (Predicate, Function, Consumer) в сочетании со Stream API.

### Item 50 — Использование Map для частых операций поиска
Enum-ы используют предварительно созданные `Map` для быстрого поиска по коду или имени (`BY_NAME`, `BY_CODE`), вместо линейного перебора через `stream().filter()`.

### Item 60 — Правильная обработка исключений
- **Checked vs Unchecked** — `RuntimeException` для ошибок валидации, checked exceptions для внешних сервисов
- **Специфичные исключения** — `BadRequestException`, `NotFoundException`, `ConflictException` вместо общего `Exception`
- **Информативные сообщения** — все исключения содержат понятные сообщения об ошибке

### Item 64 — Интерфейсы вместо классов для типов
- Возвращаемые типы методов — интерфейсы (`Map`, `List`, `Optional`, `Mono`) вместо конкретных реализаций (`HashMap`, `ArrayList`)
- Это обеспечивает гибкость и позволяет менять реализацию без изменения сигнатур методов

---

## Технологии

| Категория | Технологии |
|-----------|------------|
| **Язык** | Java 17+ |
| **Фреймворк** | Spring Boot 3.x |
| **Базы данных** | PostgreSQL, JPA/Hibernate |
| **Web Client** | Spring WebFlux (WebClient) |
| **Браузерная автоматизация** | Selenium WebDriver |
| **Безопасность** | Spring Security, BCrypt |
| **Почта** | JavaMail Sender |
| **Планировщик** | Spring Scheduling (@Scheduled) |
| **Повторные попытки** | Spring Retry (@Retryable) |

---

# Список всех endpoints проекта PriceMonitoring

## general-service (Порт: 8080)

### DashboardController

| Метод | URL | Описание |
|-------|-----|----------|
| `GET` | `/` | Главная страница. Перенаправляет на доступный модуль (bank-agent или user-service) |
| `GET` | `/api/bank_module` | Перенаправление на bank-agent модуль если он доступен |
| `GET` | `/api/modules/status` | Проверка статуса всех модулей. Возвращает JSON с состоянием bank-agent и user-service |

### PriceController

| Метод | URL | Описание |
|-------|-----|----------|
| `POST` | `/check/{email}` | Проверка цены для пользователя. Принимает `CheckPrice` в body, отправляет email если цена достигнута |

---

## bank-agent (Порт: 8081)

### SberPriceController

#### Металлы

| Метод | URL | Описание |
|-------|-----|----------|
| `GET` | `/sber/metal/lastprice/{metalName}` | Получение последней цены металла по названию |
| `GET` | `/sber/metal/all` | Получение всех последних цен металлов |
| `GET` | `/sber/metal/history/{metalName}` | Получение истории цен металла за период (параметры: `from`, `to`) |

#### Валюты

| Метод | URL | Описание |
|-------|-----|----------|
| `GET` | `/sber/currency/lastprice/{currencyName}` | Получение последней цены валюты по названию |
| `GET` | `/sber/currency/all` | Получение всех последних цен валют |
| `GET` | `/sber/currency/history/{currencyName}` | Получение истории цен валюты за период (параметры: `from`, `to`) |

### DashboardController

| Метод | URL | Описание |
|-------|-----|----------|
| `GET` | `/health` | Health check endpoint. Возвращает "OK" если сервис доступен |

---

## user-service (Порт: 8082)

### UserController

#### Аутентификация

| Метод | URL | Описание |
|-------|-----|----------|
| `GET` | `/` | Главная страница. Перенаправляет на dashboard если пользователь авторизован |
| `GET` | `/login` | Страница входа в систему |
| `POST` | `/login` | Обработка формы входа (email, password) |
| `GET` | `/logout` | Выход из системы. Очищает сессию и security context |
| `GET` | `/register` | Страница регистрации нового пользователя |
| `POST` | `/register` | Обработка формы регистрации (email, password, timezone) |

#### Dashboard

| Метод | URL | Описание |
|-------|-----|----------|
| `GET` | `/dashboard` | Главная страница пользователя. Показывает запланированные цены |

#### Запланированные цены

| Метод | URL | Описание |
|-------|-----|----------|
| `POST` | `/add/scheduled_price` | Добавление новой запланированной цены для мониторинга |
| `POST` | `/api/prices/{id}/delete` | Удаление запланированной цены по ID |
| `POST` | `/api/users/delete` | Удаление пользователя и всех его данных |

### DashboardController

| Метод | URL | Описание |
|-------|-----|----------|
| `GET` | `/health` | Health check endpoint. Возвращает "OK" если сервис доступен |

---

## Сводная таблица всех endpoints

| Модуль | Endpoint | Метод | Назначение |
|--------|----------|-------|------------|
| **general-service** | `/` | GET | Роутинг на доступные модули |
| **general-service** | `/api/bank_module` | GET | Перенаправление на bank-agent |
| **general-service** | `/api/modules/status` | GET | Статус всех модулей |
| **general-service** | `/check/{email}` | POST | Проверка цены + email уведомление |
| **bank-agent** | `/sber/metal/lastprice/{name}` | GET | Последняя цена металла |
| **bank-agent** | `/sber/metal/all` | GET | Все цены металлов |
| **bank-agent** | `/sber/metal/history/{name}` | GET | История цен металла |
| **bank-agent** | `/sber/currency/lastprice/{name}` | GET | Последняя цена валюты |
| **bank-agent** | `/sber/currency/all` | GET | Все цены валют |
| **bank-agent** | `/sber/currency/history/{name}` | GET | История цен валюты |
| **bank-agent** | `/health` | GET | Health check bank-agent |
| **user-service** | `/` | GET | Главная / редирект на dashboard |
| **user-service** | `/login` | GET/POST | Вход в систему |
| **user-service** | `/register` | GET/POST | Регистрация пользователя |
| **user-service** | `/logout` | GET | Выход из системы |
| **user-service** | `/dashboard` | GET | Личный кабинет пользователя |
| **user-service** | `/add/scheduled_price` | POST | Добавить мониторинг цены |
| **user-service** | `/api/prices/{id}/delete` | POST | Удалить мониторинг цены |
| **user-service** | `/api/users/delete` | POST | Удалить аккаунт |
| **user-service** | `/health` | GET | Health check user-service |

---

## Внешние зависимости (URL конфигурации)

| Переменная | Описание | Пример значения |
|------------|----------|-----------------|
| `bank.agent.url` | Внутренний URL bank-agent сервиса | `http://localhost:8081` |
| `bank.agent.out-url` | Внешний URL bank-agent для редиректов | `http://localhost:8081` |
| `user.service.url` | Внутренний URL user-service | `http://localhost:8082` |
| `user.service.out-url` | Внешний URL user-service для редиректов | `http://localhost:8082` |
| `general.service.url` | Внутренний URL general-service | `http://localhost:8080` |
| `spring.datasource.url` | URL подключения к PostgreSQL | `jdbc:postgresql://localhost:5432/pricemonitoring` |
| `spring.mail.host` | SMTP сервер для отправки email | `smtp.example.com` |