package Demo

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import Demo.Data._

class CreateContact extends Simulation {

  val httpConf = http
    .baseUrl(url)
    .acceptHeader("application/json")
 
  val crearContacto = scenario("Crear Contacto")
    .exec(
      http("Login para obtener token")
        .post(s"users/login")
        .body(StringBody(s"""{"email": "$email", "password": "$password"}""")).asJson
        .check(status.is(200))
        .check(jsonPath("$.token").saveAs("authToken"))
    ).exitHereIfFailed
    .repeat(1) {
    .exec(
      http("Crear nuevo contacto")
        .post(s"contacts")
        .header("Authorization", "Bearer ${authToken}")
        .body(StringBody(
          s"""{
            "firstName": "Nuevo",
            "lastName": "Contacto",
            "birthdate": "1990-01-01",
            "email": "nuevo${System.currentTimeMillis()}@hotmail.com",
            "phone": "1234567890",
            "street1": "1 Main St.",
            "street2": "Apartment A",
            "city": "Anytown",
            "stateProvince": "KS",
            "postalCode": "12345",
            "country": "USA"
          }"""
        )).asJson
        .check(status.is(201))
    )
    .exec(
      http("Obtener lista de contactos")
        .get("contacts")
        .header("Authorization", "Bearer ${authToken}")
        .check(status.is(200))
    )}
  setUp(
    crearContacto.inject(atOnceUsers(100))
    ).protocols(httpConf)
} 
