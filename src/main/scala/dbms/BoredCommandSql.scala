package dbms



/*import cats.implicits.toFunctorOps
import domain._
import domain.errors._
import cats.Applicative
import doobie._
import doobie.implicits._

trait BoredCommandSql {
  def listAll: ConnectionIO[List[Activity]]
  def findById(id: ActivityID): ConnectionIO[Option[Activity]]
  def removeById(id: ActivityID): ConnectionIO[Either[ActivityNotFound, Unit]]
  def add(activity: Activity, id: ActivityID): ConnectionIO[Either[ActivityAlreadyExists, Activity]]
}

object BoredCommandSql {

  object sqls {
    val listAllSql: Query0[Activity] =
      sql"""
           select *
           from ACTIVITIES
      """.query[Activity]

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
  }

  private final class Impl extends BoredCommandSql {

    import sqls._

    override def listAll: ConnectionIO[List[Activity]] = listAllSql.to[List]

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
  }

  def make: BoredCommandSql = new Impl
}
*/