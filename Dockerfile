# Usa la imagen oficial OpenJDK 17 como base
FROM openjdk:17-jdk-slim

# Carpeta de trabajo dentro del contenedor
WORKDIR /app

# Copia solo los archivos necesarios para construir
COPY build.gradle settings.gradle ./
COPY gradlew ./
COPY gradle ./gradle
COPY src ./src

# Ejecuta la compilación
RUN ./gradlew build --no-daemon

# Copia el jar generado al contenedor
COPY build/libs/post.jar ./post.jar

# Expone el puerto en el que corre tu app
EXPOSE 5555

# Comando para correr tu app
CMD ["java", "-jar", "post.jar"]
