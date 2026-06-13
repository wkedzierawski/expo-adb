import { NativeModule, requireNativeModule } from 'expo';

declare class ExpoAdbModule extends NativeModule<{}> {
  PI: number;
  hello(): string;
  setValueAsync(value: string): Promise<void>;
}

export default requireNativeModule<ExpoAdbModule>('ExpoAdb');
