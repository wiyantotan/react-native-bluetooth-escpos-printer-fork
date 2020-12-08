//
//  RNBluetoothEscposPrinter.h
//  BluetoothEscposPrinterFork
//
//  Created by Wiyanto Tan on 08/12/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#ifndef RNBluetoothEscposPrinter_h
#define RNBluetoothEscposPrinter_h

#import <React/RCTBridgeModule.h>
#import "RNBluetoothManager.h";

@interface RNBluetoothEscposPrinter : NSObject <RCTBridgeModule,WriteDataToBleDelegate>

@property (nonatomic,assign) NSInteger deviceWidth;
-(void) textPrint:(NSString *) text
       inEncoding:(NSString *) encoding
     withCodePage:(NSInteger) codePage
       widthTimes:(NSInteger) widthTimes
      heightTimes:(NSInteger) heightTimes
         fontType:(NSInteger) fontType
         delegate:(NSObject<WriteDataToBleDelegate> *) delegate;
@end

#endif /* RNBluetoothEscposPrinter_h */
