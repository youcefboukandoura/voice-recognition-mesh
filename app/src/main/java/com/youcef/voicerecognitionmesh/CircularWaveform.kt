import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularWaveform(
    modifier: Modifier = Modifier, amplitude: Float
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension / 2f
        val waveColor = Color(0xFF00FFFF)

        // Draw distorted circles
        for (i in 1..6) {
            val baseRadius = maxRadius * (1 - i * 0.15f)
            val path = Path()

            for (angle in 0..360 step 5) {
                val radian = angle * PI / 180
                val distortionAmount =
                    amplitude * 50 * (1 - i * 0.15f) // Decrease distortion for inner circles
                val distortedRadius = baseRadius + distortionAmount * sin(radian * 10)

                val x = center.x + distortedRadius * cos(radian)
                val y = center.y + distortedRadius * sin(radian)

                if (angle == 0) {
                    path.moveTo(x.toFloat(), y.toFloat())
                } else {
                    path.lineTo(x.toFloat(), y.toFloat())
                }
            }

            path.close()
            drawPath(
                path = path, color = waveColor, style = Stroke(width = 2f)
            )
        }

        // Draw outer circle (unchanged)
        drawCircle(
            color = waveColor, center = center, radius = maxRadius, style = Stroke(width = 2f)
        )
    }
}

@Preview
@Composable
fun CircularWaveformPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularWaveform(amplitude = 0.5f)
    }
}