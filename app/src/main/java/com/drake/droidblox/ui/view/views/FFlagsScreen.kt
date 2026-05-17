package com.drake.droidblox.ui.view.views

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.drake.droidblox.DBApplication
import com.drake.droidblox.logger.TestLogger
import com.drake.droidblox.sharedprefs.FastFlagsManager
import com.drake.droidblox.sharedprefs.SettingsManager
import com.drake.droidblox.ui.components.BasicScreen
import com.drake.droidblox.ui.components.DropdownItem
import com.drake.droidblox.ui.components.ExtendedDropdown
import com.drake.droidblox.ui.components.ExtendedSlider
import com.drake.droidblox.ui.components.ExtendedSwitch
import com.drake.droidblox.ui.components.SectionText
import com.drake.droidblox.ui.view.viewmodels.FFlagsScreenVM

@Composable
fun FFlagsScreen(
    viewModel: FFlagsScreenVM = hiltViewModel(),
    navController: NavController? = null
) {
    /*
    follows the allowlist: https://devforum.roblox.com/t/allowlist-for-local-client-configuration-via-fast-flags/3966569
    Geometry:

    DFIntCSGLevelOfDetailSwitchingDistance
    DFIntCSGLevelOfDetailSwitchingDistanceL12
    DFIntCSGLevelOfDetailSwitchingDistanceL23
    DFIntCSGLevelOfDetailSwitchingDistanceL34

    Rendering:

    DFFlagTextureQualityOverrideEnabled
    DFIntTextureQualityOverride
    FIntDebugForceMSAASamples
    DFFlagDisableDPIScale
    FFlagDebugSkyGray
    DFFlagDebugPauseVoxelizer
    DFIntDebugFRMQualityLevelOverride
    FIntFRMMaxGrassDistance
    FIntFRMMinGrassDistance
    FFlagDebugGraphicsPreferVulkan
    FFlagDebugGraphicsPreferOpenGL

    User Interface:

    FIntGrassMovementReducedMotionFactor

    the excluded fflags for android will include the following:
    FFlagHandleAltEnterFullscreenManually
    FFlagDebugGraphicsPreferD3D11
    */

    val currentFFlags = viewModel.fflagsManager.fflags

    BasicScreen("Fast Flags", navController, useLazyColumn = true, lazyColumnContents = {
        // god forgive me for the item lambdas
        item { ExtendedSwitch(
            "Allow DroidBlox to manage Fast Flags",
            "Disabling this will prevent anything configured here from being applied to Roblox.",
            viewModel.settingsManager.applyFFlags
        ) { viewModel.settingsManager.applyFFlags = it } }

        item { SectionText("Rendering", "🎨") }

        //TODO("Extended Dropdown Anti-aliasing quality (MSAA)")
        item { ExtendedDropdown(
            "Anti-aliasing quality (MSAA)",
            "Smoothens the jagged edges to make textures detailed.",
            listOf(
                DropdownItem("Automatic") {
                    viewModel.fflagsManager.edit { delete("FIntDebugForceMSAASamples")}
                },
                DropdownItem("1x") {
                    viewModel.fflagsManager.edit { set("FIntDebugForceMSAASamples", "1") }
                },
                DropdownItem("2x") {
                    viewModel.fflagsManager.edit { set("FIntDebugForceMSAASamples", "2") }
                },
                DropdownItem("4x") {
                    viewModel.fflagsManager.edit { set("FIntDebugForceMSAASamples", "4") }
                }
            )
        ) }
        item { ExtendedDropdown(
            "Rendering mode",
            "Choose what rendering API to use for Roblox",
            listOf(
                DropdownItem("Automatic") {
                    viewModel.fflagsManager.edit {
                        listOf(
                            "FFlagDebugGraphicsPreferVulkan",
                            "FFlagDebugGraphicsPreferOpenGL"
                        ).forEach {
                            delete(it)
                        }
                    }
                },
                DropdownItem("Vulkan") {
                    viewModel.fflagsManager.edit {
                        delete("FFlagDebugGraphicsPreferOpenGL")
                        set("FFlagDebugGraphicsPreferVulkan", "true")
                    }
                },
                DropdownItem("OpenGL") { // not necessary but sstill :3
                    viewModel.fflagsManager.edit {
                        delete("FFlagDebugGraphicsPreferVulkan")
                        set("FFlagDebugGraphicsPreferOpenGL", "true")
                    }
                }
            )
        ) }
        item { ExtendedDropdown(
            "Texture quality",
            "Choose what level of texture quality to render",
            items = listOf(
                DropdownItem("Automatic") {
                    viewModel.fflagsManager.edit {
                        listOf(
                            "DFFlagTextureQualityOverrideEnabled",
                            "DFIntTextureQualityOverride"
                        ).forEach {
                            delete(it)
                        }
                    }
                },
                DropdownItem("Level 0") {
                    viewModel.fflagsManager.edit {
                        set("DFFlagTextureQualityOverrideEnabled", "true")
                        set("DFIntTextureQualityOverride", "0")
                    }
                },
                DropdownItem("Level 1") {
                    viewModel.fflagsManager.edit {
                        set("DFFlagTextureQualityOverrideEnabled", "true")
                        set("DFIntTextureQualityOverride", "1")
                    }
                },
                DropdownItem("Level 2") {
                    viewModel.fflagsManager.edit {
                        set("DFFlagTextureQualityOverrideEnabled", "true")
                        set("DFIntTextureQualityOverride", "2")
                    }
                },
                DropdownItem("Level 3") {
                    viewModel.fflagsManager.edit {
                        set("DFFlagTextureQualityOverrideEnabled", "true")
                        set("DFIntTextureQualityOverride", "3")
                    }
                },
            )
        ) }
        item { ExtendedSwitch(
            "Override sky to solid gray",
            "Overrides the sky into a solid gray color",
            currentFFlags["FFlagDebugSkyGray"].toBoolean(),
            interactive = viewModel.settingsManager.applyFFlags
        ) {
            viewModel.fflagsManager.edit { set("FFlagDebugSkyGray", it.toString()) }
        } }
        item { ExtendedSwitch( // TODO: Improve title and subtitle
            "Pause voxelizer",
            "Pauses the voxelizer",
            currentFFlags["DBFFlagDebugPauseVoxelizer"].toBoolean(),
            interactive = viewModel.settingsManager.applyFFlags
        ) {
            viewModel.fflagsManager.edit { set("DFFlagDebugPauseVoxelizer", it.toString()) }
        } }
        item { ExtendedSwitch(
            "Disable DPI scaling",
            "Renders at native resolution for sharper visuals",
            currentFFlags["DFFlagDisableDPIScale"].toBoolean(),
            interactive = viewModel.settingsManager.applyFFlags
        ) {
            viewModel.fflagsManager.edit { set("DFFlagDisableDPIScale", it.toString()) }
        } }
        item { ExtendedSlider(
            "Override quality level",
            "Overrides the quality level",
            currentFFlags["DFIntDebugFRMQualityLevelOverride"],
            valueRange = 0f..10f,
            steps = 10,
            interactive = viewModel.settingsManager.applyFFlags,
            valueLabel = { "Level ${it.toInt()}" }
        ) {
            viewModel.fflagsManager.edit { set("DFIntDebugFRMQualityLevelOverride", it) }
        } }
        item { ExtendedSlider(
            "FPS unlock",
            "Unlocks the frame rate cap (may not work on all devices)",
            currentFFlags["DFIntFpsUnlock"],
            valueRange = 30f..240f,
            steps = 210,
            interactive = viewModel.settingsManager.applyFFlags,
            valueLabel = { "${it.toInt()} FPS" }
        ) {
            viewModel.fflagsManager.edit { set("DFIntFpsUnlock", it) }
        } }
        item { ExtendedSlider(
            "Minimum grass distance",
            "Set the minimum distance of rendering grass",
            currentFFlags["FIntFRMMinGrassDistance"],
            valueRange = 0f..500f,
            steps = 50,
            interactive = viewModel.settingsManager.applyFFlags,
            valueLabel = { "${it.toInt()} studs" }
        ) {
            viewModel.fflagsManager.edit { set("FIntFRMMinGrassDistance", it) }
        } }
        item { ExtendedSlider(
            "Maximum grass distance",
            "Set the maximum distance of rendering grass",
            currentFFlags["FIntFRMMaxGrassDistance"],
            valueRange = 0f..1000f,
            steps = 100,
            interactive = viewModel.settingsManager.applyFFlags,
            valueLabel = { "${it.toInt()} studs" }
        ) {
            viewModel.fflagsManager.edit { set("FIntFRMMaxGrassDistance", it) }
        } }

        item { SectionText("Geometry", "📐") }

        item { ExtendedSlider(
            "LOD for Polygons",
            "Overrides the LOD (Level of Detail) per stud",
            currentFFlags["DFIntCSGLevelOfDetailSwitchingDistance"],
            valueRange = 0f..100f,
            steps = 100,
            interactive = viewModel.settingsManager.applyFFlags,
            valueLabel = { "${it.toInt()}" }
        ) {
            viewModel.fflagsManager.edit { set("DFIntCSGLevelOfDetailSwitchingDistance", it) }
        } }
        item { ExtendedSlider(
            "LOD for Polygons L12",
            "Overrides the LOD (Level of Detail) per stud",
            currentFFlags["DFIntCSGLevelOfDetailSwitchingDistanceL12"],
            valueRange = 0f..100f,
            steps = 100,
            interactive = viewModel.settingsManager.applyFFlags,
            valueLabel = { "${it.toInt()}" }
        ) {
            viewModel.fflagsManager.edit { set("DFIntCSGLevelOfDetailSwitchingDistanceL12", it) }
        } }
        item { ExtendedSlider(
            "LOD for Polygons L23",
            "Overrides the LOD (Level of Detail) per stud",
            currentFFlags["DFIntCSGLevelOfDetailSwitchingDistanceL23"],
            valueRange = 0f..100f,
            steps = 100,
            interactive = viewModel.settingsManager.applyFFlags,
            valueLabel = { "${it.toInt()}" }
        ) {
            viewModel.fflagsManager.edit { set("DFIntCSGLevelOfDetailSwitchingDistanceL23", it) }
        } }
        item { ExtendedSlider(
            "LOD for Polygons L34",
            "Overrides the LOD (Level of Detail) per stud",
            currentFFlags["DFIntCSGLevelOfDetailSwitchingDistanceL34"],
            valueRange = 0f..100f,
            steps = 100,
            interactive = viewModel.settingsManager.applyFFlags,
            valueLabel = { "${it.toInt()}" }
        ) {
            viewModel.fflagsManager.edit { set("DFIntCSGLevelOfDetailSwitchingDistanceL34", it) }
        } }

        item { SectionText("User Interface", "🖥️") }

        item { ExtendedSlider(
            "Grass movement reduced motion factor",
            "Reduces grass movement animation by this percentage",
            currentFFlags["FIntGrassMovementReducedMotionFactor"],
            valueRange = 0f..100f,
            steps = 100,
            interactive = viewModel.settingsManager.applyFFlags,
            valueLabel = { "${it.toInt()}%" }
        ) {
            viewModel.fflagsManager.edit { set("FIntGrassMovementReducedMotionFactor", it) }
        } }
        item { ExtendedSlider(
            "Max notifications",
            "Limits the number of notifications shown",
            currentFFlags["DFIntMaxNotifications"],
            valueRange = 0f..20f,
            steps = 20,
            interactive = viewModel.settingsManager.applyFFlags,
            valueLabel = { "${it.toInt()}" }
        ) {
            viewModel.fflagsManager.edit { set("DFIntMaxNotifications", it) }
        } }
        item { ExtendedSwitch(
            "Skip teleport app prompt",
            "Skips the 'Open in App?' dialog when teleporting",
            currentFFlags["FFlagDebugDisableTeleportAppPrompt"].toBoolean(),
            interactive = viewModel.settingsManager.applyFFlags
        ) {
            viewModel.fflagsManager.edit { set("FFlagDebugDisableTeleportAppPrompt", it.toString()) }
        } }
    })
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
internal fun PreviewFFlagsScreen() {
    CompositionLocalProvider(
        LocalContext provides LocalContext.current
    ) {
        val logger = TestLogger
        FFlagsScreen(FFlagsScreenVM(
            settingsManager = SettingsManager(
                logger = logger,
                context = LocalContext.current
            ),
            fflagsManager = FastFlagsManager(
                logger = logger,
                context = LocalContext.current
            )
        ))
    }
}