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

package com.wearablesensor.aura.data_repository;

import com.wearablesensor.aura.data_repository.models.RRIntervalModel;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by lecoucl on 29/03/17.
 */

public interface LocalDataRepository {

    ArrayList<RRIntervalModel> queryRRSample(Date iStartDate, Date iEndDate) throws Exception;
    void saveRRSample(final RRIntervalModel iSampleRR) throws Exception;

    void clear();
}
