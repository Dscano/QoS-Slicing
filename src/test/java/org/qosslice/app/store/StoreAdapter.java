/*
 * Copyright 2017-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qosslice.app.store;

import com.google.common.collect.Maps;
import org.qosslice.app.api.QosSliceData;
import org.qosslice.app.api.QosSliceStore;
import org.onosproject.store.StoreDelegate;

import java.util.Collection;
import java.util.Map;

/**
 * Test adapter for Slice store.
 */
public class StoreAdapter implements QosSliceStore {
    protected Map<String, QosSliceData> sliceDataMap;
    protected StoreDelegate<QosSliceStoreEvent> delegate;

    public StoreAdapter() {
        sliceDataMap = Maps.newHashMap();
    }

    @Override
    public void setDelegate(StoreDelegate<QosSliceStoreEvent> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void unsetDelegate(StoreDelegate<QosSliceStoreEvent> delegate) {
        this.delegate = null;
    }

    @Override
    public boolean hasDelegate() {
        return this.delegate != null;
    }

    @Override
    public void addSlice (QosSliceData qoSData) {
        sliceDataMap.put(qoSData.getQosName(), qoSData);
    }

    @Override
    public void removeSlice(QosSliceData qoSData) {
        sliceDataMap.remove(qoSData.getQosName());
    }

    @Override
    public void updateSlice(QosSliceData vplsData) {
        sliceDataMap.put(vplsData.getQosName(), vplsData);
    }

    @Override
    public QosSliceData getSlice(String vplsName) {
        return sliceDataMap.get(vplsName);
    }

    @Override
    public Collection<QosSliceData> getAllSlice() {
        return sliceDataMap.values();
    }
}




