import type { ExpoAdbModuleType } from './ExpoAdb.types';

const UNSUPPORTED_MESSAGE =
  'expo-adb is available only on Android devices with a local ADB daemon exposed on 127.0.0.1:5555.';

class ExpoAdbIosModule implements ExpoAdbModuleType {
  async isAvailable(): Promise<boolean> {
    return false;
  }

  async executeCommand(): Promise<string> {
    throw new Error(UNSUPPORTED_MESSAGE);
  }

  async executeCommands(): Promise<string> {
    throw new Error(UNSUPPORTED_MESSAGE);
  }
}

export default new ExpoAdbIosModule();
