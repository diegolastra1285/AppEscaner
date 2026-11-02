package com.escaner.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.escaner.app.ui.home.components.EmptyHistoryView
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyHistoryView_muestraMensajeAmigable() {
        composeTestRule.setContent {
            EmptyHistoryView()
        }

        composeTestRule
            .onNodeWithText("Aún no tienes documentos escaneados")
            .assertIsDisplayed()
    }
}
