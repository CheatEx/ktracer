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

    operator fun times(d: Double): VectorD {
        val r = DoubleArray(this.array.size)
        for (i in this.array.indices) {
            r[i] = this.array[i] * d
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

    operator fun unaryMinus(): VectorD {
        val r = DoubleArray(this.array.size)
        for (i in this.array.indices) {
            r[i] = -this.array[i]
        }

        return VectorD(r)
    }

    infix fun dot(other: VectorD): Double {
        assert(this.array.size == other.array.size)

        var r = 0.0
        for (i in this.array.indices) {
            r += this.array[i] * other.array[i]
        }

        return r
    }

    infix fun cross(other: VectorD): VectorD =
        VectorD(this.y*other.z - this.z*other.y,
                this.z*other.x - this.x*other.z,
                this.x*other.y - this.y*other.x)

    inline val x: Double
        get() = array[0]
    inline val y: Double
        get() = array[1]
    inline val z: Double
        get() = array[2]

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

    val unit: VectorD
        get() = this / this.length
}

inline class ColorD(val array: DoubleArray) {
    constructor(r: Double, g: Double, b: Double) : this(doubleArrayOf(r, g, b))

    companion object {
        val black = ColorD(0.0, 0.0, 0.0)
        val white = ColorD(1.0, 1.0, 1.0)
    }

    operator fun plusAssign(other: Double) {
        assert(this.array.size == 3)

        for (i in this.array.indices) {
            array[i] = this.array[i] + other
        }
    }

    operator fun plusAssign(other: ColorD) {
        assert(this.array.size == 3)
        assert(other.array.size == 3)

        for (i in this.array.indices) {
            array[i] = this.array[i] + other.array[i]
        }
    }

    operator fun minusAssign(other: Double) {
        assert(this.array.size == 3)

        for (i in this.array.indices) {
            array[i] = this.array[i] - other
        }
    }

    operator fun timesAssign(other: Double) {
        assert(this.array.size == 3)

        for (i in this.array.indices) {
            array[i] = this.array[i] * other
        }
    }

    operator fun timesAssign(other: ColorD) {
        assert(this.array.size == 3)
        assert(other.array.size == 3)

        for (i in this.array.indices) {
            array[i] = this.array[i] * other.array[i]
        }
    }

    operator fun divAssign(other: Double) {
        assert(this.array.size == 3)

        for (i in this.array.indices) {
            array[i] = this.array[i] / other
        }
    }

    fun multiply(other: ColorD): ColorD {
        assert(this.array.size == 3)
        assert(other.array.size == 3)

        val r = DoubleArray(this.array.size)
        for (i in this.array.indices) {
            array[i] = this.array[i] * other.array[i]
        }

        return ColorD(r)
    }

    fun rgbInt(): Int {
        assert(this.array.size == 3)

        val red = componentByte(array[0])
        val green = componentByte(array[1])
        val blue = componentByte(array[2])
        return red.shl(16) or green.shl(8) or blue
    }

    private fun componentByte(x: Double): Int = (x * 255).toInt()

    fun copy(): ColorD {
        return ColorD(DoubleArray(3) { array[it] })
    }
}
