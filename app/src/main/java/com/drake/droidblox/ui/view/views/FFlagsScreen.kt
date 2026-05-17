package com.drake.droidblox.ui.view.views

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
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
import com.drake.droidblox.ui.components.ExtendedSwitch
import com.drake.droidblox.ui.components.ExtendedTextField
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

        item { SectionText("Rendering") }

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
        item { ExtendedTextField( // TODO: Improve title and subtitle
            "Override quality level",
            "Overrides the quality level",
            KeyboardType.Number,
            currentFFlags["DFIntDebugFRMQualityLevelOverride"]
        ) {
            viewModel.fflagsManager.edit { set("DFIntDebugFRMQualityLevelOverride", it) }
        } }
        item { ExtendedTextField( // TODO: Improve title and subtitle
            "Minimum grass distance",
            "Set the minimum distance of rendering grass",
            KeyboardType.Number,
            currentFFlags["FIntFRMMinGrassDistance"]
        ) {
            viewModel.fflagsManager.edit { set("FIntFRMMinGrassDistance", it) }
        } }
        item { ExtendedTextField( // TODO: Improve title and subtitle
            "Maximum grass distance",
            "Set the maximum distance of rendering grass",
            KeyboardType.Number,
            currentFFlags["FIntFRMMaxGrassDistance"]
        ) {
            viewModel.fflagsManager.edit { set("FIntFRMMaxGrassDistance", it) }
        } }

        item { SectionText("Geometry") }

        item { ExtendedTextField( // TODO: Improve title and subtitle
            "LOD for Polygons",
            "Overrides the LOD (Level of Detail) per stud",
            KeyboardType.Number,
            currentFFlags["DFIntCSGLevelOfDetailSwitchingDistance"]
        ) {
            viewModel.fflagsManager.edit { set("DFIntCSGLevelOfDetailSwitchingDistance", it) }
        } }
        item { ExtendedTextField( // TODO: Improve title and subtitle
            "LOD for Polygons L12",
            "Overrides the LOD (Level of Detail) per stud",
            KeyboardType.Number,
            currentFFlags["DFIntCSGLevelOfDetailSwitchingDistanceL12"]
        ) {
            viewModel.fflagsManager.edit { set("DFIntCSGLevelOfDetailSwitchingDistanceL12", it) }
        } }
        item { ExtendedTextField( // TODO: Improve title and subtitle
            "LOD for Polygons L23",
            "Overrides the LOD (Level of Detail) per stud",
            KeyboardType.Number,
            currentFFlags["DFIntCSGLevelOfDetailSwitchingDistanceL23"]
        ) {
            viewModel.fflagsManager.edit { set("DFIntCSGLevelOfDetailSwitchingDistanceL23", it) }
        } }
        item { ExtendedTextField( // TODO: Improve title and subtitle
            "LOD for Polygons L34",
            "Overrides the LOD (Level of Detail) per stud",
            KeyboardType.Number,
            currentFFlags["DFIntCSGLevelOfDetailSwitchingDistanceL34"]
        ) {
            viewModel.fflagsManager.edit { set("DFIntCSGLevelOfDetailSwitchingDistanceL34", it) }
        } }

        item { SectionText("User Interface") }

        item { ExtendedTextField( // TODO: Improve title and subtitle
            "Grass movement reduced motion factor",
            "Overrides the Grass movement reduced motion factor", // what the FUCK should i type
            KeyboardType.Number,
            currentFFlags["FIntGrassMovementReducedMotionFactor"]
        ) {
            viewModel.fflagsManager.edit { set("FIntGrassMovementReducedMotionFactor", it) }
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