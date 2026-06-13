import type { ExpoAdbModuleType } from './ExpoAdb.types';

const UNSUPPORTED_MESSAGE =
  'expo-adb is available only on Android devices with a local ADB daemon exposed on 127.0.0.1:5555.';

class ExpoAdbWebModule implements ExpoAdbModuleType {
  async isAvailable(): Promise<boolean> {
    return false;
  }

  async getAvailabilityDetails() {
    return {
      available: false,
      host: '127.0.0.1',
      port: 5555,
      keyPairPresent: false,
      errorCode: 'unsupported_platform',
      errorClass: null,
      errorMessage: UNSUPPORTED_MESSAGE,
      hint: 'Use this module only on Android devices that expose local ADB on 127.0.0.1:5555.',
    };
  }

  async executeCommand(): Promise<string> {
    throw new Error(UNSUPPORTED_MESSAGE);
  }

  async executeCommands(): Promise<string> {
    throw new Error(UNSUPPORTED_MESSAGE);
  }
}

export default new ExpoAdbWebModule();
