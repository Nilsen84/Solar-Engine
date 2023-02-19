package com.solartweaks.engine

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmErasure

val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    prettyPrint = true
}

fun loadConfig(file: File) =
    runCatching { json.decodeFromString<Configuration>(file.readText()) }
        .onFailure {
            println("Failed to load configuration from $file, using fallback!")
            it.printStackTrace()
        }
        .getOrElse { Configuration() }

val KProperty<*>.isSerialized
    get() = visibility == KVisibility.PUBLIC
            && !hasAnnotation<Transient>()
            && javaField != null

inline fun <reified T : Any> serializedPropertiesOf() =
    T::class.declaredMemberProperties.filter(KProperty<*>::isSerialized)

val KClass<*>.serializedProperties
    get() = declaredMemberProperties.filter(KProperty<*>::isSerialized)

@Serializable
data class Configuration(
    val modules: Modules = Modules(),
    val customCommands: CustomCommands = CustomCommands(),
    val debugPackets: Boolean = false
)

@Serializable
data class Modules(
    val metadata: Metadata = Metadata(),
    val removeStoreButton: RemoveStoreButton = RemoveStoreButton(),
    val discordRichPresence: DiscordRichPresence = DiscordRichPresence(),
    val privacy: Privacy = Privacy(),
    val removeFakeLevelHead: RemoveFakeLevelHead = RemoveFakeLevelHead(),
    val capeSystem: CapeSystem = CapeSystem(),
    val changeModStrings: ChangeModStrings = ChangeModStrings(),
    val windowTitle: WindowTitle = WindowTitle(),
    val websocketURL: WebsocketURL = WebsocketURL(),
    val lunarOverlays: LunarOverlays = LunarOverlays(),
    val clothCapes: ClothCapes = ClothCapes(),
    val hurtCamShake: HurtCamShake = HurtCamShake(),
    val removeMousePopup: RemoveMousePopup = RemoveMousePopup(),
    val removeProfilesCap: RemoveProfilesCap = RemoveProfilesCap(),
    val toggleSprintText: ToggleSprintText = ToggleSprintText(),
    val allowCrackedAccounts: AllowCrackedAccounts = AllowCrackedAccounts(),
    val fpsSpoof: FPSSpoof = FPSSpoof(),
    val noHitDelay: NoHitDelay = NoHitDelay(),
    val infiniteEmotes: InfiniteEmotes = InfiniteEmotes(),
    val metadataURL: MetadataURL = MetadataURL()
) {
    val modules
        get() = serializedPropertiesOf<Modules>()
            .map { it(this) }
            .filterIsInstance<Module>()
}

fun enabledModules() = globalConfiguration.modules.modules.filter { it.isEnabled }

inline fun <reified T : Module> getModule() = globalConfiguration.modules.modules.filterIsInstance<T>().firstOrNull()
    ?: error("No such module was registered: ${T::class.java}")

inline fun <reified T : Module> isModuleEnabled() = getModule<T>().isEnabled

@Serializable
sealed interface Module {
    val isEnabled: Boolean
}

@Serializable
@ModuleInfo("Metadata", "Allows you to remove certain unwanted features from Lunar Client")
data class Metadata(
    @OptionInfo(
        "Remove Mod Bans (Freelook)",
        "Prevents Lunar from setting mod settings based on the server you are on " +
                "(for example, this removes the Freelook ban)"
    )
    val removeServerIntegration: Boolean = true,

    @OptionInfo(
        "Remove Pinned Servers",
        "Removes the advertised servers on the Multiplayer screen"
    )
    val removePinnedServers: Boolean = true,

    @OptionInfo(
        "Remove Forced Mod Settings",
        "Prevents Lunar Client from forcing mod settings"
    )
    val removeModSettings: Boolean = true,

    @OptionInfo(
        "Remove Forced Client Settings",
        "Similar as Remove Mod Settings, except more general. Usually has no effect (but it might in the future)"
    )
    val removeClientSettings: Boolean = true,

    @OptionInfo(
        "Remove Blog Posts",
        "Removes advertisements of Lunar on the home screen of the client"
    )
    val removeBlogPosts: Boolean = false,

    override val isEnabled: Boolean = true
) : Module

@Serializable
@ModuleInfo(
    "Remove Store Button",
    "Removes Lunar's store button from the main menu"
)
data class RemoveStoreButton(override val isEnabled: Boolean = false) : Module

