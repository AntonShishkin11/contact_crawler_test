# 📡 Мониторинг Contact Crawler

## 📌 Описание проекта

Проект демонстрирует мониторинг и профилирование Spring Boot приложения
с использованием:

-   **Prometheus** --- сбор метрик\
-   **Grafana** --- визуализация\
-   **Micrometer** --- интеграция метрик в Spring Boot\
-   **VisualVM** --- анализ CPU, памяти и потоков\
-   **Docker / Docker Compose** --- запуск инфраструктуры

Приложение представляет собой простой **Contact Crawler**, который
выполняет HTTP-сканирование переданных URL и сохраняет найденные данные.

Цель работы --- проанализировать поведение приложения под нагрузкой и
оценить состояние JVM.

------------------------------------------------------------------------

# 🏗 Архитектура

Spring Boot (Contact Crawler) ↓ Micrometer ↓ Prometheus ↓ Grafana

Дополнительно:

VisualVM ├── CPU Profiling ├── Heap Dump └── Thread Dump

------------------------------------------------------------------------

# ⚙ Используемые технологии

-   Java 17
-   Spring Boot
-   Micrometer
-   Prometheus
-   Grafana
-   Docker
-   VisualVM
-   H2 Database

------------------------------------------------------------------------

# 🚀 Запуск проекта

## 1️⃣ Запуск Prometheus и Grafana

``` bash
docker-compose up -d
```

После запуска:

-   Prometheus → http://localhost:9090\
-   Grafana → http://localhost:3000

------------------------------------------------------------------------

## 2️⃣ Запуск приложения

``` bash
mvn spring-boot:run
```

Приложение будет доступно:

http://localhost:8080

Метрики:

http://localhost:8080/actuator/prometheus

------------------------------------------------------------------------

## 3️⃣ Генерация нагрузки

Пример запроса:

``` bash
curl -X POST http://localhost:8080/crawl   -H "Content-Type: application/json"   -d '{"seedUrls":["https://spring.io","https://openjdk.org"]}'
```

Множественная нагрузка:

``` bash
for i in {1..10}; do
  curl -X POST http://localhost:8080/crawl   -H "Content-Type: application/json"   -d '{"seedUrls":["https://spring.io"]}'
done
```

------------------------------------------------------------------------

# 📊 Реализованные метрики

## Метрики приложения

-   crawler_running_jobs
-   crawler_jobs_success_total
-   crawler_jobs_error_total
-   crawler_job_duration
-   crawler_records_saved_total
-   http.server.requests

## Метрики JVM

-   Использование heap памяти
-   GC pause
-   Количество потоков
-   Загрузка CPU
-   Пулы соединений HikariCP

------------------------------------------------------------------------

# 🔬 Профилирование (VisualVM)

## CPU

-   Пики при выполнении crawl-задач
-   Основная нагрузка связана с HTTP-запросами и JSoup

## Потоки

-   Tomcat worker threads (http-nio-8080-exec-\*)
-   Потоки краулера (crawler-thread-\*)
-   Потоки HikariCP
-   GC threads

Deadlock не обнаружено.

## Heap

-   Основное потребление: byte\[\], String
-   Утечек памяти не выявлено
-   G1 GC работает стабильно

------------------------------------------------------------------------

# 📉 Наблюдения под нагрузкой

-   CPU растёт пропорционально числу задач
-   Heap стабилен
-   GC паузы короткие
-   Throughput коррелирует с завершением задач
-   Потоков достаточно

------------------------------------------------------------------------

# ✅ Вывод

-   Настроен мониторинг Spring Boot через Prometheus + Grafana
-   Реализованы кастомные метрики
-   Построен дашборд
-   Проведён анализ CPU, heap и потоков
-   Критических узких мест не обнаружено

Проект демонстрирует базовый production-подход к мониторингу
Java-приложений.

------------------------------------------------------------------------

# 📂 Структура проекта

contact-crawler/ ├── src/ ├── docker-compose.yml ├── prometheus.yml ├──
pom.xml ├── README.md └── screenshots/
