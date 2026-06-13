export type ExpoAdbAvailabilityDetails = {
  available: boolean;
  host: string;
  port: number;
  keyPairPresent: boolean;
  errorCode: string | null;
  errorClass: string | null;
  errorMessage: string | null;
  hint: string | null;
};

export type ExpoAdbModuleType = {
  isAvailable(): Promise<boolean>;
  getAvailabilityDetails(): Promise<ExpoAdbAvailabilityDetails>;
  executeCommand(command: string): Promise<string>;
  executeCommands(commands: string[]): Promise<string>;
};
