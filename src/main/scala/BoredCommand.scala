import service.BoredCommandStorage
import dbms.BoredCommandSql
import cats.effect.{ExitCode, IO, IOApp, Resource}
import sttp.client4.Response
import sttp.client4.quick._
import sttp.model.Uri
import domain._
import doobie._
import cats.effect.unsafe.implicits.global

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

  private val transactor = Transactor.fromDriverManager[IO](
    "org.sqlite.JDBC",
    "jdbc:sqlite:bored.db",
    "",
    ""
  )

  private def insertActivity(activity: Activity, activityID: ActivityID, boredSql: BoredCommandStorage): IO[Unit] = {
    transactor.trans.apply(boredSql.create).unsafeRunSync()
    boredSql.add(activity, activityID).unsafeRunSync() match {
      case Left(error) =>
        IO.pure(println(error.message))
      case Right(_) =>
        IO.pure(println("Activity saved in favorites"))
    }
  }

  private def listFavorites(boredSql: BoredCommandStorage): IO[Unit] = {
    transactor.trans.apply(boredSql.create).unsafeRunSync()
    boredSql.list.unsafeRunSync() match {
      case Left(error) =>
        IO.pure(println(error.message))
      case Right(activities) =>
        val formattedList = activities.zipWithIndex.map {
          case (activity, index) => s"${index + 1}. ${activity._1.value} (ID: ${activity._2.value})"
        }
        IO.pure(println(formattedList.mkString("\n")))
    }
  }

  private def removeActivity(boredSql: BoredCommandStorage, activityID: ActivityID): IO[Unit] = {
    transactor.trans.apply(boredSql.create).unsafeRunSync()
    boredSql.removeById(activityID).unsafeRunSync() match {
      case Left(error) =>
        IO.pure(println(error.message))
      case Right(_) =>
        IO.pure(println("Activity removed from favorites"))
    }
  }

  def run(args: List[String]): IO[ExitCode] = {
    Resource
      .eval(IO(println("Welcome to Bored Command!")))
      .use { _ =>
        for {
          sql <- IO(BoredCommandSql.make)
          storage = BoredCommandStorage.make(sql, transactor)
          _ <- askUser(storage)
        } yield ()
      }
      .as(ExitCode.Success)
  }
  def askUser(boredSql: BoredCommandStorage): IO[Unit] = {
    for {
      _ <- IO(println("What would you like to do?"))
      _ <- IO(println("1. Generate new activity"))
      _ <- IO(println("2. List your favorites"))
      _ <- IO(println("3. Remove from favorites by ID"))
      _ <- IO(println("4. Help"))
      userInput <- readInput("Enter the number corresponding to your choice: ")

      _ <- userInput match {
        case "1" => makeRequest(boredSql)
        case "2" => listFavorites(boredSql)
        case "3" => removeFromFavorites(boredSql)
        case "4" => help()
        case _   => IO(println("Invalid choice. Please enter a valid number."))
      }
    } yield ()
  }

  private def removeFromFavorites(boredSql: BoredCommandStorage): IO[Unit] = {
    for {
      activityIdStr <- readInput("Enter the ID of the activity to remove from favorites: ")
      activityId <- IO(activityIdStr)
      check: Long = activityId.toLongOption.getOrElse(0)
      _ <- removeActivity(boredSql, ActivityID(check))
    } yield ()
  }

  def userInput(): IO[Uri] =
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

  private def makeRequest(boredSql: BoredCommandStorage): IO[Unit] = {
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
              println("Try to read Help")
            } else {
              println(s"Activity special for you: ${parsedJson("activity").str}")
              println("Do you like this activity? [y/n]")
              val answer = scala.io.StdIn.readLine().trim
              if (answer.equalsIgnoreCase("y")) {
                val activity = parsedJson("activity").str
                val activityID = parsedJson("key").str.toLong
                insertActivity(Activity(activity), ActivityID(activityID), boredSql)
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
    val baseUri = uri"https://www.boredapi.com/api/activity"
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

  private def help(): IO[Unit] = {
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
    IO.pure(println("\nDisclaimer:")) *>
      IO.pure(
        println(
          "Even if the parameters are entered correctly in form, this does not guarantee the existence of the corresponding activity"
        )
      )
  }
}

object BoredCommandApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = new BoredCommand().run(args)
}
