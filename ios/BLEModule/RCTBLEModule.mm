//
//  RCTBLEModule.m
//  imble3app
//
//  Created by imac2015 on 2025/02/01.
//

#import "RCTBLEModule.h"
#import "BLEManager.h"

@interface RCTBLEModule()
@property (strong, nonatomic) NSString *lastConnected;
@property (strong, nonatomic) RCTEventEmitter *emitter;
@end

@implementation RCTBLEModule

static NSString *const EVENT_NAME = @"DEVICE_FOUND";

RCT_EXPORT_MODULE(NativeBLE)

- (instancetype)init {
    self = [super init];
    if (self) {
        _emitter = [RCTEventEmitter new];
    }
    return self;
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:(const facebook::react::ObjCTurboModule::InitParams &)params {
  return std::make_shared<facebook::react::NativeBLESpecJSI>(params);
}

- (void)start_scan {
    NSLog(@"start_scan");
}
- (void)stop_scan {
    NSLog(@"stop_scan");
}

- (void)connect : (NSString *)addr {
    NSLog(@"connect");
}
- (void)disconnect {
    NSLog(@"disconnect");
}

- (void)send_data : (NSString *) data {
    NSLog(@"send_data");
}

- (NSArray<NSString *> *)supportedEvents {
    return @[EVENT_NAME];
}

- (void)send_event : (NSString *)data {
    if (self.bridge) {
        NSDictionary *event = @{@"json": data };
        [_emitter sendEventWithName:EVENT_NAME body:event];
    } else {
        NSLog(@"Bridge is not initialized.");
    }
}

@end
