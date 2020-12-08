//
//  PrintColumnBleWriteDelegate.h
//  BluetoothEscposPrinterFork
//
//  Created by Wiyanto Tan on 08/12/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#ifndef PrintColumnBleWriteDelegate_h
#define PrintColumnBleWriteDelegate_h

#import <React/RCTBridgeModule.h>
#import "RNBluetoothManager.h"
#import "RNBluetoothEscposPrinter.h"

@interface PrintColumnBleWriteDelegate:NSObject<WriteDataToBleDelegate>
@property NSInteger now;
@property Boolean error;
@property RCTPromiseResolveBlock pendingResolve;
@property RCTPromiseRejectBlock pendingReject;
@property RNBluetoothEscposPrinter *printer;
@property Boolean canceled;
@property NSString *encodig;
@property NSInteger codePage;
@property NSInteger widthTimes;
@property NSInteger heightTimes;
@property NSInteger fontType;
-(void)printColumn:(NSMutableArray<NSMutableString *> *) columnsToPrint withMaxcount:(NSInteger)maxcount;
@end

#endif /* PrintColumnBleWriteDelegate_h */
