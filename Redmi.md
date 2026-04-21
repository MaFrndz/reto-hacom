# Guía rápida del proyecto (en español)

Checklist de cambios/objetivos de este documento:
- Describir la arquitectura y los componentes principales.
- Explicar el flujo completo cuando se invoca InsertOrder (gRPC → Akka → Mongo → SMPP).
- Indicar cómo ver métricas Prometheus expuestas por Spring Actuator.
- Proveer comandos de ejemplo para: InsertOrder (grpcurl), consultar estado del pedido y getOrdersCountByDateRange (curl).
- Señalar la configuración de logging con `log4j2.yml`.

Resumen de la arquitectura
--------------------------
El proyecto expone un servicio gRPC para recibir pedidos y una API REST para consultas. Internamente usa:

- gRPC server: recibe la petición InsertOrder desde clientes (por ejemplo usando grpcurl).
- Akka (actor system): un actor procesa cada pedido entrante. El actor es responsable de la lógica de negocio (validaciones, transformaciones, persistencia y acciones secundarias como enviar SMS).
- Spring Data / MongoDB: persistencia de los pedidos. El repositorio guarda los documentos en la base de datos.
- SMPP client (cloudhopper-smpp): cliente para enviar SMS al finalizar el procesamiento del pedido.
- Spring Boot Actuator + Prometheus: expone métricas en `/actuator/prometheus`.
- Log4j2 (archivo `log4j2.yml` en `src/main/resources`) como sistema de logging.

Cómo interactúan los componentes (flujo de un pedido)
----------------------------------------------------
1. Un cliente llama al método gRPC `InsertOrder` (puede hacerse con `grpcurl`).
2. El servicio gRPC (un bean de Spring que implementa la interfaz generada) recibe la petición.
3. El servicio crea o envía un mensaje a un actor de Akka (por ejemplo, un actor por pedido o un actor router) para procesar el pedido asincrónicamente.
4. El actor realiza la lógica de negocio:
   - valida datos y prepara el objeto de dominio,
   - persiste el pedido en MongoDB a través del repositorio Spring Data (operación `save`),
   - al completar el guardado correctamente, prepara/ejecuta el envío de SMS vía SMPP, y
   - finalmente responde al invocador gRPC (o completa el futuro/promesa con la respuesta) indicando el resultado.
5. El gRPC responde al cliente con el resultado (éxito/fracaso) una vez que el actor reporta que terminó.

Notas importantes sobre la persistencia y errores frecuentes
-----------------------------------------------------------
- Asegúrate de que la URI de conexión a MongoDB esté correctamente configurada en `application.yaml`. En tu caso, si usas MongoDB Atlas, la URI tiene este formato:
  mongodb+srv://<usuario>:<password>@<host>/<dbname>?options
  (ejemplo que mencionaste: `mongodb+srv://mafernandeza:GZJ8GFDVylCuCsG0@hacom.uw7vwmx.mongodb.net/bd-pedidos?appName=hacom`).
- Verifica que el cluster Atlas permita conexiones desde tu IP y que las credenciales sean correctas.
- En los logs verás métricas relacionadas con el driver de Mongo; si las métricas muestran 0 conexiones, es probable que la aplicación no esté conectando satisfactoriamente.

SMPP (envío de SMS)
-------------------
- El proyecto integra (o debe integrar) `cloudhopper-smpp` como cliente SMPP.
- El actor que procesa el pedido debe abrir/obtener una sesión SMPP ligada (bound). Si aparece el error "Cannot send SMS. SMPP session is not bound.", significa que la sesión SMPP no se ha enlazado o no se inició correctamente.
- El usuario y contrasela no son los correctos (son mocks).

Prometheus y métricas
---------------------
- Spring Boot Actuator expone métricas en: `http://localhost:9090/actuator/prometheus`
- Para ver las métricas desde la terminal:
  curl -s http://localhost:9090/actuator/prometheus | less
- Ejemplo de métrica personalizada incluida en el proyecto: `grpc_orders_received_total{type="insert_order"}` — un contador que incrementa cada vez que se recibe un InsertOrder vía gRPC.
- Para comprobar el contador desde la terminal:
  curl -s http://localhost:9090/actuator/prometheus | grep grpc_orders_received_total -n || true

