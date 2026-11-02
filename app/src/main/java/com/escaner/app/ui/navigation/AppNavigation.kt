package com.escaner.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.escaner.app.data.model.PaginaDocumento
import com.escaner.app.data.model.SesionLote
import com.escaner.app.ui.camera.CameraScreen
import com.escaner.app.ui.crop.CropScreen
import com.escaner.app.ui.home.HomeScreen
import com.escaner.app.ui.review.ReviewScreen
import java.io.File

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Camera : Screen("camera")
    data object Crop : Screen("crop")
    data object Review : Screen("review")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    var sesionLote by remember { mutableStateOf(SesionLote()) }
    var paginaEnEdicion by remember { mutableStateOf<PaginaDocumento?>(null) }
    var capturaTemporalFile by remember { mutableStateOf<File?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCamera = {
                    sesionLote = SesionLote()
                    paginaEnEdicion = null
                    capturaTemporalFile = null
                    navController.navigate(Screen.Camera.route)
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                paginasCount = sesionLote.paginas.size,
                onPhotoCaptured = { photoFile ->
                    capturaTemporalFile = photoFile
                    navController.navigate(Screen.Crop.route)
                },
                onBack = {
                    if (sesionLote.paginas.isNotEmpty()) {
                        navController.navigate(Screen.Review.route) {
                            popUpTo(Screen.Camera.route) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(Screen.Crop.route) {
            val fileToCrop = paginaEnEdicion?.archivoCapturaOriginal ?: capturaTemporalFile
            CropScreen(
                imageFile = fileToCrop,
                paginaExistente = paginaEnEdicion,
                onCropConfirmed = { paginaProcesada ->
                    if (paginaEnEdicion != null) {
                        // Updating an existing page
                        val updatedList = sesionLote.paginas.map {
                            if (it.id == paginaEnEdicion!!.id) paginaProcesada else it
                        }
                        sesionLote = sesionLote.copy(paginas = updatedList)
                    } else {
                        // Adding a new page to the batch
                        val nuevaLista = sesionLote.paginas + paginaProcesada.copy(indice = sesionLote.paginas.size + 1)
                        sesionLote = sesionLote.copy(paginas = nuevaLista)
                    }
                    paginaEnEdicion = null
                    capturaTemporalFile = null
                    navController.navigate(Screen.Review.route) {
                        popUpTo(Screen.Camera.route)
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Review.route) {
            ReviewScreen(
                sesionLote = sesionLote,
                onUpdateSesion = { updated ->
                    sesionLote = updated
                },
                onAddPage = {
                    paginaEnEdicion = null
                    capturaTemporalFile = null
                    navController.navigate(Screen.Camera.route)
                },
                onAdjustEdges = { pagina ->
                    paginaEnEdicion = pagina
                    navController.navigate(Screen.Crop.route)
                },
                onFinishSave = {
                    sesionLote = SesionLote()
                    paginaEnEdicion = null
                    capturaTemporalFile = null
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onDiscardBatch = {
                    sesionLote = SesionLote()
                    paginaEnEdicion = null
                    capturaTemporalFile = null
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
