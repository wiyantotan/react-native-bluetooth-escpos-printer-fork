import { NativeModules } from 'react-native';

type BluetoothEscposPrinterType = {
  setWidth(width: number): void;
  printerInit(): Promise<any>;
  printerLeftSpace(sp: number): Promise<any>;
  printerUnderLine(sp: number): Promise<any>;
  printText(text: string, options: object): Promise<any>;
  rotate(rotate: number): Promise<any>;
  printerAlign(align: number): Promise<any>;
  printColumn(
    columnWidths: Array<number>,
    columnAligns: Array<number>,
    columnTexts: Array<string>,
    options: object
  ): Promise<any>;
  setBlob(sp: number): Promise<any>;
  printPic(base64encodeStr: string, options: object): Promise<any>;
  printQRCode(
    content: string,
    size: number,
    correctionLevel: number
  ): Promise<any>;
  printBarCode(
    str: string,
    type: number,
    nWidth: number,
    nHeight: number,
    nHriFontType: number,
    nHriFontPosition: number
  ): Promise<any>;
};

// BluetoothEscposPrinter.ERROR_CORRECTION = {
//   L:1,
//   M:0,
//   Q:3,
//   H:2
// };
//
// BluetoothEscposPrinter.BARCODETYPE={
//   UPC_A:65,//11<=n<=12
//   UPC_E:66,//11<=n<=12
//   JAN13:67,//12<=n<=12
//   JAN8:68,//7<=n<=8
//   CODE39:69,//1<=n<=255
//   ITF:70,//1<=n<=255(even numbers)
//   CODABAR:71,//1<=n<=255
//   CODE93:72,//1<=n<=255
//   CODE128:73//2<=n<=255
// };
// BluetoothEscposPrinter.ROTATION={
//   OFF:0,
//   ON:1
// };
// BluetoothEscposPrinter.ALIGN={
//   LEFT:0,
//   CENTER:1,
//   RIGHT:2
// };

const { BluetoothEscposPrinter } = NativeModules;

export default BluetoothEscposPrinter as BluetoothEscposPrinterType;
