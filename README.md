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
