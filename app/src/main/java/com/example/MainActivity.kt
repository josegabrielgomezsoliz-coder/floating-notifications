package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.PermissionManagerScreen
import com.example.ui.PermissionViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val permissionViewModel: PermissionViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        val lifecycleOwner = LocalLifecycleOwner.current

        // Automatic re-checking of all permissions when user returns to app from Android settings
        DisposableEffect(lifecycleOwner) {
          val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
              permissionViewModel.refreshPermissions()
            }
          }
          lifecycleOwner.lifecycle.addObserver(observer)
          onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
          }
        }

        Surface(modifier = Modifier.fillMaxSize()) {
          PermissionManagerScreen(viewModel = permissionViewModel)
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    permissionViewModel.refreshPermissions()
  }
}
