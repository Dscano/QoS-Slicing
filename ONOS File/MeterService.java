/*
 * Copyright 2015-present Open Networking Foundation
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
package org.onosproject.net.meter;

import org.onosproject.event.ListenerService;
import org.onosproject.net.DeviceId;

import java.util.Collection;

/**
 * Service for add/updating and removing meters. Meters are
 * are assigned to flow to rate limit them and provide a certain
 * quality of service.
 */
public interface MeterService
        extends ListenerService<MeterEvent, MeterListener> {

    /**
     * Adds a meter to the system and performs it installation.
     *
     * @param meter a meter
     * @return a meter (with a meter id)
     */
    Meter submit(MeterRequest meter);

    /**
     * Adds a meter to the system and performs it installation.
     *
     * @param meter a meter
     * @param meterId a meter
     * @return a meter (with a meter id)
     */
    Meter submitWithId(MeterRequest meter, MeterId meterId);

    /**
     * Remove a meter from the system and the dataplane.
     *
     * @param meter a meter to remove
     * @param meterId the meter id of the meter to remove.
     */
    void withdraw(MeterRequest meter, MeterId meterId);

    /**
     * Fetch the meter by the meter id.
     *
     * @param deviceId a device id
     * @param id a meter id
     * @return a meter
     */
    Meter getMeter(DeviceId deviceId, MeterId id);

    /**
     * Fetches all the meters.
     *
     * @return a collection of meters
     */
    Collection<Meter> getAllMeters();

    /**
     * Fetches the meters by the device id.
     *
     * @param deviceId a device id
     * @return a collection of meters
     */
    Collection<Meter> getMeters(DeviceId deviceId);

    /**
     * Allocates a new meter id in the system.
     *
     * @param deviceId the device id
     * @return the allocated meter id, null if there is an internal error
     * or there are no meter ids available
     */
    MeterId allocateMeterId(DeviceId deviceId);

    /**
     * Frees the given meter id.
     *
     * @param deviceId the device id
     * @param meterId the id to be freed
     */
    void freeMeterId(DeviceId deviceId, MeterId meterId);
}
