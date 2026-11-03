package com.example.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OverlayDemoService : Service() {

  companion object {
    private val _isShowingOverlay = MutableStateFlow(false)
    val isShowingOverlay: StateFlow<Boolean> = _isShowingOverlay.asStateFlow()

    fun start(context: Context) {
      if (Settings.canDrawOverlays(context)) {
        val intent = Intent(context, OverlayDemoService::class.java)
        context.startService(intent)
      }
    }

    fun stop(context: Context) {
      val intent = Intent(context, OverlayDemoService::class.java)
      context.stopService(intent)
    }
  }

  private var windowManager: WindowManager? = null
  private var overlayView: View? = null

  override fun onBind(intent: Intent?): IBinder? = null

  @SuppressLint("ClickableViewAccessibility")
  override fun onCreate() {
    super.onCreate()
    if (!Settings.canDrawOverlays(this)) {
      stopSelf()
      return
    }

    windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

    val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
      @Suppress("DEPRECATION")
      WindowManager.LayoutParams.TYPE_PHONE
    }

    val params = WindowManager.LayoutParams(
      WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.WRAP_CONTENT,
      layoutFlag,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
          WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
      PixelFormat.TRANSLUCENT
    ).apply {
      gravity = Gravity.TOP or Gravity.START
      x = 50
      y = 200
    }

    // Build interactive floating capsule
    val container = LinearLayout(this).apply {
      orientation = LinearLayout.HORIZONTAL
      setPadding(32, 20, 32, 20)
      gravity = Gravity.CENTER_VERTICAL

      val bg = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = 60f
        setColor(0xFF0F172A.toInt())
        setStroke(3, 0xFF38BDF8.toInt())
      }
      background = bg
    }

    val dot = View(this).apply {
      val dotBg = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(0xFF10B981.toInt())
      }
      background = dotBg
      val size = 24
      layoutParams = LinearLayout.LayoutParams(size, size).apply {
        marginEnd = 16
      }
    }

    val text = TextView(this).apply {
      text = "Widget Flotante Activo • Permiso Concedido"
      setTextColor(Color.WHITE)
      textSize = 13f
      typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    val closeBtn = ImageView(this).apply {
      setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
      setColorFilter(0xFF94A3B8.toInt())
      val size = 48
      layoutParams = LinearLayout.LayoutParams(size, size).apply {
        marginStart = 20
      }
      setOnClickListener {
        stopSelf()
      }
    }

    container.addView(dot)
    container.addView(text)
    container.addView(closeBtn)

    // Touch & Drag listener
    var initialX = 0
    var initialY = 0
    var initialTouchX = 0f
    var initialTouchY = 0f

    container.setOnTouchListener { _, event ->
      when (event.action) {
        MotionEvent.ACTION_DOWN -> {
          initialX = params.x
          initialY = params.y
          initialTouchX = event.rawX
          initialTouchY = event.rawY
          true
        }
        MotionEvent.ACTION_MOVE -> {
          params.x = initialX + (event.rawX - initialTouchX).toInt()
          params.y = initialY + (event.rawY - initialTouchY).toInt()
          windowManager?.updateViewLayout(container, params)
          true
        }
        else -> false
      }
    }

    try {
      windowManager?.addView(container, params)
      overlayView = container
      _isShowingOverlay.value = true
    } catch (_: Exception) {
      stopSelf()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    overlayView?.let {
      try {
        windowManager?.removeView(it)
      } catch (_: Exception) {}
      overlayView = null
    }
    _isShowingOverlay.value = false
  }
}
