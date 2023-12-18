import io.estatico.newtype.macros.newtype
import doobie.Read

package object domain {
  @newtype
  case class Activity(value: String)

  object Activity {
    implicit val read: Read[Activity] = Read[String].map(Activity.apply)
  }
  @newtype
  case class ActivityID(value: Long)

  object ActivityID {
    implicit val read: Read[ActivityID] = Read[Long].map(ActivityID.apply)
  }
}
