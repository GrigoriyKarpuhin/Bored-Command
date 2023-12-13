package config

import cats.effect.IO
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.semiauto._

final case class AppConfig(db: DbConfig)

object AppConfig {
  implicit val reader: ConfigReader[AppConfig] = deriveReader

  def load: IO[AppConfig] =
    IO.delay(ConfigSource.default.loadOrThrow[AppConfig])
}

final case class DbConfig(
  url: String,
  driver: String,
  user: String,
  password: String
)
object DbConfig {
  implicit val reader: ConfigReader[DbConfig] = deriveReader
}
