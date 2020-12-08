//
//  RNBluetoothTscPrinter.h
//  BluetoothEscposPrinterFork
//
//  Created by Wiyanto Tan on 08/12/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#ifndef RNBluetoothTscPrinter_h
#define RNBluetoothTscPrinter_h

#import <React/RCTBridgeModule.h>
#import "RNBluetoothManager.h"
@interface RNBluetoothTscPrinter : NSObject <RCTBridgeModule,WriteDataToBleDelegate>

@end

#endif /* RNBluetoothTscPrinter_h */
