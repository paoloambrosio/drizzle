package net.paoloambrosio.drizzle.gatling.core.expression

import net.paoloambrosio.drizzle.core.ScenarioContext
import net.paoloambrosio.drizzle.core.expression.Expression

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

object ExpressionCompiler {

  val valueMatcher = """\$\{([^\}]+)\}""".r

  /**
    * TODO This is just horrible. Consider the Scala Standard Parser
    * Combinator Library when something better is needed.
    *
    * @param expression expression string to be evaluated
    * @tparam T
    * @return compiled expression
    * @throws ExpressionCompilerException when static checks fail
    */
  def compile[T : ClassTag](expression: String): Expression[T] = {
    extractParts(expression) match {
      case StaticPart(text) :: Nil =>
        staticString(text)
      case DynamicPart(varName) :: Nil =>
        extractVariable(varName)
      case parts =>
        compose(parts.map(stringExpression))
    }
  }

  sealed trait Part
  case class StaticPart(static: String) extends Part
  case class DynamicPart(dynamic: String) extends Part

  def stringExpression(part: Part): Expression[String] = part match {
    case StaticPart(text) =>
      _ => Success(text)
    case DynamicPart(varName) =>
      extractVariable[Any](varName).andThen(_.map(_.toString))
  }


  def extractParts(expression: String): Seq[Part] = {
    valueMatcher.findAllMatchIn(expression).toList match {
      case Nil => Seq(StaticPart(expression))
      case m :: Nil => {
        // TODO for multiple dynamic parts
        val prefix = m.before.toString
        val prefixPart = if (prefix.isEmpty)
          Nil
        else
          Seq(StaticPart(prefix))

        val suffix = m.after.toString
        val suffixPart = if (suffix.isEmpty)
          Nil
        else
          Seq(StaticPart(suffix))

        prefixPart ++ Seq(DynamicPart(m.group(1))) ++ suffixPart
      }
      case _ => throw new ExpressionCompilerException("not implemented")
    }
  }

  def staticString[T : ClassTag](value: String): Expression[T] = {
    val ct = implicitly[ClassTag[T]]
    if (classOf[String].isAssignableFrom(ct.runtimeClass)) {
      _ => Success(value.asInstanceOf[T])
    } else {
      throw new ExpressionCompilerException("operation not supported with this type")
    }
  }

  def extractVariable[T : ClassTag](varName: String): Expression[T] = sc => {
    sc.sessionVariables.get(varName) match {
      case Some(v) => convert[T](v)
      case None => failure.variableNotfound(varName)
    }
  }

  def compose[T : ClassTag](expressions: Seq[Expression[String]]): Expression[T] = {
    expressions.reduce((e1, e2) => sc => for {
      o1 <- e1(sc)
      o2 <- e2(sc)
    } yield o1 + o2).asInstanceOf[Expression[T]]
  }

  private def convert[T : ClassTag](v: Any): Try[T] = {
    val ct = implicitly[ClassTag[T]]
    v match {
      case ct(x) => Success(x)
      case _ => failure.invalidCast
    }
  }

  private object failure {
    def variableNotfound(variable: String) = Failure(new ExpressionRuntimeException(s"'${variable}' variable not found"))
    def invalidCast = Failure(new ExpressionRuntimeException("cannot cast type"))
  }

}

class ExpressionCompilerException(msg: String) extends Exception(msg)
class ExpressionRuntimeException(msg: String) extends Exception(msg)
