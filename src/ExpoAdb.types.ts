export type ExpoAdbModuleType = {
  isAvailable(): Promise<boolean>;
  executeCommand(command: string): Promise<string>;
  executeCommands(commands: string[]): Promise<string>;
};
