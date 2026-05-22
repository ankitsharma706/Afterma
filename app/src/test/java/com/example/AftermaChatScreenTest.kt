package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.example.features.chat.AftermaChatScreen
import com.example.ui.theme.AftermaTheme
import com.example.ui.viewmodel.MainViewModel
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class AftermaChatScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        if (FirebaseApp.getApps(application).isEmpty()) {
            FirebaseApp.initializeApp(
                application,
                FirebaseOptions.Builder()
                    .setApplicationId("1:1234567890:android:1234567890")
                    .setApiKey("fake_api_key")
                    .setProjectId("fake-project-id")
                    .build()
            )
        }
        viewModel = MainViewModel(application)
    }

    @Test
    fun chat_screen_screenshot() {
        composeTestRule.setContent {
            AftermaTheme {
                AftermaChatScreen(
                    viewModel = viewModel,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/afterma_chat_screen.png")
    }
}
