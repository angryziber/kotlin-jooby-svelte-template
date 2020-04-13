package db

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DBModuleTest {
  val module = DBModule()

  @Test
  fun herokuDbUrlToJdbc() {
    val herokuUrl = "postgres://uusseerr:3782bac42234d800494192ecf@ec2-54-247-79-178.eu-west-1.compute.amazonaws.com:5432/de7ujn7doqqonv"
    assertThat(module.herokuDbUrlToJdbc(herokuUrl)).isEqualTo("jdbc:postgresql://ec2-54-247-79-178.eu-west-1.compute.amazonaws.com:5432/de7ujn7doqqonv?sslmode=require&user=uusseerr&password=3782bac42234d800494192ecf")
  }
}