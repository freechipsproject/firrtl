// See LICENSE for license details.

package firrtl.stage.phases

import firrtl.options.{DependencyManagerException, OptionsException, Phase, PhaseException}
import firrtl.{
  AnnotationSeq, CustomTransformException, FIRRTLException,
  FirrtlInternalException, FirrtlUserException, Utils
}

import scala.util.control.ControlThrowable

class CatchExceptions(val underlying: Phase) extends Phase {

  override final val prerequisites = underlying.prerequisites
  override final val optionalDependents = underlying.optionalDependents
  override final def invalidates(a: Phase): Boolean = underlying.invalidates(a)
  override final lazy val name = underlying.name

  override def transform(a: AnnotationSeq): AnnotationSeq = try {
    underlying.transform(a)
  } catch {
    /* Rethrow the exceptions which are expected or due to the runtime environment (out of memory, stack overflow, etc.).
     * Any UNEXPECTED exceptions should be treated as internal errors. */
    case p @ (_: ControlThrowable | _: FIRRTLException | _: OptionsException | _: FirrtlUserException
                | _: FirrtlInternalException | _: PhaseException | _: DependencyManagerException) => throw p
    case CustomTransformException(cause) => throw cause
    case e: Exception => Utils.throwInternalError(exception = Some(e))
  }

}


object CatchExceptions {

  def apply(p: Phase): CatchExceptions = new CatchExceptions(p)

}
