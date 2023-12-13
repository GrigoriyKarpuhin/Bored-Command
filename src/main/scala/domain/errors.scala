package domain

import cats.implicits.catsSyntaxOptionId

object errors {
  sealed abstract class AppError(
    val message: String,
    val cause: Option[Throwable] = None
  )

  case class ActivityAlreadyExists() extends AppError("Activity with same name and date already exists")

  //case class ActivityNotFound(id: ActivityID) extends AppError(s"Activity with id ${id.value} not found")

  case class InternalError(
    cause0: Throwable
  ) extends AppError("Internal error", cause0.some)
}
