//
//  RNBluetoothManager.h
//  BluetoothEscposPrinterFork
//
//  Created by Wiyanto Tan on 08/12/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#ifndef RNBluetoothManager_h
#define RNBluetoothManager_h

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <CoreBluetooth/CoreBluetooth.h>

@protocol WriteDataToBleDelegate <NSObject>
@required
- (void) didWriteDataToBle: (BOOL)success;
@end

@interface RNBluetoothManager : RCTEventEmitter <RCTBridgeModule,CBCentralManagerDelegate,CBPeripheralDelegate>
@property (strong, nonatomic) CBCentralManager      *centralManager;
@property (nonatomic,copy) RCTPromiseResolveBlock scanResolveBlock;
@property (nonatomic,copy) RCTPromiseRejectBlock scanRejectBlock;
@property (strong,nonatomic) NSMutableDictionary <NSString *,CBPeripheral *> *foundDevices;
@property (strong,nonatomic) NSString *waitingConnect;
@property (nonatomic,copy) RCTPromiseResolveBlock connectResolveBlock;
@property (nonatomic,copy) RCTPromiseRejectBlock connectRejectBlock;
+(void)writeValue:(NSData *) data withDelegate:(NSObject<WriteDataToBleDelegate> *) delegate;
+(Boolean)isConnected;
-(void)initSupportServices;
-(void)callStop;
@end

#endif /* RNBluetoothManager_h */
