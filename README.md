### Заполненные данные в БД
При запуске приложения база данных будет проинициализирована ролями и записями о пользователях. 

__Пользователь с правами админа__ имеет следующие креды:
* _username =_ "admin"
* _password =_ "admin"

__Пользователь с правами пользователя__ имеет следующие креды:
* _username =_ "user"
* _password =_ "user"

### Принцип регистрации
По конечной точке `/auth/signup` отправляется `SignupRequest`, содержащий информацию о пользователе. Пример request body:

```json
{
  "username": "username",
  "email": "email@mail.com",
  "password": "not-hashed-password"
}
```

Пример успешного ответа о регистрации: 

```json
{
  "userId": 14,
  "token": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzb21lIHVzZXIiLCJpYXQiOjE3MzEzNDg5MDAsImV4cCI6MTczMzk0MDkwMH0.suwbG98JTi8kZ9_MeRh-gun3fxFCUcAVvKSRZLrV5a4",
  "expiresIn": 2592000000,
  "roles": ["USER"]
}
```

### Принцип авторизации
По конечной точке `/auth/login` отправляется `LoginRequest`, содержащий информацию о имени пользователя и не захэшированный пароль. Пример request body:

```json
{
  "username": "username",
  "password": "not-hashed-password"
}
```

Пример ответа об успешной авторизации:

```json
{
  "token": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTczMTA3NzUzOSwiZXhwIjoxNzMzNjY5NTM5fQ.FWAPvPUGfeW5SwOealj0H8QSl9O3I7rOoes6BtF_Acw",
  "expiresIn": 2592000000
}
```

### Идентификация пользователей
На остальные конечные точки `/user/**` необходимо отправлять вместе с запросом заголовок `Authorization` со значением = "Bearer + ${your_access_token}". При получении такого запроса отработает `JwtAuthenticationInterceptor` и, если токен не валидный или недостаточно прав у данного пользователя для доступа к данному ресурсу, то вернется HTTP ответ `403 Forbidden` с соответствующим сообщением. 