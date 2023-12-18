package domain

import cats.implicits.catsSyntaxOptionId

object errors {
  sealed abstract class AppError(
    val message: String,
    val cause: Option[Throwable] = None
  )

  case class ActivityAlreadyExists() extends AppError("Activity with same name and date already in favorites")

  case class ActivityNotFound(id: ActivityID) extends AppError("No activity with this id was found")

  case class NoActivities() extends AppError("No activities in favorites")

  case class InternalError(
    cause0: Throwable
  ) extends AppError("Internal error", cause0.some)
}
