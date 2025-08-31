package com.example.vampire_system.ui.profile

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class SimpleRadarChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val data = mutableListOf<RadarPoint>()
    
    init {
        textPaint.apply {
            color = Color.BLACK
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }
        
        gridPaint.apply {
            color = Color.LTGRAY
            strokeWidth = 2f
            style = Paint.Style.STROKE
            alpha = 150
        }
    }
    
    data class RadarPoint(
        val value: Float,
        val label: String,
        val color: Int
    )
    
    fun setData(points: List<RadarPoint>) {
        data.clear()
        data.addAll(points)
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (data.isEmpty()) return
        
        val centerX = width / 2f
        val centerY = height / 2f
        val maxRadius = min(centerX, centerY) * 0.8f
        
        // Draw grid circles with more visible lines
        for (i in 1..5) {
            val radius = (maxRadius * i) / 5f
            paint.color = Color.LTGRAY
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.alpha = 120
            canvas.drawCircle(centerX, centerY, radius, paint)
        }
        
        // Draw grid lines (spokes) with more visible lines
        val angleStep = 360f / data.size
        for (i in data.indices) {
            val angle = Math.toRadians((i * angleStep - 90).toDouble())
            val endX = centerX + (maxRadius * cos(angle)).toFloat()
            val endY = centerY + (maxRadius * sin(angle)).toFloat()
            
            paint.color = Color.LTGRAY
            paint.strokeWidth = 2f
            paint.alpha = 150
            canvas.drawLine(centerX, centerY, endX, endY, paint)
        }
        
        // Find max value for normalization
        val maxValue = data.maxOfOrNull { it.value } ?: 1f
        
        // Draw radar area
        if (data.size > 2) {
            val path = Path()
            var firstPoint = true
            
            for (i in data.indices) {
                val angle = Math.toRadians((i * angleStep - 90).toDouble())
                val normalizedValue = (data[i].value / maxValue) * maxRadius
                val x = centerX + (normalizedValue * cos(angle)).toFloat()
                val y = centerY + (normalizedValue * sin(angle)).toFloat()
                
                if (firstPoint) {
                    path.moveTo(x, y)
                    firstPoint = false
                } else {
                    path.lineTo(x, y)
                }
            }
            path.close()
            
            // Draw filled area
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#4CAF50")
            paint.alpha = 80
            canvas.drawPath(path, paint)
            
            // Draw area border
            paint.style = Paint.Style.STROKE
            paint.color = Color.parseColor("#2E7D32")
            paint.strokeWidth = 4f
            paint.alpha = 255
            canvas.drawPath(path, paint)
        }
        
        // Draw data points and labels
        for (i in data.indices) {
            val angle = Math.toRadians((i * angleStep - 90).toDouble())
            val normalizedValue = (data[i].value / maxValue) * maxRadius
            val x = centerX + (normalizedValue * cos(angle)).toFloat()
            val y = centerY + (normalizedValue * sin(angle)).toFloat()
            
            // Draw data point
            paint.style = Paint.Style.FILL
            paint.color = data[i].color
            paint.alpha = 255
            canvas.drawCircle(x, y, 10f, paint)
            
            // Draw label with better positioning to avoid overlap
            val labelRadius = maxRadius + 90f
            val labelX = centerX + (labelRadius * cos(angle)).toFloat()
            val labelY = centerY + (labelRadius * sin(angle)).toFloat()
            
            textPaint.color = Color.BLACK
            textPaint.textSize = 30f
            
            // Get text bounds for better positioning
            val textBounds = Rect()
            textPaint.getTextBounds(data[i].label, 0, data[i].label.length, textBounds)
            
            var adjustedLabelX = labelX
            var adjustedLabelY = labelY
            
            // Smart label positioning to avoid overlap
            when {
                i == 0 -> { // Top
                    adjustedLabelY -= 15f
                }
                i == data.size / 2 -> { // Bottom
                    adjustedLabelY += 35f
                }
                i < data.size / 2 -> { // Left side
                    adjustedLabelX -= textBounds.width() / 2f - 10f
                }
                else -> { // Right side
                    adjustedLabelX += textBounds.width() / 2f - 10f
                }
            }
            
            // Draw label background for better readability
            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            paint.alpha = 200
            val padding = 16f
            val labelRect = RectF(
                adjustedLabelX - textBounds.width() / 2f - padding,
                adjustedLabelY - textBounds.height() - padding,
                adjustedLabelX + textBounds.width() / 2f + padding,
                adjustedLabelY + padding
            )
            canvas.drawRoundRect(labelRect, 6f, 6f, paint)
            
            // Draw label text
            canvas.drawText(data[i].label, adjustedLabelX, adjustedLabelY, textPaint)
        }
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = 450
        val width = resolveSize(desiredSize, widthMeasureSpec)
        val height = resolveSize(desiredSize, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}
