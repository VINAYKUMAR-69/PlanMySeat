package com.simats.automaticexamseatting

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.Locale
import kotlin.math.*

class BarChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var data: Map<String, Int> = emptyMap()
        set(value) {
            field = value
            invalidate()
        }

    private var touchedIndex: Int = -1
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 28f
        textAlign = Paint.Align.CENTER
        color = Color.parseColor("#64748B")
    }
    
    private val tooltipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E293B")
        style = Paint.Style.FILL
    }
    
    private val tooltipTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 32f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E2E8F0")
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) {
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("No data available", width / 2f, height / 2f, textPaint)
            return
        }

        val width = width.toFloat()
        val height = height.toFloat()
        val paddingLeft = 100f
        val paddingBottom = 80f
        val paddingTop = 60f
        val paddingRight = 40f
        
        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom
        
        val maxValRaw = data.values.maxOrNull()?.toFloat() ?: 0f
        val maxVal = if (maxValRaw <= 0f) 10f else maxValRaw
        val ySteps = 4
        
        for (i in 0..ySteps) {
            val yValue = (maxVal / ySteps) * i
            val yPos = height - paddingBottom - (yValue / maxVal * chartHeight)
            if (i > 0) {
                canvas.drawLine(paddingLeft, yPos, width - paddingRight, yPos, gridPaint)
            }
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(yValue.toInt().toString(), paddingLeft - 15f, yPos + 10f, textPaint)
        }

        val barAreaWidth = chartWidth / data.size
        val barWidth = barAreaWidth * 0.6f
        val barSpacing = barAreaWidth * 0.2f

        var x = paddingLeft + barSpacing
        data.entries.forEachIndexed { index, (branch, count) ->
            val barHeight = (count / maxVal) * chartHeight
            val barRect = RectF(x, height - paddingBottom - barHeight, x + barWidth, height - paddingBottom)
            
            paint.color = if (index == touchedIndex) Color.parseColor("#2563EB") else Color.parseColor("#3B82F6")
            canvas.drawRoundRect(barRect, 8f, 8f, paint)
            
            textPaint.textAlign = Paint.Align.CENTER
            val label = if (branch.length > 5) branch.substring(0, 4) + ".." else branch
            canvas.drawText(label, x + (barWidth / 2), height - paddingBottom + 40f, textPaint)
            
            if (index == touchedIndex) {
                val tooltipWidth = 100f
                val tooltipHeight = 60f
                val tx = x + (barWidth / 2)
                val ty = height - paddingBottom - barHeight - 20f
                val tooltipRect = RectF(tx - tooltipWidth/2, ty - tooltipHeight, tx + tooltipWidth/2, ty)
                canvas.drawRoundRect(tooltipRect, 12f, 12f, tooltipPaint)
                canvas.drawText(count.toString(), tx, ty - 18f, tooltipTextPaint)
            }
            x += barAreaWidth
        }
        
        paint.color = Color.parseColor("#94A3B8")
        paint.strokeWidth = 3f
        canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, paint)
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, height - paddingBottom, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (data.isEmpty()) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x = event.x
                val paddingLeft = 100f
                val paddingRight = 40f
                val chartWidth = width - paddingLeft - paddingRight
                if (x >= paddingLeft && x <= width - paddingRight) {
                    val barAreaWidth = chartWidth / data.size
                    val index = ((x - paddingLeft) / barAreaWidth).toInt()
                    if (index in 0 until data.size) {
                        if (touchedIndex != index) {
                            touchedIndex = index
                            invalidate()
                        }
                    }
                } else {
                    if (touchedIndex != -1) {
                        touchedIndex = -1
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchedIndex = -1
                invalidate()
                performClick()
            }
        }
        return true
    }
    
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var data: Map<String, Int> = emptyMap()
        set(value) {
            field = value
            invalidate()
        }

    private var touchedIndex: Int = -1
    private val colors = listOf("#3B82F6", "#8B5CF6", "#EC4899", "#F59E0B", "#10B981")
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 26f
        color = Color.parseColor("#64748B")
    }
    
    private val tooltipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E293B")
        style = Paint.Style.FILL
    }
    
    private val tooltipTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 30f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) {
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("No data available", width / 2f, height / 2f, textPaint)
            return
        }

        val total = data.values.sum().toFloat()
        if (total <= 0f) {
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("Total count is zero", width / 2f, height / 2f, textPaint)
            return
        }

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(width, height) / 3.5f
        
        var startAngle = 0f

        data.entries.forEachIndexed { index, entry ->
            val percentage = (entry.value / total) * 100f
            val sweepAngle = (entry.value / total) * 360f
            
            val sliceRadius = if (index == touchedIndex) radius * 1.05f else radius
            val sliceRect = RectF(centerX - sliceRadius, centerY - sliceRadius, centerX + sliceRadius, centerY + sliceRadius)
            
            paint.color = Color.parseColor(colors[index % colors.size])
            canvas.drawArc(sliceRect, startAngle, sweepAngle, true, paint)

            val middleAngle = startAngle + sweepAngle / 2
            
            val labelRadius = radius * 1.35f
            val labelX = centerX + labelRadius * cos(Math.toRadians(middleAngle.toDouble())).toFloat()
            val labelY = centerY + labelRadius * sin(Math.toRadians(middleAngle.toDouble())).toFloat()
            
            textPaint.color = Color.parseColor(colors[index % colors.size])
            textPaint.textAlign = if (labelX > centerX) Paint.Align.LEFT else Paint.Align.RIGHT
            canvas.drawText("${entry.key}: ${String.format(Locale.getDefault(), "%.1f", percentage)}%", labelX, labelY, textPaint)

            if (index == touchedIndex) {
                val tipRadius = radius * 0.6f
                val tipX = centerX + tipRadius * cos(Math.toRadians(middleAngle.toDouble())).toFloat()
                val tipY = centerY + tipRadius * sin(Math.toRadians(middleAngle.toDouble())).toFloat()
                
                canvas.drawCircle(tipX, tipY, 40f, tooltipPaint)
                canvas.drawText(entry.value.toString(), tipX, tipY + 10f, tooltipTextPaint)
            }

            startAngle += sweepAngle
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (data.isEmpty()) return false
        val centerX = width / 2f
        val centerY = height / 2f
        
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val dx = event.x - centerX
                val dy = event.y - centerY
                val distance = sqrt(dx * dx + dy * dy)
                val radius = min(width, height) / 3.5f
                
                if (distance <= radius * 1.1f) {
                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    if (angle < 0) angle += 360f
                    
                    val total = data.values.sum().toFloat()
                    if (total > 0) {
                        var currentStartAngle = 0f
                        var foundIndex = -1
                        
                        data.entries.forEachIndexed { index, entry ->
                            val sweep = (entry.value / total) * 360f
                            if (angle >= currentStartAngle && angle <= currentStartAngle + sweep) {
                                foundIndex = index
                            }
                            currentStartAngle += sweep
                        }
                        
                        if (touchedIndex != foundIndex) {
                            touchedIndex = foundIndex
                            invalidate()
                        }
                    }
                } else {
                    if (touchedIndex != -1) {
                        touchedIndex = -1
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchedIndex = -1
                invalidate()
                performClick()
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}

class DonutChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var data: Map<String, Int> = emptyMap()
        set(value) {
            field = value
            invalidate()
        }

    private val colors = listOf("#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6")
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 24f
        color = Color.parseColor("#64748B")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return

        val total = data.values.sum().toFloat()
        if (total <= 0) return
        
        val centerX = width / 2.5f
        val centerY = height / 2f
        val radius = min(width, height) / 4f
        val strokeWidth = radius * 0.4f
        paint.strokeWidth = strokeWidth
        
        val rect = RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        var startAngle = -90f

        data.entries.forEachIndexed { index, entry ->
            val sweepAngle = (entry.value / total) * 360f
            paint.color = Color.parseColor(colors[index % colors.size])
            canvas.drawArc(rect, startAngle, sweepAngle, false, paint)
            
            // Legend
            val legendX = width * 0.7f
            val legendY = 40f + index * 50f
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = paint.color }
            canvas.drawRect(legendX, legendY - 15f, legendX + 20f, legendY + 5f, dotPaint)
            
            val percentage = (entry.value / total * 100).toInt()
            canvas.drawText("${entry.key}", legendX + 30f, legendY, textPaint)
            canvas.drawText("$percentage%", legendX + 30f, legendY + 25f, textPaint.apply { textSize = 20f })
            textPaint.textSize = 24f

            startAngle += sweepAngle
        }
    }
}

class LineChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#10B981")
        strokeWidth = 6f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F1F5F9")
        strokeWidth = 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.size < 2) return

        val w = width.toFloat()
        val h = height.toFloat()
        val padding = 40f
        val chartW = w - 2 * padding
        val chartH = h - 2 * padding
        
        val maxValRaw = data.maxOrNull() ?: 0f
        val maxVal = if (maxValRaw <= 0f) 10f else maxValRaw
        val stepX = chartW / (data.size - 1)
        
        // Grid
        for (i in 0..4) {
            val y = padding + i * (chartH / 4)
            canvas.drawLine(padding, y, w - padding, y, gridPaint)
        }

        val path = Path()
        val fillPath = Path()
        
        data.forEachIndexed { index, value ->
            val x = padding + index * stepX
            val y = h - padding - (value / maxVal * chartH)
            
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h - padding)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
            
            if (index == data.size - 1) {
                fillPath.lineTo(x, h - padding)
                fillPath.close()
            }
        }

        // Area fill with gradient
        fillPaint.shader = LinearGradient(0f, padding, 0f, h - padding, 
            Color.parseColor("#2010B981"), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        canvas.drawPath(fillPath, fillPaint)
        
        canvas.drawPath(path, paint)
        
        // Dots
        paint.style = Paint.Style.FILL
        data.forEachIndexed { index, value ->
            val x = padding + index * stepX
            val y = h - padding - (value / maxVal * chartH)
            canvas.drawCircle(x, y, 8f, paint)
            
            val dotHolePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
            canvas.drawCircle(x, y, 4f, dotHolePaint)
        }
    }
}