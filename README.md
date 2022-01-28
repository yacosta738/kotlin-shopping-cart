# shoppingCart

Ejemplo de un carrito de compras simple en kotlin 

## Desarrollo

Para iniciar tu aplicación en el perfil de desarrollo, ejecuta:

```bash
docker-compose -f src/main/docker/postgresql.yml up -d

./mvnw
```

## Construir para producción

### Empaquetar como jar

Para empaquetar como jar, ejecuta:

```
./mvnw -Pprod clean verify
```

Para comprobar que todo está bien, ejecuta:

```
java -jar target/*.jar
```

## Testing

Para ejecutar los tests, ejecuta:

```bash
./mvnw verify
```


## Ejemplos de los endpoints 

Autenticarse en la aplicación y obtener el token 

```bash
curl --location --request POST 'http://localhost:8080/api/authenticate' \
--header 'accept: */*' \
--header 'Content-Type: application/json' \
--data-raw '{
  "username": "admin",
  "password": "admin",
  "rememberMe": true
}'
```

Este nos retornará un jwt similar al siguiente:

```
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfVVNFUiIsImV4cCI6MTY0NTgzMjk1Mn0.ro9fxhqoiDQ7_JyPPjhJsKkOuic2JlYZ_l4TPjbmwaWcdmuUrPASeGHaGs2DVk9r3OrjnGuf0M-uYv_oQuK4DQ
```

Este token lo usaremos en todas las peticiones 

Crear un carrito para el usuario logueado

```bash
curl --location --request POST 'http://localhost:8080/api/carts' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfVVNFUiIsImV4cCI6MTY0NTgzMjk1Mn0.ro9fxhqoiDQ7_JyPPjhJsKkOuic2JlYZ_l4TPjbmwaWcdmuUrPASeGHaGs2DVk9r3OrjnGuf0M-uYv_oQuK4DQ' \
--header 'Content-Type: application/json' \
--header 'Cookie: Cookie_1=value' \
--data-raw '{
    "state":"PENDING"
}'
```

Listar el carrito creado

```bash
curl --location --request GET 'http://localhost:8080/api/carts/0c4a01c9-4928-4b7b-ae2d-c1a939b89abd' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfVVNFUiIsImV4cCI6MTY0NTgzMjk1Mn0.ro9fxhqoiDQ7_JyPPjhJsKkOuic2JlYZ_l4TPjbmwaWcdmuUrPASeGHaGs2DVk9r3OrjnGuf0M-uYv_oQuK4DQ' \
--header 'Cookie: Cookie_1=value'
```

Crear un Producto
```bash
curl --location --request POST 'http://localhost:8080/api/products' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfVVNFUiIsImV4cCI6MTY0NTgzMjk1Mn0.ro9fxhqoiDQ7_JyPPjhJsKkOuic2JlYZ_l4TPjbmwaWcdmuUrPASeGHaGs2DVk9r3OrjnGuf0M-uYv_oQuK4DQ' \
--header 'Content-Type: application/json' \
--header 'Cookie: Cookie_1=value' \
--data-raw '{
    "name":" Pepsi",
    "sku": "some sku",
    "description": "some nice description",
    "price": 5.8
}'
```

Agregar un producto al carrito

```bash
curl --location --request POST 'http://localhost:8080/api/carts/3066598f-6d41-4d20-ba94-aa49f7739a74/add-product' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfVVNFUiIsImV4cCI6MTY0NTgzMjk1Mn0.ro9fxhqoiDQ7_JyPPjhJsKkOuic2JlYZ_l4TPjbmwaWcdmuUrPASeGHaGs2DVk9r3OrjnGuf0M-uYv_oQuK4DQ' \
--header 'Content-Type: application/json' \
--header 'Cookie: Cookie_1=value' \
--data-raw '{
    "productId":"d48a4730-9b82-4587-a4b0-0231c219bf63",
    "quantity": 12
}'
```

Listar los productos de un carrito

```bash
curl --location --request GET 'http://localhost:8080/api/carts/25f1aa77-3689-4347-9726-7ea6cb2370d9/products' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfVVNFUiIsImV4cCI6MTY0NTgzMjk1Mn0.ro9fxhqoiDQ7_JyPPjhJsKkOuic2JlYZ_l4TPjbmwaWcdmuUrPASeGHaGs2DVk9r3OrjnGuf0M-uYv_oQuK4DQ' \
--header 'Cookie: Cookie_1=value'
```

La operación de checkout

```bash
curl --location --request GET 'http://localhost:8080/api/carts/3066598f-6d41-4d20-ba94-aa49f7739a74/total-price' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfVVNFUiIsImV4cCI6MTY0NTgzMjk1Mn0.ro9fxhqoiDQ7_JyPPjhJsKkOuic2JlYZ_l4TPjbmwaWcdmuUrPASeGHaGs2DVk9r3OrjnGuf0M-uYv_oQuK4DQ' \
--header 'Cookie: Cookie_1=value'
```


Otras operaciones están listadas en la documentacion de postman
