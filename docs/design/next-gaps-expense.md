# Next API Gaps - Expense Module

> Ergou expense module vs Next backend API coverage analysis.

## Currently Implemented in Ergou

| Endpoint | Status |
|----------|--------|
| `GET /api/expenses` | OK (list with from/to/tags filter) |
| `POST /api/expenses` | OK (create with amount/notes/tags/currency/date/items) |
| `GET /api/expenses/{id}` | OK (get detail) |
| `PUT /api/expenses/{id}` | OK (update) |
| `DELETE /api/expenses/{id}` | OK |
| `GET /api/expenses/summary` | OK (period + date params) |
| `GET /api/expenses/tags` | OK (dynamic tag fetching) |

## Not Yet Implemented in Ergou

### 1. Analytics Endpoint
- **Route**: `GET /api/expenses/analytics?period=week|month&date=YYYY-MM-DD`
- **Returns**: Category breakdown (9 categories with amounts/percentages) + daily distribution
- **Use case**: Rich charts, spending trend analysis
- **Priority**: Medium (nice UX improvement for monthly spending insights)

### 2. Exchange Rate Endpoint
- **Route**: `GET /api/expenses/rates`
- **Returns**: `{ base: "CAD", rates: { "CNY": 5.12 } }`
- **Use case**: Display CNY equivalent for CAD expenses (or vice versa)
- **Priority**: Low (functional without it, summary just shows raw amounts)

### 3. Photo Upload
- **Route**: `POST /api/expenses/{id}/photos` (multipart/form-data)
- **Returns**: Photo metadata
- **Use case**: Attach receipt photos to expense entries
- **Priority**: Medium (aligns with AI receipt parsing feature)

### 4. AI Receipt Parsing
- **Route**: `POST /api/expenses/parse-preview` (base64 images + optional text)
- **Returns**: Parsed merchant, items, tags, amounts from receipt image
- **Route**: `POST /api/expenses/{id}/parse` (parse existing entry's photos)
- **Use case**: Camera capture receipt -> auto-fill expense
- **Priority**: High (killer feature, but requires camera integration)

### 5. Photo Management
- **Route**: `DELETE /api/expenses/photos/{photo_id}`
- **Route**: `GET /api/expenses/uploads/{user_id}/{filename}`
- **Use case**: View/delete receipt photos
- **Priority**: Low (blocked by photo upload implementation)

## DTO Fixes Applied (This Session)

| Field | Before | After | Reason |
|-------|--------|-------|--------|
| `NextExpenseItem.quantity` | `Int = 1` | `Double = 1.0` | Backend uses REAL for weight-based items (e.g., 0.5kg meat) |
| `NextExpenseItem.unitPrice` | `Double = 0.0` | `Double? = null` | Backend field is nullable |
| `NextExpenseItemRequest.quantity` | `Int = 1` | `Double = 1.0` | Match backend schema |
| `NextExpenseItemRequest.unitPrice` | `Double = 0.0` | `Double? = null` | Match backend schema |

## API Bug Fixed

| Issue | Before | After |
|-------|--------|-------|
| `ExpensePeriod.TODAY.apiValue` | `"today"` | `"day"` |
| **Impact**: Summary endpoint returned empty/error for daily period because backend only accepts "day"/"week"/"month" |

## Next Backend Category Mapping (Reference)

The backend maps individual tags to 9 analytical categories for reporting:

| Category | Tags |
|----------|------|
| food_grocery | supermarket, grocery, meat, vegetables, fruit, seafood, snacks, drinks, dairy, spices, bread, fresh, ingredients |
| dining | dining, delivery, restaurant, coffee, milk_tea, breakfast, lunch, dinner, hotpot, fast_food, dessert, bar |
| transport | transport, gas, parking, bus, subway, taxi, train, flights, fuel, car_rental |
| shopping | shopping, clothes, shoes, electronics, digital, home, appliances, daily_needs, cosmetics |
| housing | housing, rent, utilities, internet, property, repairs, furniture, phone_bill |
| entertainment | entertainment, movies, games, travel, attractions, KTV, sports, gym |
| healthcare | medical, medicine, doctor, checkup, dental, supplements |
| education | education, books, courses, training, stationery |
| other | (anything unrecognized) |
