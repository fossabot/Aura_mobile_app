/**
 * @file DataSyncEndNotification
 * @author  clecoued <clement.lecouedic@aura.healthcare>
 * @version 1.0
 *
 * @section LICENSE
 *
 * Aura Mobile Application
 * Copyright (C) 2017 Aura Healthcare
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 *
 * @section DESCRIPTION
 * DataSyncStatus is an enum describing the data sync events
 *
 */
package com.wearablesensor.aura.data_sync.notifications;

public enum DataSyncStatus {
    START_SYNC,
    END_SYNC,
    UPDATE_SYNC_STATE,
    SIGNAL_STATE_LOW,
    SIGNAL_STATE_NONE,
    UNDEFINED
}
