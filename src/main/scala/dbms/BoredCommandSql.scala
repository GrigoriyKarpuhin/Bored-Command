package dbms

import cats.implicits.toFunctorOps
import domain._
import domain.errors._
import cats.Applicative
import doobie._
import doobie.implicits._

trait BoredCommandSql {
  def listAll: ConnectionIO[Either[NoActivities, List[(Activity, ActivityID)]]]
  def findById(id: ActivityID): ConnectionIO[Option[Activity]]
  def removeById(id: ActivityID): ConnectionIO[Either[ActivityNotFound, Unit]]
  def add(activity: Activity, id: ActivityID): ConnectionIO[Either[ActivityAlreadyExists, Activity]]
  def create: ConnectionIO[Unit]
}

object BoredCommandSql {

  private object sqls {
    val listAllSql: Query0[(Activity, ActivityID)] =
      sql"""
           select activity, id
           from ACTIVITIES
      """.query[(Activity, ActivityID)]

    def findByIdSql(id: ActivityID): Query0[Activity] =
      sql"""
           select *
           from ACTIVITIES
           where id=${id.value}
      """.query[Activity]

    def removeByIdSql(id: ActivityID): Update0 =
      sql"""
           delete from ACTIVITIES
           where id=${id.value}
         """.update

    def insertSql(activity: Activity, id: ActivityID): Update0 =
      sql"""
          insert into ACTIVITIES (activity, id)
          values (${activity.value}, ${id.value})
         """.update

    def createSql: Update0 =
      sql"""
          create table if not exists ACTIVITIES (
            activity text not null,
            id text not null unique
          )
         """.update
  }

  private final class Impl extends BoredCommandSql {

    import sqls._

    override def listAll: ConnectionIO[Either[NoActivities, List[(Activity, ActivityID)]]] =
      for {
        activities <- listAllSql.to[List]
        result <- activities match {
          case Nil => Applicative[ConnectionIO].pure(Left(NoActivities()))
          case _   => Applicative[ConnectionIO].pure(Right(activities))
        }
      } yield result

    override def findById(
      id: ActivityID
    ): ConnectionIO[Option[Activity]] = findByIdSql(id).option

    override def removeById(
      id: ActivityID
    ): ConnectionIO[Either[ActivityNotFound, Unit]] =
      for {
        activity <- findByIdSql(id).option
        result <- activity match {
          case Some(_) => removeByIdSql(id).run.as(Right(()))
          case None    => Applicative[ConnectionIO].pure(Left(ActivityNotFound(id)))
        }
      } yield result

    override def add(
      activity: Activity,
      id: ActivityID
    ): ConnectionIO[Either[ActivityAlreadyExists, Activity]] =
      for {
        existingActivity <- findByIdSql(id).option
        result <- existingActivity match {
          case Some(_) => Applicative[ConnectionIO].pure(Left(ActivityAlreadyExists()))
          case None    => insertSql(activity, id).run.as(Right(activity))
        }
      } yield result

    override def create: ConnectionIO[Unit] = createSql.run.void
  }

  def make: BoredCommandSql = new Impl
}