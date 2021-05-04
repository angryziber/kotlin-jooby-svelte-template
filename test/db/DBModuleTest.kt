package db

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DBModuleTest {
  @Test
  fun herokuDbUrlToJdbc() {
    val herokuUrl = "postgres://uusseerr:pwd@ec2-54-247-79-178.eu-west-1.compute.amazonaws.com:5432/de7ujn7doqqonv"
    assertThat(DBModule.herokuDbUrlToJdbc(herokuUrl)).isEqualTo("jdbc:postgresql://ec2-54-247-79-178.eu-west-1.compute.amazonaws.com:5432/de7ujn7doqqonv?sslmode=require&user=uusseerr&password=pwd")
  }
}