/*
Aura Mobile Application
Copyright (C) 2017 Aura Healthcare

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/
*/

package com.wearablesensor.aura.device_pairing;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.UiThread;
import android.util.Log;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.utils.Uuids;
import com.wearablesensor.aura.DataCollectorService;
import com.wearablesensor.aura.DataCollectorServiceConstants;
import com.wearablesensor.aura.data_repository.DateIso8601Mapper;
import com.wearablesensor.aura.data_repository.models.ElectroDermalActivityModel;
import com.wearablesensor.aura.data_repository.models.MotionAccelerometerModel;
import com.wearablesensor.aura.data_repository.models.MotionGyroscopeModel;
import com.wearablesensor.aura.data_repository.models.PhysioSignalModel;
import com.wearablesensor.aura.data_repository.models.RRIntervalModel;
import com.wearablesensor.aura.data_repository.models.SkinTemperatureModel;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.reader.GattCustomGSRTemperatureCharacteristicReader;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.reader.GattHeartRateCharacteristicReader;
import com.wearablesensor.aura.device_pairing.bluetooth.gatt.reader.GattMovementCharacteristicReader;
import com.wearablesensor.aura.device_pairing.data_model.PhysioEvent;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingBatteryLevelNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingConnectedNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingDisconnectedNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingReceivedDataNotification;
import com.wearablesensor.aura.device_pairing.notifications.DevicePairingStatus;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.UUID;


public class BluetoothDevicePairingService extends DevicePairingService{
    private final String TAG = this.getClass().getSimpleName();


    private Handler handler = new Handler();
    private Runnable sendDataOnTic;

    // a enum to describe action types that can be applied to a specific GATT characteristic
    public enum StateListenerAction{
        READ,
        WRITE,
        ENABLE_NOTIFICATION
    }

    /**
     * @class an inner class used to described actions to realise on GATT channel (read/write/notification) and
     * associated callbacks
     *
     */
    class StateListenerConfig
    {
        private UUID mGattService; // selected GATT service
        private UUID mGattCharacteristic; // selected GATT characteristic
        private BleDevice.ReadWriteListener mReadWriteListener; // action callback
        private StateListenerAction mAction; // action type

        StateListenerConfig(UUID iGattService, UUID iGattCharacteristic, BleDevice.ReadWriteListener  iReadWriteListener, StateListenerAction iAction) {
            mGattService = iGattService;
            mGattCharacteristic = iGattCharacteristic;
            mReadWriteListener = iReadWriteListener;
            mAction = iAction;
        }

        public UUID getGattService(){
            return mGattService;
        }

        public UUID getGattCharacteristic(){
            return mGattCharacteristic;
        }

        public BleDevice.ReadWriteListener getReadWriteListener(){
            return mReadWriteListener;
        }

        public StateListenerAction getAction(){
            return mAction;
        }
    }

