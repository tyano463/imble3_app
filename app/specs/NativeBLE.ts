import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  start_scan(): void
  stop_scan(): void
  connect(address: string): void
  disconnect(): void
  send_data(val: string): void
}

export default TurboModuleRegistry.getEnforcing<Spec>(
  'BLEModule',
);