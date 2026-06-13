import ExpoAdb from 'expo-adb';
import { useState } from 'react';
import { Button, SafeAreaView, ScrollView, Text, View } from 'react-native';

export default function App() {
  const [output, setOutput] = useState('Tap a button to talk to local ADB.');

  async function run(action: () => Promise<string>) {
    try {
      const nextOutput = await action();
      setOutput(nextOutput.trim() || '(empty output)');
    } catch (error) {
      setOutput(error instanceof Error ? error.message : String(error));
    }
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.container}>
        <Text style={styles.header}>expo-adb example</Text>
        <Group name="Availability">
          <Button
            title="Check local ADB"
            onPress={() =>
              run(async () => {
                const available = await ExpoAdb.isAvailable();
                return available ? 'Local ADB is reachable.' : 'Local ADB is not reachable.';
              })
            }
          />
        </Group>
        <Group name="Single command">
          <Button
            title="Run getprop ro.product.model"
            onPress={() => run(() => ExpoAdb.executeCommand('getprop ro.product.model'))}
          />
        </Group>
        <Group name="Multi command script">
          <Button
            title="Run two commands"
            onPress={() =>
              run(() =>
                ExpoAdb.executeCommands([
                  'getprop ro.product.brand',
                  'getprop ro.build.version.release',
                ])
              )
            }
          />
        </Group>
        <Group name="Output">
          <Text selectable>{output}</Text>
        </Group>
      </ScrollView>
    </SafeAreaView>
  );
}

function Group(props: { name: string; children: React.ReactNode }) {
  return (
    <View style={styles.group}>
      <Text style={styles.groupHeader}>{props.name}</Text>
      {props.children}
    </View>
  );
}

const styles = {
  header: { fontSize: 30, margin: 20 },
  groupHeader: { fontSize: 20, marginBottom: 20 },
  group: { margin: 20, backgroundColor: '#fff', borderRadius: 10, padding: 20 },
  container: { flex: 1, backgroundColor: '#eee' },
  view: { flex: 1, height: 200 },
};