    public BluetoothDevicePairingService(Context iContext){
        super(iContext);
        // callback used to handle standard Heart rate profile
        EventManager eventManager = new EventManager() {
            @Override
            public void processEvent(PhysioEvent event) {

                Calendar c = Calendar.getInstance();
                String lCurrentTimestamp = DateIso8601Mapper.getString(c.getTime());
                String lDeviceAddress = event.getMacAddress();

                GattHeartRateCharacteristicReader heartRateCharacteristicReader = new GattHeartRateCharacteristicReader();
                if(heartRateCharacteristicReader.read(event)){
                    RRIntervalModel lRrIntervalModel = new RRIntervalModel(event.getMacAddress(), lCurrentTimestamp, heartRateCharacteristicReader.getRrInterval());
                    Log.d(TAG, lRrIntervalModel.getTimestamp() + " " + lRrIntervalModel.getUuid() + " " + lRrIntervalModel.getRrInterval() + " " + lRrIntervalModel.getUser());
                    receiveData(lRrIntervalModel);
                }

                GattCustomGSRTemperatureCharacteristicReader temperatureCharacteristicReader = new GattCustomGSRTemperatureCharacteristicReader();
                if(temperatureCharacteristicReader.read(event)){
                    SkinTemperatureModel lSkinTemperatureModel = new SkinTemperatureModel(lDeviceAddress, lCurrentTimestamp, temperatureCharacteristicReader.getSkinTemperature());
                    ElectroDermalActivityModel lElectroDermalActivityModel = new ElectroDermalActivityModel(lDeviceAddress, lCurrentTimestamp, 7812, temperatureCharacteristicReader.getElectroDermalActivity());
                    Log.d(TAG, lSkinTemperatureModel.getTimestamp() + " " + lSkinTemperatureModel.getUuid() + " " + lSkinTemperatureModel.getTemperature() + " " + lSkinTemperatureModel.getUser());
                    Log.d(TAG, lElectroDermalActivityModel.getTimestamp() + " " + lElectroDermalActivityModel.getUuid() + " " + lElectroDermalActivityModel.getElectroDermalActivity() + " " + lElectroDermalActivityModel.getUser());
                    receiveData(lSkinTemperatureModel);
                    receiveData(lElectroDermalActivityModel);
                }

                GattMovementCharacteristicReader lGattMovementCharacteristic = new GattMovementCharacteristicReader();
                if(lGattMovementCharacteristic.read(event)){
                    MotionAccelerometerModel lAccelerometerModel = new MotionAccelerometerModel(lDeviceAddress, lCurrentTimestamp, lGattMovementCharacteristic.getAccelerometer(), "2G");
                    MotionGyroscopeModel lGyroscopeModel = new MotionGyroscopeModel(lDeviceAddress, lCurrentTimestamp, lGattMovementCharacteristic.getGyroscope());
                    receiveData(lAccelerometerModel);
                    receiveData(lGyroscopeModel);
                }
            }
        };
        stubDeviceDataFlow(eventManager);
    }

    @UiThread
    private void stubDeviceDataFlow(final EventManager eventManager){
        final StubEventManager manager = StubEventManager.getEventManager(StubEventManager.EventType.HEART_BEAT, mContext.getAssets());

        sendDataOnTic = new Runnable() {
            @Override
            public void run() {
                PhysioEvent event = manager.getEvent();
                eventManager.processEvent(event);
                handler.postDelayed(sendDataOnTic, manager.getFrequency());
            }
        };
        handler.postDelayed(sendDataOnTic, manager.getFrequency());
    }

    /**
     * @brief check if available bluetooth devices are compatibles with Aura prototype
     *
     * @param device available bluetooth device
     *
     * @return true if device is compatible, false otherwise
     */
    private boolean isCompatibleDevice(BleDevice device) {

        if(isHeartRateCompatibleDevice(device) || isGSRTemperatureCustomCompatibleDevice(device) || isMotionMovuinoCompatibleDevice(device)){
            return true;
        }

        return false;
    }

    /**
     * @brief check if available bluetooth devices are compatibles for heart rate data streaming with Aura prototype
     *
     * @param device available bluetooth device
     *
     * @return true if device is compatible, false otherwise
     */
    private boolean isHeartRateCompatibleDevice(BleDevice device) {

        String lDeviceName = device.getName_native();

        if(lDeviceName != null) {
            String lDeviceUpperName = lDeviceName.toUpperCase();

            if ((lDeviceUpperName.contains("RHYTHM") || lDeviceUpperName.contains("POLAR") || lDeviceUpperName.contains("MIO"))) {
                return true;
            }

            return false;
        }
        return false;
    }

    /**
     * @brief check if available bluetooth devices are compatibles for temperature and electro dermal activity
     * data streaming with Aura prototype
     *
     * @param device available bluetooth device
     *
     * @return true if device is compatible, false otherwise
     */
    private boolean isGSRTemperatureCustomCompatibleDevice(BleDevice device) {
        String lDeviceName = device.getName_native();

        if(lDeviceName != null) {
            String lDeviceUpperName = lDeviceName.toUpperCase();

            if( lDeviceUpperName.contains("MAXREFDES73")) {
                return true;
            }
            return false;
        }

        return false;
    }

