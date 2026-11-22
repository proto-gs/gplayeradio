package sound.gplayeraudio.ui.components


import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import sound.gplayeraudio.model.CoverShape
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class MaterialCoverShape(private val shapeType: CoverShape) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = min(w, h) / 2f
        val path = Path()

        when (shapeType) {
            CoverShape.SQUARE -> {
                path.moveTo(0f, 0f)
                path.lineTo(w, 0f)
                path.lineTo(w, h)
                path.lineTo(0f, h)
                path.close()
            }
            CoverShape.ROUNDED -> {
                val cr = r * 0.25f
                path.addRoundRect(
                    RoundRect(0f, 0f, w, h, CornerRadius(cr, cr))
                )
            }
            CoverShape.CIRCLE -> {
                path.addOval(Rect(0f, 0f, w, h))
            }
            CoverShape.SQUIRCLE -> {
                val n = 3.5
                val steps = 120
                for (i in 0..steps) {
                    val t = 2.0 * PI * i / steps
                    val cosT = cos(t)
                    val sinT = sin(t)
                    val x = cx + r * Math.signum(cosT).toFloat() * Math.pow(abs(cosT), 2.0 / n).toFloat()
                    val y = cy + r * Math.signum(sinT).toFloat() * Math.pow(abs(sinT), 2.0 / n).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
            }
            CoverShape.DIAMOND -> {
                path.moveTo(cx, 0f)
                path.lineTo(w, cy)
                path.lineTo(cx, h)
                path.lineTo(0f, cy)
                path.close()
            }
            CoverShape.PENTAGON -> buildPolygonPath(path, cx, cy, r, 5, -PI / 2.0)
            CoverShape.HEXAGON -> buildPolygonPath(path, cx, cy, r, 6, -PI / 2.0)
            CoverShape.COOKIE -> buildPolarWavePath(path, cx, cy, r, lobes = 8, depth = 0.08f)
            CoverShape.WAVE -> buildPolarWavePath(path, cx, cy, r, lobes = 12, depth = 0.06f)
            CoverShape.FLOWER -> buildPolarWavePath(path, cx, cy, r, lobes = 6, depth = 0.16f)
            CoverShape.CLOVER -> buildPolarWavePath(
                path, cx, cy, r, lobes = 4, depth = 0.22f, offsetAngle = PI / 4.0
            )
            CoverShape.BURST -> {
                val points = 12
                val innerR = r * 0.78f
                for (i in 0 until points * 2) {
                    val currentR = if (i % 2 == 0) r else innerR
                    val angle = i * PI / points - PI / 2.0
                    val x = cx + currentR * cos(angle).toFloat()
                    val y = cy + currentR * sin(angle).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
            }
            CoverShape.DROP -> {
                path.moveTo(cx, 0f)
                path.cubicTo(w * 0.95f, h * 0.42f, w, h * 0.72f, cx, h)
                path.cubicTo(0f, h * 0.72f, w * 0.05f, h * 0.42f, cx, 0f)
                path.close()
            }
            CoverShape.HEART -> {
                path.moveTo(cx, h * 0.28f)
                path.cubicTo(cx, h * 0.1f, w * 0.05f, h * 0.05f, w * 0.05f, h * 0.36f)
                path.cubicTo(w * 0.05f, h * 0.62f, cx, h * 0.85f, cx, h * 0.96f)
                path.cubicTo(cx, h * 0.85f, w * 0.95f, h * 0.62f, w * 0.95f, h * 0.36f)
                path.cubicTo(w * 0.95f, h * 0.05f, cx, h * 0.1f, cx, h * 0.28f)
                path.close()
            }
            CoverShape.ARCH -> {
                path.moveTo(0f, h)
                path.lineTo(0f, cy)
                path.arcTo(Rect(0f, 0f, w, h), 180f, 180f, false)
                path.lineTo(w, h)
                path.close()
            }
        }
        return Outline.Generic(path)
    }

    private fun buildPolygonPath(
        path: Path, cx: Float, cy: Float, r: Float, sides: Int, startAngle: Double
    ) {
        for (i in 0 until sides) {
            val angle = startAngle + 2.0 * PI * i / sides
            val x = cx + r * cos(angle).toFloat()
            val y = cy + r * sin(angle).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
    }

    private fun buildPolarWavePath(
        path: Path, cx: Float, cy: Float, r: Float, lobes: Int, depth: Float, offsetAngle: Double = 0.0
    ) {
        val steps = 180
        val baseR = r * (1f - depth)
        val amp = r * depth
        for (i in 0..steps) {
            val theta = 2.0 * PI * i / steps
            val currentR = baseR + amp * cos(lobes.toDouble() * (theta + offsetAngle)).toFloat()
            val x = cx + currentR * cos(theta).toFloat()
            val y = cy + currentR * sin(theta).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
    }
}
