//
//  BLEManager.h
//  imble3app
//
//  Created by imac2015 on 2025/02/01.
//

#ifndef BLEManager_h
#define BLEManager_h

#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>

@interface BLEManager : NSObject <CBCentralManagerDelegate, CBPeripheralDelegate>

@property (strong, nonatomic) CBCentralManager *centralManager;
@property (strong, nonatomic) CBCharacteristic *readCharacteristic;
@property (strong, nonatomic) CBCharacteristic *writeCharacteristic;

@property (nonatomic, strong) NSMutableDictionary<NSString *, CBPeripheral *> *peripherals;
@property (strong, nonatomic) CBPeripheral *connectedPeripheral;
@property (copy, nonatomic) void (^scanCallback)(CBPeripheral *peripheral);
@end


#endif /* BLEManager_h */
