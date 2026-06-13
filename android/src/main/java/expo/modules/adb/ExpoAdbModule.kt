package expo.modules.adb

import android.content.Context
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class ExpoAdbModule : Module() {
  private val context: Context
    get() = requireNotNull(appContext.reactContext) {
      "React context is not available"
    }

  override fun definition() = ModuleDefinition {
    Name("ExpoAdb")

    AsyncFunction("isAvailable") {
      AdbShellClient.isAvailable(context)
    }

    AsyncFunction("getAvailabilityDetails") {
      AdbShellClient.getAvailabilityDetails(context).toMap()
    }

    AsyncFunction("executeCommand") { command: String ->
      AdbShellClient.executeCommand(context, command)
    }

    AsyncFunction("executeCommands") { commands: List<String> ->
      AdbShellClient.executeCommands(context, commands)
    }
  }
}
