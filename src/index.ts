// Reexport the native module. On web, it will be resolved to ExpoAdbModule.web.ts
// and on native platforms to ExpoAdbModule.ts
export { default } from './ExpoAdbModule';
export * from './ExpoAdb.types';
