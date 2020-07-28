sealed trait Loc
sealed trait Behavior {
  def behave: Behavior
}
case class Moving(loc: Loc) extends Behavior
case class Idling extends Behavior