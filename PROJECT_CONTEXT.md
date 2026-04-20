Objetivo:
Crear un proyecto simple integrando diferentes tecnologías que se usan en proyectos del tipo
Telco en HACOM. Queremos evaluar tu capacidad para poner en marcha un proyecto simple.

Requisitos guardados (contexto):

1) Crear un proyecto con Spring boot, Gradle y java 17.

2) Utilizar las siguientes dependencias / tecnologías:
   - Spring Data mongodb reactive
   - Spring Webflux
   - Spring Log4j2
   - Spring Actuator

3) Utilizar `application.yml` en vez de `application.properties`.

4) Crear 3 variables en el `application.yml` que deben ser usadas posteriormente para la
   conexión con mongodb y setear el puerto de servidor de Webflux:

   mongodbDatabase: exampleDb
   mongodbUri: "mongodb://127.0.0.1:27017"
   apiPort: 9898

   Importante:
   - Configurar la conexión a mongodb de forma programática, NO utilizar la configuración por
     defecto de Spring (no `spring.data.mongodb.uri`).
   - Configurar el puerto de Webflux de forma programática también.

5) Integrar gRPC con Gradle.
   - Crear un servicio gRPC para insertar pedidos (Order). Campos: orderId, customerId,
     customerPhoneNumber, list of items.
   - Respuesta: orderId y status.

6) Integrar Akka Classic Actors.
   - Crear un actor que procese pedidos ingresados por gRPC y que envíe la respuesta gRPC
     cuando termine el procesamiento.

7) Integración con MongoDB.
   - El actor debe finalizar el pedido insertando la información en mongodb con la entidad:

     public class Order {
      @Id
      private ObjectId _id;
      private String orderId;
      private String customerId;
      private String customerPhoneNumber;
      private String status;
      private List<String> items;
      private OffsetDateTime ts;
     }

   - Usar mongodb-reactive (Spring Data Reactive MongoDB / reactive drivers).

8) Integrar la librería SMPP (cloudhopper-smpp) para envío de SMS.
   - Crear cliente SMPP y enviar SMS con el texto: "Your order " + request.getOrderId() + " has been processed"
     una vez el actor termine de procesar el pedido.

9) Crear una API REST (Webflux):
   - Endpoint para consultar el estado del pedido.
   - Endpoint para consultar el total de pedidos por rango de fecha (usar OffsetDateTime para las fechas).

10) Insertar logs convenientemente en el código.

11) Configurar Log4j2 usando `log4j2.yml` (no XML).

12) Usar Spring Actuator y exponer métricas prometheus; colocar al menos un contador.

Notas adicionales del desarrollador / issues actuales (recopiladas del workspace):
- No usar `spring.data.mongodb.uri` (programmatic config).
- Se desea separar URI en 3 variables: host/uri base, puerto y nombre de bd.
- Se debe evitar valores por defecto en `MongoUriConfig` — la configuración debe tomar las
  variables del `application.yml` (las 3 variables mencionadas) o fallar si no están presentes.
- Error observado al intentar guardar: "Can't find a codec for OffsetDateTime" — requiere
  conversores/codec para `OffsetDateTime` (o mapear a `Instant`/`Date`), y registrar converters
  en la configuración de MongoDB reactiva.
- Error de conexión: `Connection refused` a localhost:27017 — asegurar que MongoDB esté corriendo
  o usar variables para apuntar a la instancia correcta.

Siguientes pasos propuestos (elige uno o indícame otro):
1) Añadir las 3 variables en `src/main/resources/application.yml` tal como pide el enunciado.
2) Añadir dependencias a `build.gradle` para Spring Webflux, reactive MongoDB, Log4j2, Actuator,
   gRPC, Akka, cloudhopper-smpp.
3) Implementar configuración programática de MongoDB reactiva (`MongoConfig` / `MongoUriConfig`),
   incluyendo codecs/converters para `OffsetDateTime`.
4) Configurar puerto Webflux programáticamente (leer `apiPort` desde `application.yml`).
5) Crear `controller` con endpoint GET `/test` y endpoints REST para pedidos.
6) Crear `Order` entity, `OrderRepository` (reactive), `OrderService` (inyectado por constructor),
   y ejemplo de `save` desde controller.
7) Integrar gRPC y Akka (esqueleto), y cliente SMPP (esqueleto).
8) Implementar logs y `log4j2.yml`.
9) Añadir actuator y métricas Prometheus.

Si estás de acuerdo, puedo empezar por (1) y (2): actualizar `application.yml` y `build.gradle` con
las dependencias necesarias y después implementar la configuración programática de MongoDB y
el puerto del servidor Webflux.

Si prefieres que comience por otra tarea (por ejemplo, arreglar `MongoUriConfig` que comentaste),
indícamelo y empiezo por ahí.

---
Archivo guardado en: `PROJECT_CONTEXT.md` (raíz del proyecto)

Fecha de creación: 2026-04-20

