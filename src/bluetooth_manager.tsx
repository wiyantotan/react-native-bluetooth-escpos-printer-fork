import { NativeModules } from 'react-native';

type BluetoothManagerType = {
  isBluetoothEnabled(): Promise<boolean>;
  enableBluetooth(): Promise<boolean>;
  disableBluetooth(): Promise<boolean>;
  scanDevices(): Promise<any>;
  stopScan(): Promise<any>;
  connect(address: string): Promise<any>;
};

const { BluetoothManager } = NativeModules;

export default BluetoothManager as BluetoothManagerType;
