## Список задач

1. Починить тесты [commit #7](https://github.com/xfxom/test-backend/commit/0b5b83e039cf091ef199a4f043fe0b616e60ebea)
  * `testBudgetPagination` - некорректно работает пагинация + неправильно считается общая статистика записей 
  * `testStatsSortOrder` - необходимо реализовать сортировку выдачи в указанном порядке 
2. Из модели `BudgetType` через миграцию БД убрать значение `Комиссия`, заменив его на `Расход` [commit #4](https://github.com/xfxom/test-backend/commit/daee1a4a31b3c25e599f88bf8432ea8a82410b2e)
3. Добавить таблицу `Author` - автор внесения записи. [commit #5](https://github.com/xfxom/test-backend/commit/911f75e2a8fd4b9d89593feeb31ed08fd489c383)
   * 3 колонки - `ID`, `ФИО`, `Дата создания` (дата-время). 
   * Добавить в апи метод создания новой записи в `Author`. На вход передается ФИО, дата создания проставляется сервером автоматически.
   * В `BudgetTable` добавить опциональную привязку по `Author.id`
   * Дополнить `/budget/add` возможностью указать ID автора (опциональное поле)
   * В элементах ответа `/budget/year/{year}/stats` выводить ФИО автора, если он указан для записи, а также время создания записи автора. [commit #6](https://github.com/xfxom/test-backend/commit/4bace5dbc70705b16eb74d49b1c0b1d4851b3d90)
   * Добавить в параметры запроса `/budget/year/{year}/stats` опциональный фильтр по ФИО автора и фильтровать по совпадению подстроки игнорируя регистр [commit #6](https://github.com/xfxom/test-backend/commit/4bace5dbc70705b16eb74d49b1c0b1d4851b3d90)

## Запуск

### Базовый запуск

```shell
docker-compose up -d --build
```

### Запуск тестов

```shell
docker-compose --profile tests up --build tests
```


## Endpoints

### Авторы

#### Создать автора

`POST /author/create`

**Тело запроса**:

```json
{
  "fullName": "Иванов Иван Иванович"
}
```

**Ответ**:

```json
{
  "id": 1,
  "fullName": "Иванов Иван Иванович",
  "createdAt": "2024-03-15T12:34:56.789Z"
}
```

### Бюджет

#### Добавить запись

`POST /budget/add`

**Тело запроса**:

```json
{
  "year": 2024,
  "month": 3,
  "amount": 15000,
  "type": "Приход",
  "authorId": 1
}
```

**Ответ**:

```json
{
  "year": 2024,
  "month": 3,
  "amount": 15000,
  "type": "Приход",
  "authorId": 1,
  "authorFullName": "Иванов Иван Иванович",
  "authorCreatedAt": "2024-03-15T12:34:56.789Z"
}
```

#### Получить статистику за год

`GET /budget/year/{year}/stats`

**Параметры**:

- `year` - целевой год (обязательный)
- `limit` - количество записей на странице
- `offset` - смещение пагинации
- `authorName` - фильтр по ФИО автора (частичное совпадение)

**Пример запроса**:

```
GET /budget/year/2024/stats?limit=10&offset=0&authorName=Иван
```

**Ответ**:

```json
{
  "total": 100,
  "totalByType": {
    "Приход": 750000,
    "Расход": 500000
  },
  "items": [
    {
      "year": 2024,
      "month": 3,
      "amount": 15000,
      "type": "Приход",
      "authorId": 1,
      "authorFullName": "Иванов Иван Иванович",
      "authorCreatedAt": "2024-03-15T12:34:56.789Z"
    }
  ]
}
```

## Валидация

- `year`: минимальное значение 1900
- `month`: от 1 до 12
- `amount`: минимальное значение 1
- `type`: только "Приход" или "Расход"

## Типы данных

```kotlin
enum class BudgetType {
    Приход,
    Расход
}

data class BudgetYearStatsResponse(
    val total: Int,
    val totalByType: Map<String, Int>,
    val items: List<BudgetRecordResponse>
)
```

## Swagger

API будет доступно по адресу: `http://localhost:8080/swagger-ui/index.html?url=/openapi.json`

> Swagger документация доступна по адресу: `/swagger-ui/index.html`


