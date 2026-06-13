import { requireNativeModule } from 'expo';

declare class ExpoAdbModule {
  isAvailable(): Promise<boolean>;
  getAvailabilityDetails(): Promise<import('./ExpoAdb.types').ExpoAdbAvailabilityDetails>;
  executeCommand(command: string): Promise<string>;
  executeCommands(commands: string[]): Promise<string>;
}

export default requireNativeModule<ExpoAdbModule>('ExpoAdb');