@Serializable
@ModuleInfo(
    "Discord Rich Presence",
    "Allows you to change the look-and-feel of the Discord Rich Presence"
)
data class DiscordRichPresence(
    @OptionInfo("Client ID", "If you don't know what this is, do not touch")
    val clientID: String = "920998351430901790",

    @OptionInfo(
        "Icon ID",
        "Allows you to set the icon ID (leave this as \"logo\" for the ST logo"
    )
    val icon: String = "logo",

    @OptionInfo("Icon Text", "Changes the hover text for the icon")
    val iconText: String = "Solar Tweaks",

    @OptionInfo(
        "AFK Text",
        "Text that will show up if the game screen is currently not active"
    )
    val afkText: String = "AFK",

    @OptionInfo("Menu Text", "Text that will show up if some kind of menu is opened")
    val menuText: String = "In Menu",

    @OptionInfo("Single Player Text", "Text that will show up if you are not playing on a server")
    val singlePlayerText: String = "Playing Singleplayer",

    @OptionInfo(
        "Dislay Activity",
        "If enabled, your current activity (singleplayer/multiplayer/menu/afk) will be shown in Discord"
    )
    val displayActivity: Boolean = true,

    @OptionInfo(
        "Show server IP",
        "If enabled, the address of the server you are on is show in Discord. Be careful with private SMPs!"
    )
    val showServerIP: Boolean = true,

    @OptionInfo(
        "Show Icon",
        "If enabled, an icon will be shown"
    )
    val showIcon: Boolean = true,
    override val isEnabled: Boolean = false
) : Module

@Serializable
@ModuleInfo(
    "Privacy",
    "Prevents Lunar Client from spying on your computer"
)
data class Privacy(override val isEnabled: Boolean = false) : Module

@Serializable
@ModuleInfo(
    "Remove Fake Level Head",
    "On Hypixel, nicked players will no longer get assigned a random level"
)
data class RemoveFakeLevelHead(override val isEnabled: Boolean = false) : Module

@Serializable
@ModuleInfo(
    "Cape System",
    "Allows you to change the cape/cosmetics system that the client will use"
)
data class CapeSystem(
    @OptionInfo("Server URL", "Paste the server URL here")
    val serverURL: String = "http://s.optifine.net",
    override val isEnabled: Boolean = false
) : Module

@Serializable
@ModuleInfo(
    "Change Mod Text",
    "Allows you to change texts shown by certain mods"
)
data class ChangeModStrings(
    @OptionInfo(
        "Nickhider Text",
        "The username that will show up if you enable self-nicking. " +
                "Particularly useful if you want a custom name with symbols and colors"
    )
    val nickhiderText: String = "You",

    @OptionInfo("FPS Text", "The text that the FPS counter will say (instead of FPS)")
    val fpsText: String = "FPS",

    @OptionInfo("CPS Text", "The text that the CPS counter will say (instead of CPS)")
    val cpsText: String = "CPS",

    @OptionInfo("Auto GG Command", "The command that will be executed instead of the default \"/achat gg\"")
    val autoGGCommand: String = "/ac gg",

    @OptionInfo("Level Head Text", "The text that will show up under someones username (instead of Level)")
    val levelHeadText: String = "Level",

    @OptionInfo("Reach Text", "The text that will show up in the Reach mod (instead of \"blocks\")")
    val reachText: String = "blocks",
    override val isEnabled: Boolean = false
) : Module

@Serializable
@ModuleInfo(
    "Websocket",
    "Allows you to modify the websocket URL that the client uses to fetch resources"
)
data class WebsocketURL(
    @OptionInfo("URL", "Paste the websocket URL you want to use here")
    val url: String = "wss://assetserver.lunarclientprod.com/connect",
    override val isEnabled: Boolean = false
) : Module

@Serializable
@ModuleInfo(
    "Window Title",
    "Allows you to set a custom title for the game's window"
)
data class WindowTitle(
    @OptionInfo("Title", "The new title that will show up")
    val title: String = "Solar Tweaks",
    @OptionInfo("Show Version", "Determines if the Engine version is displayed in the window title")
    val showVersion: Boolean = true,
    override val isEnabled: Boolean = true
) : Module

@Serializable
@ModuleInfo(
    "Overlays",
    "Allows you to use so-called \"Lunar Client overlays\""
)
data class LunarOverlays(override val isEnabled: Boolean = false) : Module

@Serializable
@ModuleInfo(
    "Cloth Cloaks",
    "Replaces all Lunar cloaks with a cloth cloak texture (might slow down your game)"
)
data class ClothCapes(override val isEnabled: Boolean = false) : Module

@Serializable
@ModuleInfo(
    "Hurt Camera Multiplier",
    "Changes how much the camera shakes when taking damage"
)
data class HurtCamShake(
    @OptionInfo(
        "Multiplier",
        "The amount of camera shake that you want. Set to 0 if you want to remove camera shake"
    )
    val multiplier: Float = .3f,
    override val isEnabled: Boolean = false
) : Module

@Serializable
@ModuleInfo(
    "No Mouse Polling Popup",
    "Removes the annoying popup about your mouse polling rate"
)
data class RemoveMousePopup(override val isEnabled: Boolean = false) : Module

@Serializable
@ModuleInfo(
    "Remove Profiles Cap",
    "Removes the maximum profiles count of 8"
)
data class RemoveProfilesCap(override val isEnabled: Boolean = false) : Module

//

