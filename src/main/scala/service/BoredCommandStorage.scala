package service



/*import cats.syntax.either._
import cats.effect.IO
import dbms.BoredCommandSql
import domain._
import domain.errors._
import doobie._
import doobie.implicits._

trait BoredCommandStorage {
  def list: IO[Either[InternalError, List[Activity]]]
  def findById(id: ActivityID): IO[Either[InternalError, Option[Activity]]]
  def removeById(id: ActivityID): IO[Either[AppError, Unit]]
  def add(activity: Activity, id: ActivityID): IO[Either[AppError, Activity]]
}

object BoredCommandStorage {

  private final class Impl(boredSql: BoredCommandSql, transactor: Transactor[IO]) extends BoredCommandStorage {
    override def list: IO[Either[InternalError, List[Activity]]] =
      boredSql.listAll
        .transact(transactor)
        .attempt
        .map(_.leftMap(InternalError.apply))

    override def findById(id: ActivityID): IO[
      Either[InternalError, Option[Activity]]
    ] = boredSql
      .findById(id)
      .transact(transactor)
      .attempt
      .map(_.leftMap(InternalError.apply))

    override def removeById(
      id: ActivityID
    ): IO[Either[AppError, Unit]] = boredSql
      .removeById(id)
      .transact(transactor)
      .attempt
      .map {
        case Left(th)           => InternalError(th).asLeft
        case Right(Left(error)) => error.asLeft
        case _                  => ().asRight
      }
    override def add(
      activity: Activity,
      id: ActivityID
    ): IO[Either[AppError, Activity]] = boredSql
      .add(activity, id)
      .transact(transactor)
      .attempt
      .map {
        case Left(th)           => InternalError(th).asLeft
        case Right(Left(error)) => error.asLeft
        case Right(Right(todo)) => todo.asRight
      }
  }

  def make(boredSql: BoredCommandSql, transactor: Transactor[IO]): BoredCommandStorage =
    new Impl(boredSql, transactor)
}
*/