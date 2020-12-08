import { NativeModules } from 'react-native';

type BluetoothEscposPrinterForkType = {
  multiply(a: number, b: number): Promise<number>;
};

const { BluetoothEscposPrinterFork } = NativeModules;

export default BluetoothEscposPrinterFork as BluetoothEscposPrinterForkType;