Endpoints y comandos de ejemplo
------------------------------
1) InsertOrder (gRPC)
- Comando de ejemplo usando grpcurl (asumiendo servidor gRPC en localhost:6565 y sin TLS):

  grpcurl -plaintext \
    -d '{"customerId":"minim eu sint","customerPhoneNumber":"ipsum consectetur","items":["in do elit ut laboris","officia aliqua","commodo est","do officia eiusmod laborum irure","ut"],"orderId":"ea voluptate dolore quis"}' \
    'localhost:6565' \
    hacom.grpc.OrderService.InsertOrder

- Qué ocurre internamente: el servicio gRPC recibe la petición, dispara el actor de Akka que procesa y persiste el pedido. Al terminar, el actor envía un SMS y devuelve la respuesta gRPC.

2) Consultar estado de un pedido (REST)
- Endpoint sugerido: GET /api/orders/{orderId}
- Ejemplo curl:

  curl -s -X GET "http://localhost:9090/api/orders/ea%20voluptate%20dolore%20quis" \
    -H "Accept: application/json"

- Respuesta esperada: JSON con información del pedido, entre ellos un campo que indique el estado (por ejemplo: `status: "PROCESSED"`).

3) getOrdersCountByDateRange (REST) — devuelve {"totalOrdenes": N}
- Endpoint sugerido: GET /api/orders/count?from=<ISO_OFFSET_DATE_TIME>&to=<ISO_OFFSET_DATE_TIME>
- Usa `OffsetDateTime` en los parámetros `from` y `to`. Formato de ejemplo: `2026-04-20T00:00:00+00:00` o con zona local `2026-04-20T00:00:00-05:00`.

- Ejemplo curl (rangos):

  curl -s -X GET "http://localhost:9090/api/orders/count?from=2026-04-01T00:00:00%2B00:00&to=2026-04-30T23:59:59%2B00:00" \
    -H "Accept: application/json"

- Respuesta esperada (ejemplo):

  {"totalOrdenes": 42}

Notas de implementación y comprobación
--------------------------------------
- Si el contador Prometheus `grpc_orders_received_total` no aparece, revisa que el bean que lo incrementa se esté ejecutando en el handler gRPC.
- Para validar que Mongo guarda correctamente los pedidos, busca en los logs entradas de la operación `save` o usa la consola de MongoDB/Atlas para inspeccionar la colección de pedidos.
- Si el SMS no se envía: revisa la inicialización/binding de la sesión SMPP. El mensaje de error "SMPP session is not bound" indica que la sesión no está en estado BOUND_TX o BOUND_TRX.

Archivos de configuración relevantes
----------------------------------
- `src/main/resources/application.yaml` — configuración de conexión a MongoDB, puerto HTTP, Actuator, etc.
- `src/main/resources/log4j2.yml` — configuración de Log4j2 (preferir `log4j2.yml` en lugar de `log4j2.xml`).
- Clase gRPC service: `com.hacom.app_hacom.grpc.OrderGrpcServiceImpl` (recibe peticiones gRPC y delega al actor)
- Actor/Actors: paquete `com.hacom.app_hacom.actors` o similar (contiene la lógica de procesamiento y envío de SMS).
- Repositorio: `OrderRepository` (Spring Data MongoRepository).

Cómo depurar pasos comunes
-------------------------
- Si el contexto de Spring falla porque no existe un bean `ActorSystem`: revisa que la configuración que crea el `ActorSystem` esté activa y marcada como `@Bean`. Debes tener algo similar a:

  @Configuration
  public class AkkaConfig {
      @Bean
      public ActorSystem actorSystem() {
          return ActorSystem.create("app-system");
      }
  }

- Si Mongo no persiste: activar logging en el driver Mongo o revisar `spring.data.mongodb.uri`.
- Si SMPP falla en el bind: habilitar logs del cliente SMPP o probar con el proveedor SMPP en modo sandbox.

Pasos rápidos para probar localmente
-----------------------------------
1. Iniciar la aplicación (puerto HTTP: 9090 y gRPC en 6565 según configuración del proyecto).
2. Probar gRPC (InsertOrder) con el comando `grpcurl` anterior.
3. Consultar métricas Prometheus:
   curl http://localhost:9090/actuator/prometheus
   y buscar `grpc_orders_received_total`.
4. Comprobar guardado en MongoDB (logs o Atlas UI).
5. Probar endpoints REST:
   - GET /api/orders/{orderId}
   - GET /api/orders/count?from=...&to=...

Si quieres, puedo:
- Generar ejemplos curl/grpcurl con tus valores reales.
- Abrir/editar los controladores para asegurar los endpoints REST pedidos (si no existen ya).
- Revisar el código del actor para asegurar que responda vía gRPC y que haga el bind SMPP.

Fin del documento.
