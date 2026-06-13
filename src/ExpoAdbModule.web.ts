import { registerWebModule, NativeModule } from 'expo';

// ExpoAdbModule is not available on the web platform.
class ExpoAdbModule extends NativeModule<{}> {}

export default registerWebModule(ExpoAdbModule, 'ExpoAdbModule');
