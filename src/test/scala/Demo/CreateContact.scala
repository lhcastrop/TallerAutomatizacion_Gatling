package Demo

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import Demo.Data._

class CreateContact extends Simulation {

  val httpConf = http
    .baseUrl(url)
    .acceptHeader("application/json")

  val createContactScn = scenario("Crear Contacto")
    .exec(
      http("Login para obtener token")
        .post("users/login")
        .body(StringBody(s"""{"email": "$email", "password": "$password"}""")).asJson
        .check(status.is(200))
        .check(jsonPath("$.token").saveAs("authToken"))
    )
    .exec(
      http("Crear nuevo contacto")
        .post("contacts")
        .header("Authorization", "Bearer ${authToken}")
        .body(StringBody(
          s"""{
            "firstName": "Nuevo",
            "lastName": "Contacto",
            "birthdate": "1990-01-01",
            "email": "nuevo${System.currentTimeMillis()}@mail.com",
            "phone": "1234567890"
          }"""
        )).asJson
        .check(status.is(201))
        .check(jsonPath("$.id").saveAs("contactId"))
    )
    .exec(
      http("Obtener lista de contactos")
        .get("contacts")
        .header("Authorization", "Bearer ${authToken}")
        .check(status.is(200))
        .check(jsonPath("$[?(@.id=='${contactId}')]").exists)
    )

  setUp(
    createContactScn.inject(rampUsers(5).during(10))
  ).protocols(httpConf)
}
