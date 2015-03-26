/*******************************************************************************
 * Copyright 2014 Alexander Leontyev
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
 *******************************************************************************/
package com.woodblockwithoutco.remotecontroller;

import android.app.PendingIntent;
import android.content.Intent;

public interface RemoteControlDisplay {
	public boolean registerRemoteControls();
	public boolean registerRemoteControls(int width, int height);
	public void unregisterRemoteControls();
	public void unregisterAndDestroyRemoteControls();
	public Intent getCurrentClientIntent();
	public boolean isClientActive();
	public boolean isRegistered();
	public void sendBroadcastMediaCommand(MediaCommand command);
	public void sendBroadcastMediaCommand(MediaCommand command, PendingIntent intent);
	public boolean sendMediaCommand(MediaCommand command);
	public long getPosition();
	public boolean seekTo(long position);
	public boolean setSynchronizationEnabled(boolean enabled);
	public boolean pingService();

	public void setArtworkChangeListener(OnArtworkChangeListener l);
	public void setMetadataChangeListener(OnMetadataChangeListener l);
	public void setPlaybackStateChangeListener(OnPlaybackStateChangeListener l);
	public void setRemoteControlFeaturesChangeListener(OnRemoteControlFeaturesChangeListener l);
}
