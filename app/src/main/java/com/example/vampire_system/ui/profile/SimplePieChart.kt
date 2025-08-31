package com.example.vampire_system.ui.profile

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class SimplePieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val data = mutableListOf<PieSlice>()
    
    init {
        textPaint.apply {
            color = Color.WHITE
            textSize = 30f
            textAlign = Paint.Align.CENTER
        }
    }
    
    data class PieSlice(
        val value: Float,
        val label: String,
        val color: Int
    )
    
    fun setData(slices: List<PieSlice>) {
        data.clear()
        data.addAll(slices)
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (data.isEmpty()) return
        
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(centerX, centerY) * 0.8f
        
        val total = data.sumOf { it.value.toDouble() }.toFloat()
        var startAngle = -90f // Start from top
        
        data.forEach { slice ->
            val sweepAngle = (slice.value / total) * 360f
            
            // Draw pie slice
            paint.color = slice.color
            val rect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
            canvas.drawArc(rect, startAngle, sweepAngle, true, paint)
            
            // Draw label
            val midAngle = startAngle + sweepAngle / 2
            val labelRadius = radius * 0.6f
            val labelX = centerX + (labelRadius * cos(Math.toRadians(midAngle.toDouble()))).toFloat()
            val labelY = centerY + (labelRadius * sin(Math.toRadians(midAngle.toDouble()))).toFloat()
            
            canvas.drawText(slice.label, labelX, labelY + 10f, textPaint)
            
            startAngle += sweepAngle
        }
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = 300
        val width = resolveSize(desiredSize, widthMeasureSpec)
        val height = resolveSize(desiredSize, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}
