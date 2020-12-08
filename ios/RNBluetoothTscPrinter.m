//
//  RNBluetoothTscPrinter.m
//  BluetoothEscposPrinterFork
//
//  Created by Wiyanto Tan on 08/12/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RNBluetoothTscPrinter.h"
#import "RNTscCommand.h"
#import "RNBluetoothManager.h"

@implementation RNBluetoothTscPrinter

NSData *toPrint;
RCTPromiseRejectBlock _pendingReject;
RCTPromiseResolveBlock _pendingResolve;
NSInteger now;

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

/**
 * Exports the constants to javascritp.
 **/
- (NSDictionary *)constantsToExport
{
    NSDictionary *direction = @{@"FORWARD": @0, @"BACKWARD": @1};
    NSDictionary *density = @{@"DNESITY0": @0,@"DNESITY1": @1,@"DNESITY2": @2,@"DNESITY3": @23,@"DNESITY4": @4,@"DNESITY5": @5,@"DNESITY6": @6,@"DNESITY7": @7,@"DNESITY8": @8,@"DNESITY9": @9,@"DNESITY10": @10,@"DNESITY11": @11,@"DNESITY12": @12,@"DNESITY13": @13,@"DNESITY14": @14,@"DNESITY15": @15};
    NSDictionary *barcodeType = @{@"CODE128": @"128", @"CODE128M": @"128M", @"EAN128": @"EAN128", @"ITF25": @"25", @"ITF25C": @"25C", @"CODE39": @"39", @"CODE39C": @"39C", @"CODE39S": @"39S", @"CODE93": @"93", @"EAN13": @"EAN13", @"EAN13_2": @"EAN13+2", @"EAN13_5": @"EAN13+5", @"EAN8": @"EAN8", @"EAN8_2": @"EAN8+2", @"EAN8_5": @"EAN8+5", @"CODABAR": @"CODA", @"POST": @"POST", @"UPCA": @"EAN13", @"UPCA_2": @"EAN13+2", @"UPCA_5": @"EAN13+5", @"UPCE": @"EAN13", @"UPCE_2": @"EAN13+2", @"UPCE_5": @"EAN13+5", @"CPOST": @"CPOST", @"MSI": @"MSI", @"MSIC": @"MSIC", @"PLESSEY": @"PLESSEY", @"ITF14": @"ITF14", @"EAN14": @"EAN14"};
    NSDictionary *fontType = @{@"FONT_1": @"1", @"FONT_2": @"2", @"FONT_3": @"3", @"FONT_4": @"4", @"FONT_5": @"5", @"FONT_6": @"6", @"FONT_7": @"7", @"FONT_8": @"8", @"SIMPLIFIED_CHINESE": @"TSS24.BF2", @"TRADITIONAL_CHINESE": @"TST24.BF2", @"KOREAN": @"K"};
    NSDictionary *ecc = @{@"LEVEL_L": @"L", @"LEVEL_M": @"M", @"LEVEL_Q": @"Q", @"LEVEL_H": @"H"};
    NSDictionary *rotation = @{@"ROTATION_0": @0, @"ROTATION_90": @90, @"ROTATION_180": @180, @"ROTATION_270": @270};
    NSDictionary *fontMul = @{@"MUL_1": @1, @"MUL_2": @2, @"MUL_3": @3, @"MUL_4": @4, @"MUL_5": @5, @"MUL_6": @6, @"MUL_7": @7, @"MUL_8": @8, @"MUL_9": @9, @"MUL_10": @10};
    NSDictionary *bitmapMode = @{@"OVERWRITE": @0, @"OR": @1, @"XOR": @2};
    NSDictionary *printSpeed = @{@"SPEED1DIV5":@1, @"SPEED2":@2, @"SPEED3":@3, @"SPEED4":@4};
    NSDictionary *tear = @{@"ON":@"ON",@"OFF":@"OFF"};
    NSDictionary *readable = @{@"DISABLE": @0, @"EANBLE": @1};
    
    return @{ @"DIRECTION":direction,
              @"DENSITY":density,
              @"BARCODETYPE":barcodeType,
              @"FONTTYPE":fontType,
              @"EEC":ecc,
              @"ROTATION":rotation,
              @"FONTMUL":fontMul,
              @"BITMAP_MODE":bitmapMode,
              @"PRINT_SPEED":printSpeed,
              @"TEAR":tear,
              @"READABLE":readable
    };
}