@Serializable
@ModuleInfo(
    "Change Toggle Sprint Text",
    "Allows you to change the text shown by the Toggle Sprint/Sneak mod"
)
data class ToggleSprintText(
    @OptionInfo("Flying Text", "Text shown instead of \"Flying\"")
    val flyingText: String = "Flying",

    @OptionInfo("Flying Boost Text", "Text shown instead of \"Boost\"")
    val flyingBoostText: String = "Boost",

    @OptionInfo("Riding Text", "Text shown instead of \"Riding\"")
    val ridingText: String = "Riding",

    @OptionInfo("Descending Text", "Text shown instead of \"Descending\"")
    val descendingText: String = "Descending",

    @OptionInfo("Dismounting Text", "Text shown instead of \"Dismounting\"")
    val dismountingText: String = "Dismounting",

    @OptionInfo("Sneaking Text", "Text shown instead of \"Sneaking\"")
    val sneakingText: String = "Sneaking",

    @OptionInfo("Toggled Text", "Text shown instead of \"Toggled\"")
    val toggledText: String = "Toggled",

    @OptionInfo("Sprinting Text", "Text shown instead of \"Sprinting\"")
    val sprintingText: String = "Sprinting",
    override val isEnabled: Boolean = false
) : Module

@Serializable
@ModuleInfo(
    "Allow Cracked Accounts",
    "Allows you to use cracked/offline Minecraft accounts on Lunar Client (remove all premium accounts to use)"
)
data class AllowCrackedAccounts(
    @OptionInfo("Cracked Username", "Set this field to the username you want")
    val crackedUsername: String = "Steve",
    override val isEnabled: Boolean = false
) : Module

@Serializable
@ModuleInfo(
    "FPS Spoof",
    "Allows you to increase the number your FPS counter says by multiplying by a constant"
)
data class FPSSpoof(
    @OptionInfo(
        "Multiplier",
        "Specifies the number to multiply the fps value with"
    )
    val multiplier: Float = 1.0f,
    override val isEnabled: Boolean = false
) : Module

@Serializable
@ModuleInfo(
    "No Hit Delay",
    "Allows you to revert combat back to 1.7 and have no hit delay. " +
            "This is sometimes considered a cheat, use at your own risk!"
)
data class NoHitDelay(override val isEnabled: Boolean = false) : Module

@Serializable
@ModuleInfo(
    "Infinite Emotes",
    "Allows you to always use emotes, even when walking"
)
data class InfiniteEmotes(override val isEnabled: Boolean = false) : Module

@Serializable
@ModuleInfo(
    "Set Metadata URL",
    "Sets the Metadata URL (don't use if you don't know what this means)"
)
data class MetadataURL(
    @OptionInfo(
        "Metadata URL",
        "The new URL"
    )
    val metadataURL: String = "https://api.lunarclientprod.com/game/metadata",
    override val isEnabled: Boolean = false
) : Module

@Serializable
data class Schema(val modules: Map<String, ModuleDefinition>)

@Serializable
data class ModuleDefinition(
    val options: Map<String, OptionDefinition>,
    val displayName: String,
    val description: String
)

@Serializable
data class OptionDefinition(val type: OptionType, val displayName: String, val description: String)

@Serializable
enum class OptionType(val clazz: KClass<*>) {
    STRING(String::class), BOOLEAN(Boolean::class), INTEGER(Int::class), FLOAT(Float::class)
}

fun schemaOfModules() = Schema(serializedPropertiesOf<Modules>()
    .associate { moduleProp ->
        val moduleType = moduleProp.returnType.jvmErasure
        val info = moduleType.findAnnotation() ?: ModuleInfo(moduleType.simpleName ?: "Unnamed module")
        moduleProp.name to ModuleDefinition(moduleType.serializedProperties
            .filterNot { it.name == "isEnabled" }
            .associate { prop ->
                val optionType = (enumValues<OptionType>().find { it.clazz == prop.returnType.jvmErasure }
                    ?: error("No option type for parameter $prop for module $moduleType"))

                val optionInfo = prop.findAnnotation() ?: OptionInfo(prop.name)

                prop.name to OptionDefinition(
                    optionType,
                    displayName = optionInfo.displayName,
                    description = optionInfo.description
                )
            },
            displayName = info.displayName,
            description = info.description
        )
    }
)

@Serializable
data class CustomCommands(
    // name to execute
    val commands: Map<String, String> = emptyMap(),
    // name to script path (absolute)
    val scripts: Map<String, String> = emptyMap()
) {
    @Transient
    var loadedScripts = loadScripts()

    private fun loadScripts() = if (isGraalLoaded) scripts.mapValues { (_, f) ->
        runCatching {
            val ctx = File(f).readText().asGraalContext()
            return@runCatching { args: List<String> ->
                runCatching { ctx.runCommand(args) }
                    .onFailure { println("Failed to execute command from script $f:") ; it.printStackTrace() }
            }
        }.onFailure { println("Failed to load command script $f"); it.printStackTrace() }.getOrNull()
    } else emptyMap()

    fun reloadScripts() {
        loadedScripts = loadScripts()
    }
}

@Target(AnnotationTarget.CLASS)
@Retention
annotation class ModuleInfo(val displayName: String, val description: String = "No description was given.")

@Target(AnnotationTarget.PROPERTY)
@Retention
annotation class OptionInfo(val displayName: String, val description: String = "No description was given.")