    public boolean isMotionMovuinoCompatibleDevice(BleDevice device){
        String lDeviceName = device.getName_native();
        if(lDeviceName != null){
            String lDeviceUpperName = lDeviceName.toUpperCase();

            if( lDeviceUpperName.contains("MOVUINO")){
                return true;
            }
            return false;
        }

        return false;
    }
    /**
     * @brief start automatic pairing
     *
     * @param iContext application context
     */
    public void automaticPairing(final Context iContext){

        // stub
        mPaired = true;
        DevicePairingNotification iDevicePairingNotification = new DevicePairingConnectedNotification();
        EventBus.getDefault().post(iDevicePairingNotification);

        /*
        super.automaticPairing(iContext);

        mConnectedDevices.clear();
        mScanningHandler = new Handler();

        mScanningHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BleManager.get(iContext).stopScan();
                if(mConnectedDevices.size() < 1){
                    endPairing();
                }
            }
        }, SCAN_PERIOD);

        BleManager.get(iContext).startScan(mDiscoveryListener);
        */
    }

    public void startPairing(){
        // stub - super.startPairing();

        mPaired = true;
        mIsPairing = false;

        DevicePairingNotification iDevicePairingNotification = new DevicePairingNotification(DevicePairingStatus.CONNECTED);
        EventBus.getDefault().post(iDevicePairingNotification);
    }

    public void endPairing(){
        // stub - super.endPairing();

        mPaired = false;
        mIsPairing = false;

        Intent stopIntent = new Intent(mContext, DataCollectorService.class);
        stopIntent.setAction(DataCollectorServiceConstants.ACTION.STOPFOREGROUND_ACTION);
        mContext.startService(stopIntent);

        EventBus.getDefault().post(new DevicePairingDisconnectedNotification());

    }

    /**
     * @brief receive a physiological data sample and filter corrupted values
     *
     * @param iPhysioSignal input physiological data sample
     */
    private void receiveData(PhysioSignalModel iPhysioSignal){
        // filter corrupted cardiac R-R intervals
        if( iPhysioSignal.getType().equals(RRIntervalModel.RR_INTERVAL_TYPE) ){
            RRIntervalModel lRrIntervalModel = ((RRIntervalModel) iPhysioSignal);
            if(lRrIntervalModel.getRrInterval() == 0 || lRrIntervalModel.getTimestamp() == null
                    || lRrIntervalModel.getTimestamp() == ""){
                return;
            }
        }

        EventBus.getDefault().post(new DevicePairingReceivedDataNotification(iPhysioSignal));
    }

    /**
     * @brief receive Battery level update for a single device
     *
     * @param iDeviceInfo input updated device info
     */
    private void receiveBatteryLevel(DeviceInfo iDeviceInfo) {
        EventBus.getDefault().post(new DevicePairingBatteryLevelNotification(iDeviceInfo));
    }
    /**
     * @brief get connected devices though Bluetooth LE
     *
     * @return device info list
     */
    @Override
    public LinkedList<DeviceInfo> getDeviceList(){
        LinkedList<DeviceInfo> oDeviceList = new LinkedList<>();

        DeviceInfo device1 = new DeviceInfo("02:34:56:78:31", "D1");
        DeviceInfo device2 = new DeviceInfo("02:34:56:78:32", "D2");
        oDeviceList.add(device1);
        oDeviceList.add(device2);
        // stub
        /*for ( Map.Entry<String, BleDevice> lEntry : mConnectedDevices.entrySet() ) {
            oDeviceList.add(new DeviceInfo(lEntry.getValue().getMacAddress(), lEntry.getValue().getName_native()));
        }*/

        return oDeviceList;
    }

    /**
     * @brief close service in the application exit
     */
    @Override
    public void close(){
        BleManager.get(mContext).turnOff();
        handler.removeCallbacks(sendDataOnTic);
        super.close();
    }

}
