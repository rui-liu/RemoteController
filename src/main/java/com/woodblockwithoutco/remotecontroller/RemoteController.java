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


import com.woodblockwithoutco.remotecontroller.impl.RemoteControlDisplayImplV14;
import com.woodblockwithoutco.remotecontroller.impl.RemoteControlDisplayImplV18;
import com.woodblockwithoutco.remotecontroller.impl.RemoteControlDisplayImplV19;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;

public class RemoteController implements RemoteControlDisplay {

	private RemoteControlDisplay mImpl;
	
	public RemoteController(Context context) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			mImpl = new RemoteControlDisplayImplV19(context);
		} else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2) {
			mImpl = new RemoteControlDisplayImplV18(context);
		} else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			mImpl = new RemoteControlDisplayImplV14(context);
		}
		
		if(mImpl == null) throw new UnsupportedOperationException("Android API level must be >= 14!");
	}
	
	
	public boolean registerRemoteControls() {
		return mImpl.registerRemoteControls();
	}
	
	public boolean registerRemoteControls(int width, int height) {
		return mImpl.registerRemoteControls(width, height);
	}
	
	public void unregisterRemoteControls() {
		mImpl.unregisterRemoteControls();
	}
	
	public void unregisterAndDestroyRemoteControls() {
		mImpl.unregisterAndDestroyRemoteControls();
	}
	
	public Intent getCurrentClientIntent() {
		return mImpl.getCurrentClientIntent();
	}
	
	public boolean isClientActive() {
		return mImpl.isClientActive();
	}
	
	public void sendBroadcastMediaCommand(MediaCommand command) {
		mImpl.sendBroadcastMediaCommand(command);
	}
	
	public boolean sendMediaCommand(MediaCommand command) {
		return mImpl.sendMediaCommand(command);
	}
	
	public long getPosition() {
		return mImpl.getPosition();
	}
	
	public boolean seekTo(long position) {
		return mImpl.seekTo(position);
	}
	
	public void setArtworkChangeListener(OnArtworkChangeListener l) {
		mImpl.setArtworkChangeListener(l);
	}
	public void setMetadataChangeListener(OnMetadataChangeListener l) {
		mImpl.setMetadataChangeListener(l);
	}
	
	public void setPlaybackStateChangeListener(OnPlaybackStateChangeListener l) {
		mImpl.setPlaybackStateChangeListener(l);
	}
	
	public void setRemoteControlFeaturesChangeListener(OnRemoteControlFeaturesChangeListener l) {
		mImpl.setRemoteControlFeaturesChangeListener(l);
	}
	
	public boolean isRegistered() {
		return mImpl.isRegistered();
	}
	
	public boolean setSynchronizationEnabled(boolean enabled) {
		return mImpl.setSynchronizationEnabled(enabled);
	}

	@Override
	public boolean pingService() {
		return mImpl.pingService();
	}

	@Override
	public void sendBroadcastMediaCommand(MediaCommand command, PendingIntent intent) {
		mImpl.sendBroadcastMediaCommand(command, intent);
	}
}
