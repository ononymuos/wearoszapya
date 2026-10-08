package com.bastyoliva.wearoszapya.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import com.bastyoliva.wearable.delete.DeleteRoute
import com.bastyoliva.wearable.file_list.FileListRoute
import com.bastyoliva.wearable.video.VideoRoute
import com.bastyoliva.wearable.images.GalleryRoute
import com.bastyoliva.wearable.home.HomeRoute
import com.bastyoliva.wearable.music.MusicRoute
import com.bastyoliva.wearable.navigation.Routes
import com.bastyoliva.wearable.new_directory.NewDirectoryRoute
import com.bastyoliva.wearable.pdf_viewer.PdfViewerRoute
import com.bastyoliva.wearable.rename.RenameRoute
import com.bastyoliva.wearable.settings.SettingsRoute
import com.bastyoliva.wearable.text_viewer.TextViewerRoute
import com.bastyoliva.wearable.theming.ThemingRoute
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.nav.SwipeDismissableNavHost
import com.google.android.horologist.compose.nav.composable


@OptIn(ExperimentalHorologistApi::class)
@Composable
fun NavigationGraph(
    navController: NavHostController
) {
    SwipeDismissableNavHost(
        navController = navController, startDestination = Routes.Home
    ) {

        composable<Routes.Home> {
            HomeRoute()
        }

        composable<Routes.Settings> {
            SettingsRoute()
        }

        composable<Routes.Gallery> {
            GalleryRoute()
        }
        composable<Routes.Theming> {
            ThemingRoute()
        }


        composable<Routes.Music> {
            MusicRoute()
        }

        composable<Routes.Video> {
            VideoRoute()
        }

        composable<Routes.FilesList> { backStackEntry ->

            val args = backStackEntry.toRoute<Routes.FilesList>()

            FileListRoute(
                path = args.path
            )
        }


        composable<Routes.TextViewer> { backStackEntry ->

            val args = backStackEntry.toRoute<Routes.TextViewer>()

            TextViewerRoute(
                uriString = args.uriString
            )
        }

        composable<Routes.PdfViewer> { backStackEntry ->

            val args = backStackEntry.toRoute<Routes.PdfViewer>()

            PdfViewerRoute(
                uriString = args.uriString
            )
        }


        composable<Routes.Rename> { backStackEntry ->

            val args = backStackEntry.toRoute<Routes.Rename>()

            RenameRoute(
                path = args.path
            )
        }

        composable<Routes.NewDirectory> { backStackEntry ->

            val args = backStackEntry.toRoute<Routes.NewDirectory>()

            NewDirectoryRoute(
                path = args.path
            )
        }

        composable<Routes.Delete> { backStackEntry ->
            val args = backStackEntry.toRoute<Routes.Delete>()
            DeleteRoute(
                paths = args.paths
            )
        }

    }
}
