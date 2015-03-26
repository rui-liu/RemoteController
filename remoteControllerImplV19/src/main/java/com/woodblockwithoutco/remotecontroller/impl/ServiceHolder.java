/*
 * Copyright 2014 Alexander Leontev
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.woodblockwithoutco.remotecontroller.impl;


import java.lang.ref.WeakReference;

class ServiceHolder {

    private static WeakReference<RemoteControllerService> sRemoteControllerService;

    public static void setService(RemoteControllerService service) {
        if(service != null) {
            sRemoteControllerService = new WeakReference<RemoteControllerService>(service);
        } else {
            sRemoteControllerService = null;
        }
    }

    public static RemoteControllerService getService() {
        return sRemoteControllerService != null ? sRemoteControllerService.get() : null;
    }
}
