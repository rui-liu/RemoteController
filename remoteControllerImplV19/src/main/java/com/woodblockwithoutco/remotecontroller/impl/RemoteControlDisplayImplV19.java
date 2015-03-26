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
package com.woodblockwithoutco.remotecontroller.impl;

import java.util.ArrayList;
import java.util.List;

import com.woodblockwithoutco.remotecontroller.MediaCommand;
import com.woodblockwithoutco.remotecontroller.OnArtworkChangeListener;
import com.woodblockwithoutco.remotecontroller.OnMetadataChangeListener;
import com.woodblockwithoutco.remotecontroller.OnPlaybackStateChangeListener;
import com.woodblockwithoutco.remotecontroller.OnRemoteControlFeaturesChangeListener;
import com.woodblockwithoutco.remotecontroller.PlayState;
import com.woodblockwithoutco.remotecontroller.RemoteControlDisplay;
import com.woodblockwithoutco.remotecontroller.RemoteControlFeature;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;




public class RemoteControlDisplayImplV19 implements RemoteControlDisplay {

	private Context mContext;
	private IntentFilter mFilter;
	private OnPlaybackStateChangeListener mPlaybackStateChangeListener;
	private OnRemoteControlFeaturesChangeListener mRemoteControlFeaturesChangeListener;
	private OnMetadataChangeListener mMetadataChangeListener;
	private OnArtworkChangeListener mArtworkChangeListener;
	private BroadcastReceiver mReceiver = new RemoteControllerServiceBroadcastReceiver();
	private boolean mIsRegistered = false;


	public RemoteControlDisplayImplV19(Context context) {
		mContext = context;
		mFilter = new IntentFilter();
		mFilter.addAction(RemoteControlIntent.ACTION_ARTWORK_CHANGED);
		mFilter.addAction(RemoteControlIntent.ACTION_METADATA_CHANGED);
		mFilter.addAction(RemoteControlIntent.ACTION_PLAYBACK_STATE_CHANGED);
		mFilter.addAction(RemoteControlIntent.ACTION_REMOTE_CONTROL_FEATURES_CHANGED);
	}

