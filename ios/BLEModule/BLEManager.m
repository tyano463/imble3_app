//
//  BLEManager.m
//  imble3app
//
//  Created by imac2015 on 2025/02/01.
//

#import "BLEManager.h"

@implementation BLEManager

// サービスUUID、Read/Write CharacteristicのUUIDを定義
static NSString * const kServiceUUID = @"12345678-1234-1234-1234-123456789abc";
static NSString * const kReadCharacteristicUUID = @"abcdef12-1234-1234-1234-123456789abc";
static NSString * const kWriteCharacteristicUUID = @"abcdef12-5678-5678-5678-123456789abc";

+ (instancetype)sharedInstance {
    static BLEManager *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[BLEManager alloc] init];
    });
    return sharedInstance;
}

// 初期化
- (instancetype)init {
    self = [super init];
    if (self) {
        self.centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
        self.peripherals = [NSMutableDictionary dictionary];
    }
    return self;
}

// BLEスキャン開始
- (void)startScanWithCallback:(void (^)(CBPeripheral *peripheral))callback {
    self.scanCallback = callback;  // コールバックを保存
    [self.centralManager scanForPeripheralsWithServices:@[[CBUUID UUIDWithString:kServiceUUID]] options:nil];
    NSLog(@"Started scanning for peripherals with service UUID: %@", kServiceUUID);
}

// CBCentralManagerDelegate メソッド - スキャン結果
- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary *)advertisementData RSSI:(NSNumber *)RSSI {
    NSLog(@"Discovered peripheral: %@", peripheral.name);
    
    // コールバックが登録されていれば呼び出す
    if (self.scanCallback) {
        self.scanCallback(peripheral);
    }
    
    // 一度見つかったらスキャンを停止
    [self.centralManager stopScan];
}

// 接続処理
- (void)connectPeripheral:(CBPeripheral *)peripheral {
    self.connectedPeripheral = peripheral;
    peripheral.delegate = self;
    [self.centralManager connectPeripheral:peripheral options:nil];
    NSLog(@"Connecting to peripheral: %@", peripheral.name);
}

// CBCentralManagerDelegate メソッド - 接続成功
- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral {
    NSLog(@"Connected to peripheral: %@", peripheral.name);
    [peripheral discoverServices:@[[CBUUID UUIDWithString:kServiceUUID]]];
}

// サービス発見後、Read/Write Characteristic の発見
- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error {
    if (error) {
        NSLog(@"Error discovering services: %@", error.localizedDescription);
        return;
    }

    for (CBService *service in peripheral.services) {
        if ([service.UUID isEqual:[CBUUID UUIDWithString:kServiceUUID]]) {
            [peripheral discoverCharacteristics:@[[CBUUID UUIDWithString:kReadCharacteristicUUID], [CBUUID UUIDWithString:kWriteCharacteristicUUID]] forService:service];
        }
    }
}

// Read/Write Characteristic 発見後の処理
- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error {
    if (error) {
        NSLog(@"Error discovering characteristics: %@", error.localizedDescription);
        return;
    }

    for (CBCharacteristic *characteristic in service.characteristics) {
        if ([characteristic.UUID isEqual:[CBUUID UUIDWithString:kReadCharacteristicUUID]]) {
            self.readCharacteristic = characteristic;
            [peripheral setNotifyValue:YES forCharacteristic:characteristic]; // 通知の有効化
        }
        if ([characteristic.UUID isEqual:[CBUUID UUIDWithString:kWriteCharacteristicUUID]]) {
            self.writeCharacteristic = characteristic;
        }
    }
}

// Read Characteristic からのデータ受信
- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error {
    if (error) {
        NSLog(@"Error updating characteristic value: %@", error.localizedDescription);
        return;
    }

    if ([characteristic.UUID isEqual:[CBUUID UUIDWithString:kReadCharacteristicUUID]]) {
        NSData *data = characteristic.value;
        NSString *dataString = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        NSLog(@"Received data: %@", dataString);
        
        // 受信したデータをコールバックで返す処理を追加可能
    }
}

// 通知の有効化
- (void)enableNotifications {
    [self.connectedPeripheral setNotifyValue:YES forCharacteristic:self.readCharacteristic];
    NSLog(@"Enabled notifications for read characteristic.");
}

// Write Characteristic へのデータ書き込み
- (void)writeData:(NSData *)data {
    if (self.writeCharacteristic) {
        [self.connectedPeripheral writeValue:data forCharacteristic:self.writeCharacteristic type:CBCharacteristicWriteWithResponse];
        NSLog(@"Written data to write characteristic: %@", data);
    }
}

@end
