//import service.BoredCommandStorage
//import dbms.BoredCommandSql
import cats.effect.{ExitCode, IO, IOApp, Resource}
import sttp.client4.Response
import sttp.client4.quick._
import sttp.model.Uri
import domain._
import domain.errors._
import doobie._
import doobie.implicits._
import config.AppConfig

class BoredCommand extends IOApp {

  private val json = ujson.Obj(
    "activity" -> "Some activity",
    "accessibility" -> 0.5,
    "`type`" -> "Some type",
    "participants" -> 2,
    "price" -> 1.0,
    "link" -> Some("https://example.com"),
    "key" -> "Some key"
  )

  def run(args: List[String]): IO[ExitCode] = {
    /*(for {
      config <- Resource.eval(AppConfig.load)
      transactor = Transactor.fromDriverManager[IO](
        config.db.url,
        config.db.driver,
        config.db.user,
        config.db.password
      )
      sql = BoredCommandSql.make
      storage = BoredCommandStorage.make(sql, transactor)
      _ <- Resource.eval(
        if (args.contains("--help")) help()
        else makeRequest(storage)
      )
    } yield ()).useForever.as(ExitCode.Success)*/
   program.use(_ => makeRequest()).as(ExitCode.Success)
  }

  private def program: Resource[IO, Unit] =
    Resource.make(IO.unit)(_ => IO.unit)

  private def userInput(): IO[Uri] =
    for {
      key <- readInput("Enter a unique numeric id of activity (if you have one): ")
      result <-
        if (key.nonEmpty) IO.pure(constructRequest(key, "", "", "", "", "", ""))
        else
          for {
            activityType <- readInput("Enter activity type (optional): ")
            participants <- readInput("Enter number of participants (optional): ")
            minPrice <- readInput("Enter minimum price (optional): ")
            maxPrice <- readInput("Enter maximum price (optional): ")
            minAccessibility <- readInput("Enter minimum accessibility (optional): ")
            maxAccessibility <- readInput("Enter maximum accessibility (optional): ")
          } yield constructRequest(
            key,
            activityType,
            participants,
            minPrice,
            maxPrice,
            minAccessibility,
            maxAccessibility
          )
    } yield result

  private def makeRequest(/*boredSql: BoredCommandStorage*/): IO[Unit] = {
    val constructedUrlIO: IO[Uri] = userInput()

    val response: IO[Response[Either[String, String]]] =
      constructedUrlIO.flatMap(constructedUrl =>
        IO {
          basicRequest
            .get(constructedUrl)
            .body(ujson.write(json))
            .send()
        }
      )

    response.flatMap(res =>
      IO {
        val jsonResult = res.body
        jsonResult.fold(
          error => println(s"Failed to parse JSON: $error"),
          jsonString => {
            val parsedJson = ujson.read(jsonString)
            if (parsedJson.obj.contains("error")) {
              println(parsedJson("error").str)
              println("See --help")
            } else {
              println(s"Activity special for you: ${parsedJson("activity").str}")
              println("Do you like this activity? [y/n]")
              val answer = scala.io.StdIn.readLine().trim
              if (answer.equalsIgnoreCase("y")) {
                val activity = parsedJson("activity").str
                val activityID = parsedJson("key").str.toLong
                //boredSql.add(Activity(activity), ActivityID(activityID))
                println("Activity saved in favorites")
              } else {
                println("Ok, let's try again")
              }
            }
          }
        )
      }
    )
  }

  private def constructRequest(
    key: String,
    activityType: String,
    participants: String,
    minPrice: String,
    maxPrice: String,
    minAccessibility: String,
    maxAccessibility: String
  ): Uri = {
    val baseUri = uri"http://www.boredapi.com/api/activity"
    val queryParams = List(
      "key" -> key,
      "type" -> activityType,
      "participants" -> participants,
      "minprice" -> minPrice,
      "maxprice" -> maxPrice,
      "minaccessibility" -> minAccessibility,
      "maxaccessibility" -> maxAccessibility
    ).collect {
      case (k, v) if v.nonEmpty => k -> v
    }
    baseUri.params(queryParams: _*)
  }

  private def readInput(prompt: String): IO[String] = {
    IO.pure(println(prompt)) *>
      IO.pure(scala.io.StdIn.readLine().trim)
  }

  def help(): IO[Unit] = {
    IO.pure(println("Instructions for data entry:")) *>
      IO.pure(println("- Unique numeric id - number from 1000000 to 9999999")) *>
      IO.pure(
        println(
          "- Activity type - type of activity (supported activity types: education, recreational, social, diy, charity, cooking, relaxation, music, busywork)"
        )
      ) *>
      IO.pure(println("- Number of participants - number from 1 to 10")) *>
      IO.pure(
        println(
          "- Minimum/maximum price - number from 0 to 1 (factor describing the cost of the event with zero being free)"
        )
      ) *>
      IO.pure(
        println(
          "- Minimum/maximum accessibility - number from 0 to 1 (factor describing how possible an event is to do with zero being the most accessible)"
        )
      )
  }
}

object BoredCommandApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    if (args.contains("--help")) {
      new BoredCommand().help().as(ExitCode.Success)
    } else {
      new BoredCommand().run(args)
    }
  }
}