	@Override
	public boolean registerRemoteControls() {
		RemoteControllerService instance = ServiceHolder.getService();
		if(instance != null) {
			mIsRegistered = instance.registerRemoteControls();
		}
		LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, mFilter);
		return mIsRegistered;

	}

	@Override
	public boolean registerRemoteControls(int width, int height) {
		RemoteControllerService instance = ServiceHolder.getService();
		if(instance != null) {
			mIsRegistered = instance.registerRemoteControls(width, height);
		}
		if(mIsRegistered) LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, mFilter);
		return mIsRegistered;
	}

	@Override
	public void unregisterRemoteControls() {
		mIsRegistered = false;
		RemoteControllerService instance = ServiceHolder.getService();
		if(instance != null) {
			instance.unregisterRemoteControls();
			LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
		}
	}

	@Override
	public void unregisterAndDestroyRemoteControls() {
		unregisterRemoteControls();
	}

	@Override
	public Intent getCurrentClientIntent() {
        RemoteControllerService instance = ServiceHolder.getService();
        if(instance != null) {
			return instance.getCurrentClientIntent();
		}
		return null;
	}

	@Override
	public boolean isClientActive() {
        RemoteControllerService instance = ServiceHolder.getService();
        if(instance != null) {
			return instance.isClientActive();
		}
		return false;
	}

	@Override
	public void sendBroadcastMediaCommand(MediaCommand command) {
        RemoteControllerService instance = ServiceHolder.getService();
        if(instance != null) {
			instance.sendBroadcastMediaCommand(command);
		}
	}

	@Override
	public boolean sendMediaCommand(MediaCommand command) {
        RemoteControllerService instance = ServiceHolder.getService();
        if(instance != null) {
            instance.sendMediaCommand(command);
		}
		return false;
	}

	@Override
	public long getPosition() {
        RemoteControllerService instance = ServiceHolder.getService();
        if(instance != null) {
            return instance.getPosition();
        }

		return 0;
	}

	@Override
	public void setArtworkChangeListener(OnArtworkChangeListener l) {
		mArtworkChangeListener = l;
	}

	@Override
	public void setMetadataChangeListener(OnMetadataChangeListener l) {
		mMetadataChangeListener = l;
	}

	@Override
	public void setPlaybackStateChangeListener(OnPlaybackStateChangeListener l) {
		mPlaybackStateChangeListener = l;
	}

	@Override
	public void setRemoteControlFeaturesChangeListener(OnRemoteControlFeaturesChangeListener l) {
		mRemoteControlFeaturesChangeListener = l;
	}


	private class RemoteControllerServiceBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(RemoteControlIntent.ACTION_METADATA_CHANGED.equals(action)) {
				dispatchMetadataChange(intent);
			} else if(RemoteControlIntent.ACTION_PLAYBACK_STATE_CHANGED.equals(action)) {
				dispatchPlaystateChange(intent);
			} else if(RemoteControlIntent.ACTION_ARTWORK_CHANGED.equals(action)) {
				dispatchArtworkChange(intent);
			} else if(RemoteControlIntent.ACTION_REMOTE_CONTROL_FEATURES_CHANGED.equals(action)) {
				dispatchRemoteControlFeaturesChange(intent);
			}
		}

		private void dispatchMetadataChange(Intent intent) {
			if(mMetadataChangeListener != null) {
				String artist = intent.getStringExtra("artist");
				String albumArtist = intent.getStringExtra("albumArtist");
				String title = intent.getStringExtra("title");
				String album = intent.getStringExtra("album");
				long duration = intent.getLongExtra("duration", -1);

				mMetadataChangeListener.onMetadataChanged(artist, title, album, albumArtist, duration);
			}
		}

		private void dispatchPlaystateChange(Intent intent) {
			if(mPlaybackStateChangeListener != null) {
				PlayState state = (PlayState) intent.getSerializableExtra("state");
				state = state == null ? PlayState.STOPPED : state;
				mPlaybackStateChangeListener.onPlaybackStateChanged(state);
			}
		}

		private void dispatchArtworkChange(Intent intent) {
			if(mArtworkChangeListener != null) {
				Bitmap artwork = intent.getParcelableExtra("artwork");
				mArtworkChangeListener.onArtworkChanged(artwork);
			}
		}

		private void dispatchRemoteControlFeaturesChange(Intent intent) {
			if(mRemoteControlFeaturesChangeListener != null) {
				String[] features = intent.getStringArrayExtra("features");
				if(features != null) {
					List<RemoteControlFeature> result = new ArrayList<RemoteControlFeature>();
					for(String s : features) {
						result.add(RemoteControlFeature.valueOf(s));
					}
					mRemoteControlFeaturesChangeListener.onFeaturesChanged(result);
				}
			}
		}

	}


	@Override
	public boolean seekTo(long position) {
        RemoteControllerService instance = ServiceHolder.getService();
        if(instance != null) {
            return instance.seekTo(position);
		}
		return false;
	}

	@Override
	public boolean setSynchronizationEnabled(boolean enabled) {
        RemoteControllerService instance = ServiceHolder.getService();
        if(instance != null) {
            instance.setSynchronizationEnabled(enabled);
		}
		return false;
	}

	@Override
	public boolean isRegistered() {
		return mIsRegistered && ServiceHolder.getService() != null;
	}

	@Override
	public boolean pingService() {
		return ServiceHolder.getService() != null;
	}

	@Override
	public void sendBroadcastMediaCommand(MediaCommand command, PendingIntent intent) {
        RemoteControllerService instance = ServiceHolder.getService();
        if(instance != null) {
            instance.sendBroadcastMediaCommand(command, intent);
		}
	}


}
