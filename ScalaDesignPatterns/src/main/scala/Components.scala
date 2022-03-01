import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// First the interfaces
trait Time {
  def getTime(): String
}

trait RecipeFinder {
  def findRecipe(dish: String): String
}

trait Cooker {
  def cook(what: String): Food
}

case class Food(name: String)

// Now the components

trait TimeComponent {
  val time: Time

  class TimeImpl extends Time {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    override def getTime(): String = LocalDateTime.now().format(formatter)
  }
}

trait RecipeComponent {

  val recipe: RecipeFinder

  class RecipeFinderImpl extends RecipeFinder {
    override def findRecipe(dish: String): String = {
      dish match {
        case "chips"    => "Fry potatoes for 10 mins."
        case "fish"     => "Clean the fish and put in oven for 30 mins."
        case "sandwich" => "Put butter, ham, cheese and tomatoes."
        case _          => throw new RuntimeException(s"$dish is unkown.")
      }
    }
  }
}

trait CookingComponent {
  this: RecipeComponent =>

  val cooker: Cooker

  class CookerImpl extends Cooker {
    override def cook(what: String): Food = {
      val recipeText = recipe.findRecipe(what)
      Food(what)
    }
  }
}

class RobotRegistry extends TimeComponent with RecipeComponent with CookingComponent {
  override val time: Time = new TimeImpl
  override val recipe: RecipeFinder = new RecipeFinderImpl
  override val cooker: Cooker = new CookerImpl
}

class Robot extends RobotRegistry {
  def cook(what: String) = cooker.cook(what)
  def getTime() = time.getTime()
}

object ComponentApp extends App {
  val robot = new Robot
  println(robot.getTime())
  println(robot.cook("chips"))
  println(robot.cook("sandwich"))
}
