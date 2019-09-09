package cc.cheatex.ktracer

import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import kotlin.math.abs

const val E = 1e-6

fun equal(v: VectorD) = VectorDMatcher(v, E)

class VectorDMatcher(val value: VectorD, val error: Double)
  : TypeSafeMatcher<VectorD>(VectorD::class.java) {

  override fun describeTo(description: Description) {
    with(description) {
      appendText("a vector with elements within ")
      appendValue(error)
      appendText(" of ")
      appendValue(value)
    }
  }

  override fun describeMismatchSafely(item: VectorD?, description: Description) {
    with(description) {
      appendValue(item)
      appendText(" differed more than ")
      appendValue(error)
    }
  }

  override fun matchesSafely(item: VectorD?): Boolean {
    if (item == null) {
      return false
    } else {
      for (i in value.array.indices) {
        if (delta(value.array[i], item.array[i]) > error) {
          return false
        }
      }
      return true
    }
  }

  fun delta(expected: Double, actual: Double): Double =
      abs(expected - actual)
}
