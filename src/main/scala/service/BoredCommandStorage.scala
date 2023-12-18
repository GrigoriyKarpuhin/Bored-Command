package service

import cats.syntax.either._
import cats.effect.IO
import dbms.BoredCommandSql
import domain._
import domain.errors._
import doobie._
import doobie.implicits._

trait BoredCommandStorage {
  def list: IO[Either[AppError, List[(Activity, ActivityID)]]]
  def removeById(id: ActivityID): IO[Either[AppError, Unit]]
  def add(activity: Activity, id: ActivityID): IO[Either[AppError, Activity]]
  def create: ConnectionIO[Unit]
}

object BoredCommandStorage {

  private final class Impl(boredSql: BoredCommandSql, transactor: Transactor[IO]) extends BoredCommandStorage {

    override def list: IO[Either[
      AppError,
      List[(Activity, ActivityID)]
    ]] =
      boredSql.listAll
        .transact(transactor)
        .attempt
        .map {
            case Left(th)           => InternalError(th).asLeft
            case Right(Left(error)) => error.asLeft
            case Right(Right(list)) => list.asRight
        }
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

    override def create: ConnectionIO[Unit] = boredSql.create
  }

  def make(boredSql: BoredCommandSql, transactor: Transactor[IO]): BoredCommandStorage =
    new Impl(boredSql, transactor)
}