RCT_EXPORT_MODULE(BluetoothTscPrinter);
//printLabel(final ReadableMap options, final Promise promise)
RCT_EXPORT_METHOD(printLabel:(NSDictionary *) options withResolve:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    NSInteger width = [[options valueForKey:@"width"] integerValue];
    NSInteger height = [[options valueForKey:@"height"] integerValue];
    NSInteger gap = [[options valueForKey:@"gap"] integerValue];
    NSInteger home = [[options valueForKey:@"home"] integerValue];
    NSString *tear = [options valueForKey:@"tear"];
    if(!tear || ![@"ON" isEqualToString:tear]) tear = @"OFF";
    NSArray *texts = [options objectForKey:@"text"];
    NSArray *qrCodes = [options objectForKey:@"qrcode"];
    NSArray *barCodes = [options objectForKey:@"barcode"];
    NSArray *images = [options objectForKey:@"image"];
    NSArray *reverses = [options objectForKey:@"revers"];
    NSInteger direction = [[options valueForKey:@"direction"] integerValue];
    NSInteger density = [[options valueForKey:@"density"] integerValue];
    NSArray* reference = [options objectForKey:@"reference"];
    NSInteger sound = [[options valueForKey:@"sound"] integerValue];
    NSInteger speed = [[options valueForKey:@"speed"] integerValue];
    RNTscCommand *tsc = [[RNTscCommand alloc] init];
    if(speed){
        [tsc addSpeed:[tsc findSpeedValue:speed]];
    }
    if(density){
        [tsc addDensity:density];
    }
    [tsc addSize:width height:height];
    [tsc addGap:gap];
    [tsc addDirection:direction];
    if(reference && [reference count] ==2){
        NSInteger x = [[reference objectAtIndex:0] integerValue];
        NSInteger y = [[reference objectAtIndex:1] integerValue];
        NSLog(@"refernce  %ld y:%ld ",x,y);
        [tsc addReference:x y:y];
    }else{
        [tsc addReference:0 y:0];
    }
    [tsc addTear:tear];
    if(home && home == 1){
      [tsc addBackFeed:16];
      [tsc addHome];
    }
    [tsc addCls];

    //Add Texts
    for(int i=0; texts && i<[texts count];i++){
        NSDictionary * text = [texts objectAtIndex:i];
        NSString *t = [text valueForKey:@"text"];
        NSInteger x = [[text valueForKey:@"x"] integerValue];
        NSInteger y = [[text valueForKey:@"y"] integerValue];
        NSString *fontType = [text valueForKey:@"fonttype"];
        NSInteger rotation = [[text valueForKey:@"rotation"] integerValue];
        NSInteger xscal = [[text valueForKey:@"xscal"] integerValue];
        NSInteger yscal = [[text valueForKey:@"yscal"] integerValue];
        Boolean bold = [[text valueForKey:@"bold"] boolValue];

        [tsc addText:x y:y fontType:fontType rotation:rotation xscal:xscal yscal:yscal text:t];
        if(bold){
            [tsc addText:x+1 y:y fontType:fontType
                rotation:rotation xscal:xscal yscal:yscal  text:t];
            [tsc addText:x y:y+1 fontType:fontType
                rotation:rotation xscal:xscal yscal:yscal  text:t];
        }
    }

  //images
        for (int i = 0; images && i < [images count]; i++) {
            NSDictionary *img = [images objectAtIndex:i];
            NSInteger x = [[img valueForKey:@"x"] integerValue];
            NSInteger y = [[img valueForKey:@"y"] integerValue];
            NSInteger imgWidth = [[img valueForKey:@"width"] integerValue];
            NSInteger mode = [[img valueForKey:@"mode"] integerValue];
            NSString *image  = [img valueForKey:@"image"];
            NSData *imageData = [[NSData alloc] initWithBase64EncodedString:image options:0];
            UIImage *uiImage = [[UIImage alloc] initWithData:imageData];
            [tsc addBitmap:x y:y bitmapMode:mode width:imgWidth bitmap:uiImage];
        }

    //QRCode
    for (int i = 0; qrCodes && i < [qrCodes count]; i++) {
        NSDictionary *qr = [qrCodes objectAtIndex:i];
        NSInteger x = [[qr valueForKey:@"x"] integerValue];
        NSInteger y = [[qr valueForKey:@"y"] integerValue];
        NSInteger qrWidth = [[qr valueForKey:@"width"] integerValue];
        NSString *level = [qr valueForKey:@"level"];
        if(!level)level = @"M";
        NSInteger rotation = [[qr valueForKey:@"rotation"] integerValue];
        NSString *code = [qr valueForKey:@"code"];
        [tsc addQRCode:x y:y errorCorrectionLevel:level width:qrWidth rotation:rotation code:code];
    }

    //BarCode
   for (int i = 0; barCodes && i < [barCodes count]; i++) {
       NSDictionary *bar = [barCodes objectAtIndex:i];
       NSInteger x = [[bar valueForKey:@"x"] integerValue];
       NSInteger y = [[bar valueForKey:@"y"] integerValue];
       NSInteger barWide =[[bar valueForKey:@"wide"] integerValue];
       if(!barWide) barWide = 2;
       NSInteger barHeight = [[bar valueForKey:@"height"] integerValue];
       NSInteger narrow = [[bar valueForKey:@"narrow"] integerValue];
       if(!narrow) narrow = 2;
       NSInteger rotation = [[bar valueForKey:@"rotation"] integerValue];
       NSString *code = [bar valueForKey:@"code"];
       NSString *type = [bar valueForKey:@"type"];
       NSInteger readable = [[bar valueForKey:@"readable"] integerValue];
       [tsc add1DBarcode:x y:y barcodeType:type height:barHeight wide:barWide narrow:narrow readable:readable rotation:rotation content:code];
    }
    for(int i=0; reverses&& i < [reverses count]; i++){
        NSDictionary *area = [reverses objectAtIndex:i];
        NSInteger ax = [[area valueForKey:@"x"] integerValue];
        NSInteger ay = [[area valueForKey:@"y"] integerValue];
        NSInteger aWidth = [[area valueForKey:@"width"] integerValue];
        NSInteger aHeight = [[area valueForKey:@"height"] integerValue];
        [tsc addReverse:ax y:ay xwidth:aWidth yheigth:aHeight];
    }
    [tsc addPrint:1 n:1];
    if (sound) {
        [tsc addSound:2 interval:100];
    }
    _pendingReject = reject;
    _pendingResolve = resolve;
    toPrint = tsc.command;
    now = 0;
    [RNBluetoothManager writeValue:toPrint withDelegate:self];
}

- (void) didWriteDataToBle: (BOOL)success{
    if(success){
        if(_pendingResolve){
            _pendingResolve(nil);
        }
    }else if(_pendingReject){
        _pendingReject(@"PRINT_ERROR",@"PRINT_ERROR",nil);
    }
}

@end
