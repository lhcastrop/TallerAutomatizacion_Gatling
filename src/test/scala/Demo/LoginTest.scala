package Demo

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import Demo.Data._

class LoginTest extends Simulation{

  // 1 Http Conf
  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")
    //Verificar de forma general para todas las solicitudes

  // 2 Scenario Definition
  val scn = scenario("Login").
    exec(http("login")
      .post(s"users/login")
      .body(StringBody(s"""{"email": "$email", "password": "$password"}""")).asJson
         //Validar status 200 del servicio
      .check(status.is(200))
      .check(jsonPath("$.token").saveAs("authToken"))
    )
    .exec(
      http("Create Contact")
        .post(s"contacts")
        .header("Authorization", "Bearer ${authToken}")
        .body(StringBody(s"""{"firstName": "Leonardo","lastName": "Castro","birthdate": "1991-01-01","email": "pruebasleo@hotmail.com","phone": "8005555555","street1": "1 Main St.","street2": "Apartment A","city": "Anytown","stateProvince": "KS","postalCode": "12345","country": "USA"}""")).asJson
        .check(status.is(201))
    )
     .exec(
      http("Obtener contactos con token")
        .get("contacts")
        .header("Authorization", "Bearer ${authToken}")
        .check(status.is(200))
    )

  // 3. Escenario: Login con credenciales inválidas
  val invalidCredentialsLogin = scenario("Login con credenciales inválidas")
    .exec(
      http("Login inválido")
        .post("users/login")
        .body(StringBody(
          """{"email": "usuario@falso.com", "password": "passwordIncorrecto"}"""
        )).asJson
        .check(status.is(401)) // O 400 según cómo responde la API
        .check(jsonPath("$.error").is("Incorrect email or password"))
    )

  // 4. Escenario: Login con formato inválido de email
  val invalidEmailFormat = scenario("Login con email malformado")
    .exec(
      http("Login con email inválido")
        .post("users/login")
        .body(StringBody(
          """{"email": "emailInvalido", "password": "12345678"}"""
        )).asJson
        .check(status.in(400 to 422)) // Dependiendo del backend
    )

  // 5. Ejecución de escenarios
  setUp(
    scn.inject(rampUsers(10).during(10)),
    invalidCredentialsLogin.inject(rampUsers(2).during(5)),
    invalidEmailFormat.inject(rampUsers(2).during(5))
  ).protocols(httpConf)
}


