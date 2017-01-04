package net.paoloambrosio.drizzle.gatling.core.expression

import net.paoloambrosio.drizzle.core.expression.Expression

import scala.annotation.tailrec
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

object ExpressionCompiler {

  val valueMatcher = """\$\{([^\}]+)\}""".r

  /**
    * Compiles a basic version of Gatling's Expression Language with variables
    * expansion only.
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


  def extractParts(expression: CharSequence): Seq[Part] = {
    valueMatcher.findFirstMatchIn(expression) match {
      case None =>
        if (expression.length() > 0) Seq(StaticPart(expression.toString))
        else Seq.empty
      case Some(m) => {
        val static = if (m.before.length() == 0) Seq.empty
                     else Seq(StaticPart(m.before.toString))
        val dynamic = Seq(DynamicPart(m.group(1)))
        val remaining = m.after
        static ++ dynamic ++ extractParts(remaining)
      }
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
    expressions.reduceOption((e1, e2) => sc => for {
      o1 <- e1(sc)
      o2 <- e2(sc)
    } yield o1 + o2).getOrElse(staticString("")).asInstanceOf[Expression[T]]
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
