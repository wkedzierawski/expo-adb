# expo-adb

<p style="color: red;"><strong>Important:</strong> To use this module on a device, Developer Options and ADB debugging must be enabled, and the device must expose a local ADB daemon on <code>127.0.0.1:5555</code> that accepts the app-generated ADB key.</p>

`expo-adb` is an Expo module that runs Android ADB shell commands through a local ADB daemon exposed on `127.0.0.1:5555`.

## What it does

- generates and stores an app-local ADB RSA keypair,
- connects to local `adbd` over TCP,
- opens an ADB shell session,
- executes one command or a small script.

## Requirements

- Android only (iOS / Web mocks)
- local ADB daemon reachable on `127.0.0.1:5555`
- device configured to accept the generated ADB key

If the device does not expose local ADB or rejects the key, calls will fail.

## Installation

```bash
yarn add expo-adb
npm install expo-adb
```

## API

```ts
isAvailable(): Promise<boolean>
getAvailabilityDetails(): Promise<{
  available: boolean
  host: string
  port: number
  keyPairPresent: boolean
  errorCode: string | null
  errorClass: string | null
  errorMessage: string | null
  hint: string | null
}>
executeCommand(command: string): Promise<string>
executeCommands(commands: string[]): Promise<string>
```

## Usage

```ts
import ExpoAdb from 'expo-adb';

const available = await ExpoAdb.isAvailable();
const details = await ExpoAdb.getAvailabilityDetails();

if (available) {
  const model = await ExpoAdb.executeCommand('getprop ro.product.model');

  const output = await ExpoAdb.executeCommands([
    'getprop ro.product.brand',
    'getprop ro.build.version.release',
  ]);
}
```

`executeCommands()` sends every command on a new line and appends `exit` automatically.

`executeCommand()` returns a cleaned command result for single-command use cases. It strips shell echo lines such as the input command itself and trailing `exit`.

## Behavior notes

- `executeCommand()` returns cleaned output for a single command.
- `executeCommands()` returns raw shell output for the whole script. It does not parse command success or failure for you.
- The RSA keypair is stored in the app sandbox under a directory named `expo-adb`.
- `isAvailable()` only checks transport/auth connectivity to local ADB.
- `getAvailabilityDetails()` returns diagnostic information when local ADB is unreachable.
- On iOS and web, `isAvailable()` returns `false` and command methods throw an error.

## Example commands

```ts
await ExpoAdb.executeCommand('getprop ro.product.model');
await ExpoAdb.executeCommand('pm list packages');
await ExpoAdb.executeCommands(['settings get global adb_enabled', 'getprop service.adb.tcp.port']);
```

## Limitations

- This is not a replacement for desktop `adb`.
- It only talks to a local daemon already available on the Android device.
- Some commands may still fail because of Android version, OEM policy, shell privileges, or ADB authorization state.

## Known issues

- On some devices, local ADB may not be reachable immediately even when Developer Options are already enabled.
- Observed on Samsung Galaxy S23: local ADB on `127.0.0.1:5555` initially returned `connection_refused`, then started working only after the Wireless debugging / pairing flow appeared in system UI.
- Because of that, `isAvailable()` and `getAvailabilityDetails()` can change from failing to working without any code change, depending on the current system ADB state.
