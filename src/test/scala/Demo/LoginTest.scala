package Demo

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import Demo.Data._

class LoginTest extends Simulation{

  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")

  val Login = scenario("Login")
    .exec(http("login")
      .post(s"users/login")
      .body(StringBody(s"""{"email": "$email", "password": "$password"}""")).asJson
      .check(status.is(200))
      .check(jsonPath("$.token").saveAs("authToken"))
    ).exitHereIfFailed
    .exec(http("Obtener contactos con token")
      .get("contacts")
      .header("Authorization", "Bearer ${authToken}")
      .check(status.is(200))
    )

  val credencialesLoginInvalidas = scenario("Login con credenciales inválidas")
    .exec(http("Login inválido")
      .post("users/login")
      .body(StringBody("""{"email": "usuario@falso.com", "password": "passwordIncorrecto"}""")).asJson
      .check(status.is(401))
    )

  val formatoEmailIncorrecto = scenario("Login con email malformado")
    .exec(http("Login con email inválido")
      .post("users/login")
      .body(StringBody("""{"email": "emailInvalido", "password": "12345678"}""")).asJson
      .check(status.in(401))
    )  

  setUp(
    //Login.inject(atOnceUsers(50)),
    Login.inject(atOnceUsers(rampUsers(10).during(100))),
    credencialesLoginInvalidas.inject(rampUsers(10).during(10)),
    formatoEmailIncorrecto.inject(rampUsers(10).during(10))
  ).protocols(httpConf)
}
