import cats.effect.unsafe.implicits.global
import cats.effect.{ExitCode, IO}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import service.BoredCommandStorage
import sttp.client4.{Response, UriContext}
import org.mockito.MockitoSugar
import sttp.model.StatusCode

class BoredCommandSpec extends AsyncWordSpec with Matchers with MockitoSugar {

  "BoredCommand" should {
    "handle user input and API response" in {
      val responseMock = mock[Response[Either[String, String]]]

      when(responseMock.body).thenReturn(Right("""{"activity":"Test Activity","key":"123"}"""))
      when(responseMock.code).thenReturn(StatusCode(200))
      when(responseMock.isSuccess).thenReturn(true)
      when(responseMock.isClientError).thenReturn(false)
      when(responseMock.isServerError).thenReturn(false)

      val boredCommand = new BoredCommand {
        override def userInput(): IO[sttp.model.Uri] =
          IO.pure(uri"https://www.boredapi.com/api/activity?key=123")

        override def askUser(boredSql: BoredCommandStorage): IO[Unit] = {
          makeRequest(boredSql)
        }

        private def makeRequest(boredSql: BoredCommandStorage): IO[Unit] = {
          val responseIO: IO[Response[Either[String, String]]] = IO.pure(responseMock)
          responseIO.flatMap(res =>
            IO {
              val jsonResult = res.body
              jsonResult.fold(
                error => fail(s"Failed to parse JSON: $error"),
                jsonString => {
                  val parsedJson = ujson.read(jsonString)
                  if (parsedJson.obj.contains("error")) {
                    fail(parsedJson("error").str)
                  } else {
                    succeed
                  }
                }
              )
            }
          )
        }
      }

      boredCommand.run(List.empty[String]).unsafeToFuture().map { exitCode =>
        exitCode shouldBe ExitCode.Success
      }
    }

    "handle user input and API response with error" in {
      val responseMock = mock[Response[Either[String, String]]]

      when(responseMock.body).thenReturn(Right("""{"error":"Test Error"}"""))
      when(responseMock.code).thenReturn(StatusCode(200))
      when(responseMock.isSuccess).thenReturn(true)
      when(responseMock.isClientError).thenReturn(false)
      when(responseMock.isServerError).thenReturn(false)

      val boredCommand = new BoredCommand {
        override def userInput(): IO[sttp.model.Uri] =
          IO.pure(uri"https://www.boredapi.com/api/activity?key=123")

        override def askUser(boredSql: BoredCommandStorage): IO[Unit] = {
          makeRequest(boredSql)
        }

        private def makeRequest(boredSql: BoredCommandStorage): IO[Unit] = {
          val responseIO: IO[Response[Either[String, String]]] = IO.pure(responseMock)
          responseIO.flatMap(res =>
            IO {
              val jsonResult = res.body
              jsonResult.fold(
                error => fail(s"Failed to parse JSON: $error"),
                jsonString => {
                  val parsedJson = ujson.read(jsonString)
                  if (parsedJson.obj.contains("error")) {
                    println(parsedJson("error").str)
                    println("See --help")
                    succeed
                  } else {
                    fail
                  }
                }
              )
            }
          )
        }
      }
      boredCommand.run(List.empty[String]).unsafeToFuture().map { exitCode =>
        exitCode shouldBe ExitCode.Success
      }
    }

    "handle invalid user input" in {
      val responseMock = mock[Response[Either[String, String]]]

      when(responseMock.body).thenReturn(Right("""{"error":"Test Error"}"""))
      when(responseMock.code).thenReturn(StatusCode(200))
      when(responseMock.isSuccess).thenReturn(true)
      when(responseMock.isClientError).thenReturn(false)
      when(responseMock.isServerError).thenReturn(false)

      val boredCommand = new BoredCommand {
        override def userInput(): IO[sttp.model.Uri] =
          IO.raiseError(new RuntimeException("Simulating invalid user input"))

        override def askUser(boredSql: BoredCommandStorage): IO[Unit] = {
          makeRequest(boredSql)
        }

        private def makeRequest(boredSql: BoredCommandStorage): IO[Unit] = {
          val responseIO: IO[Response[Either[String, String]]] = IO.pure(responseMock)
          responseIO.flatMap(res =>
            IO {
              val jsonResult = res.body
              jsonResult.fold(
                error => fail(s"Failed to parse JSON: $error"),
                jsonString => {
                  val parsedJson = ujson.read(jsonString)
                  if (parsedJson.obj.contains("error")) {
                    println(parsedJson("error").str)
                    println("See --help")
                    succeed
                  } else {
                    fail("Expected an error in user input.")
                  }
                }
              )
            }
          )
        }
      }

      boredCommand.run(List.empty[String]).unsafeToFuture().map { exitCode =>
        exitCode shouldBe ExitCode.Success
      }
    }
  }
}
