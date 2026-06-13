import { requireNativeModule } from 'expo';

declare class ExpoAdbModule {
  isAvailable(): Promise<boolean>;
  executeCommand(command: string): Promise<string>;
  executeCommands(commands: string[]): Promise<string>;
}

export default requireNativeModule<ExpoAdbModule>('ExpoAdb');
