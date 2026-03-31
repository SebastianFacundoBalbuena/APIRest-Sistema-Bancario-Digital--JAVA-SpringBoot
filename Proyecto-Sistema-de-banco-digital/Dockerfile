#  Usamos una imagen oficial de Java 17
#    eclipse-temurin:17-jdk,es la imagen oficial y actualizada de Java 17.
FROM eclipse-temurin:17-jdk

#Guardamso en la variable JAR_FILE la ruta del archivo .jar , no el archivo en sí.
ARG JAR_FILE=target/banco-0.0.1-SNAPSHOT.jar

#Creamos y movemos todo a la carpeta app
#Todo lo que haga después (copiar, ejecutar) va a ocurrir dentro de /app
WORKDIR /app

#Copiamos el JAR de nuestra máquina al contenedor
COPY ${JAR_FILE} app.jar

#Le decimos a Docker que la app usa el puerto 8080
EXPOSE 8080

#comandos que se ejeucta cuando arranca el contendor
ENTRYPOINT [ "java", "-jar", "app.jar" ]