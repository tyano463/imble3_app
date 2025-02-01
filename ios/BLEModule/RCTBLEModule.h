//
//  RCTBLEModule.h
//  imble3app
//
//  Created by imac2015 on 2025/02/01.
//

#import <Foundation/Foundation.h>
#import <NativeBLESpec/NativeBLESpec.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTBridgeModule.h>

NS_ASSUME_NONNULL_BEGIN

@interface RCTBLEModule : NSObject<NativeBLESpec>
- (void) send_event: (NSString *)data;
@end

NS_ASSUME_NONNULL_END
