package cc.cheatex.ktracer

import kotlin.math.sqrt

inline class VectorD(val array: DoubleArray) {
    constructor(x: Double, y: Double, z: Double) : this(doubleArrayOf(x, y, z))

    companion object {
        val zero = VectorD(0.0, 0.0, 0.0)
    }

    operator fun plus(other: VectorD): VectorD {
        assert(this.array.size == other.array.size)

        val r = DoubleArray(this.array.size)
        for (i in this.array.indices) {
            r[i] = this.array[i] + other.array[i]
        }

        return VectorD(r)
    }

    operator fun minus(other: VectorD): VectorD {
        assert(this.array.size == other.array.size)

        val r = DoubleArray(this.array.size)
        for (i in this.array.indices) {
            r[i] = this.array[i] - other.array[i]
        }

        return VectorD(r)
    }

    operator fun times(other: VectorD): VectorD {
        assert(this.array.size == other.array.size)

        val r = DoubleArray(this.array.size)
        for (i in this.array.indices) {
            r[i] = this.array[i] * other.array[i]
        }

        return VectorD(r)
    }

    operator fun div(d: Double): VectorD {
        val r = DoubleArray(this.array.size)
        for (i in this.array.indices) {
            r[i] = this.array[i] / d
        }

        return VectorD(r)
    }

    fun dot(other: VectorD): Double {
        assert(this.array.size == other.array.size)

        var r = 0.0
        for (i in this.array.indices) {
            r += this.array[i] * other.array[i]
        }

        return r
    }

    val sumsq: Double
        get() {
            var r = 0.0
            for (i in this.array.indices) {
                r += this.array[i] * this.array[i]
            }

            return r
        }

    val length: Double
        get() = sqrt(this.sumsq)

    val norm: VectorD
        get() = this / this.length
}

inline class ColorD(val array: DoubleArray) {
    constructor(r: Double, g: Double, b: Double) : this(doubleArrayOf(r, g, b))

    companion object {
        val black = ColorD(0.0, 0.0, 0.0)
        val white = ColorD(1.0, 1.0, 1.0)
    }
}
