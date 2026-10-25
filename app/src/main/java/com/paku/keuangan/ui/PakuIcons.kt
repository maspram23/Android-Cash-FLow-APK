package com.paku.keuangan.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Ikon vektor ringan agar aplikasi tidak bergantung pada library ikon besar. */
object PakuIcons {

    private fun build(name: String, path: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).addPath(
            pathData = addPathNodes(path),
            fill = SolidColor(Color.Black)
        ).build()

    val Add: ImageVector by lazy {
        build("Add", "M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6V13z")
    }
    val Close: ImageVector by lazy {
        build(
            "Close",
            "M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12 19,6.41z"
        )
    }
    val Back: ImageVector by lazy {
        build("Back", "M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z")
    }
    val ChevronLeft: ImageVector by lazy {
        build("ChevronLeft", "M15.41,7.41L14,6l-6,6 6,6 1.41,-1.41L10.83,12 15.41,7.41z")
    }
    val ChevronRight: ImageVector by lazy {
        build("ChevronRight", "M8.59,16.59L10,18l6,-6 -6,-6 -1.41,1.41L13.17,12 8.59,16.59z")
    }
    val ChevronDown: ImageVector by lazy {
        build("ChevronDown", "M7.41,8.59L12,13.17l4.59,-4.58L18,10l-6,6 -6,-6 1.41,-1.41z")
    }
    val Check: ImageVector by lazy {
        build("Check", "M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41L9,16.17z")
    }
    val Delete: ImageVector by lazy {
        build(
            "Delete",
            "M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM19,4h-3.5l-1,-1h-5l-1,1H5v2h14V4z"
        )
    }
    val Edit: ImageVector by lazy {
        build(
            "Edit",
            "M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25zM20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41l-2.34,-2.34c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 3.75,3.75 1.83,-1.83z"
        )
    }
    val Home: ImageVector by lazy {
        build("Home", "M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8h5z")
    }
    val Chart: ImageVector by lazy {
        build("Chart", "M5,9.2h3V19H5V9.2zM10.6,5h2.8v14h-2.8V5zM16.2,13H19v6h-2.8V13z")
    }
    val Receipt: ImageVector by lazy {
        build(
            "Receipt",
            "M3,13h2v-2H3v2zM3,17h2v-2H3v2zM3,9h2V7H3v2zM7,13h14v-2H7v2zM7,17h14v-2H7v2zM7,7v2h14V7H7z"
        )
    }
    val Settings: ImageVector by lazy {
        build(
            "Settings",
            "M3,17v2h6v-2H3zM3,5v2h10V5H3zM13,21v-2h8v-2h-8v-2h-2v6h2zM7,9v2H3v2h4v2h2V9H7zM21,13v-2H11v2h10zM15,9h2V7h4V5h-4V3h-2v6z"
        )
    }
    val Search: ImageVector by lazy {
        build(
            "Search",
            "M15.5,14h-0.79l-0.28,-0.27C15.41,12.59 16,11.11 16,9.5 16,5.91 13.09,3 9.5,3S3,5.91 3,9.5 5.91,16 9.5,16c1.61,0 3.09,-0.59 4.23,-1.57l0.27,0.28v0.79l5,4.99L20.49,19l-4.99,-5zM9.5,14C7.01,14 5,11.99 5,9.5S7.01,5 9.5,5 14,7.01 14,9.5 11.99,14 9.5,14z"
        )
    }
    val Calendar: ImageVector by lazy {
        build(
            "Calendar",
            "M19,3h-1V1h-2v2H8V1H6v2H5c-1.11,0 -1.99,0.9 -1.99,2L3,21c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2V5c0,-1.1 -0.9,-2 -2,-2zM19,21H5V9h14v12zM7,11h5v5H7z"
        )
    }
    val TrendingUp: ImageVector by lazy {
        build("TrendingUp", "M16,6l2.29,2.29 -4.88,4.88 -4,-4L2,16.59 3.41,18l6,-6 4,4 6.3,-6.29L22,12V6h-6z")
    }
    val TrendingDown: ImageVector by lazy {
        build(
            "TrendingDown",
            "M16,18l2.29,-2.29 -4.88,-4.88 -4,4L2,7.41 3.41,6l6,6 4,-4 6.3,6.29L22,12v6h-6z"
        )
    }
    val Wallet: ImageVector by lazy {
        build(
            "Wallet",
            "M21,7.28V5c0,-1.1 -0.9,-2 -2,-2H5C3.89,3 3,3.9 3,5v14c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2v-2.28c0.59,-0.34 1,-0.98 1,-1.72V9c0,-0.74 -0.41,-1.38 -1,-1.72zM20,9v6h-7V9h7zM5,19V5h14v2h-6c-1.1,0 -2,0.9 -2,2v6c0,1.1 0.9,2 2,2h6v2H5z"
        )
    }
    val Download: ImageVector by lazy {
        build("Download", "M19,9h-4V3H9v6H5l7,7 7,-7zM5,18v2h14v-2H5z")
    }
    val Refresh: ImageVector by lazy {
        build(
            "Refresh",
            "M17.65,6.35C16.2,4.9 14.21,4 12,4c-4.42,0 -7.99,3.58 -7.99,8s3.57,8 7.99,8c3.73,0 6.84,-2.55 7.73,-6h-2.08c-0.82,2.33 -3.04,4 -5.65,4 -3.31,0 -6,-2.69 -6,-6s2.69,-6 6,-6c1.66,0 3.14,0.69 4.22,1.78L13,11h7V4l-2.35,2.35z"
        )
    }
    val Info: ImageVector by lazy {
        build(
            "Info",
            "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM13,17h-2v-6h2v6zM13,9h-2V7h2v2z"
        )
    }
    val ArrowUp: ImageVector by lazy {
        build("ArrowUp", "M4,12l1.41,1.41L11,7.83V20h2V7.83l5.58,5.59L20,12l-8,-8 -8,8z")
    }
    val ArrowDown: ImageVector by lazy {
        build("ArrowDown", "M20,12l-1.41,-1.41L13,16.17V4h-2v12.17l-5.58,-5.59L4,12l8,8 8,-8z")
    }
    val Backspace: ImageVector by lazy {
        build(
            "Backspace",
            "M22,3H7C6.31,3 5.77,3.35 5.41,3.88L0,12l5.41,8.11c0.36,0.53 0.9,0.89 1.59,0.89h15c1.1,0 2,-0.9 2,-2V5c0,-1.1 -0.9,-2 -2,-2zM19,15.59L17.59,17 14,13.41 10.41,17 9,15.59 12.59,12 9,8.41 10.41,7 14,10.59 17.59,7 19,8.41 15.41,12 19,15.59z"
        )
    }
}
