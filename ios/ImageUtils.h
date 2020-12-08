//
//  ImageUtils.h
//  BluetoothEscposPrinterFork
//
//  Created by Wiyanto Tan on 08/12/20.
//  Copyright © 2020 Facebook. All rights reserved.
//

#ifndef ImageUtils_h
#define ImageUtils_h

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
@interface ImageUtils :NSObject
+ (UIImage*)imagePadLeft:(NSInteger) left withSource: (UIImage*)source;
+ (uint8_t *)imageToGreyImage:(UIImage *)image;
+ (UIImage *)imageWithImage:(UIImage *)image scaledToFillSize:(CGSize)size;
+ (NSData*)bitmapToArray:(UIImage*) bmp;
+ (NSData *)eachLinePixToCmd:(unsigned char *)src nWidth:(NSInteger) nWidth nHeight:(NSInteger) nHeight nMode:(NSInteger) nMode;
+(unsigned char *)format_K_threshold:(unsigned char *) orgpixels
                               width:(NSInteger) xsize height:(NSInteger) ysize;
+(NSData *)pixToTscCmd:(uint8_t *)src width:(NSInteger) width;
@end

#endif /* ImageUtils_h */
