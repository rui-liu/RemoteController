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

import java.util.ArrayList;
import java.util.List;

import com.woodblockwithoutco.remotecontroller.OnArtworkChangeListener;
import com.woodblockwithoutco.remotecontroller.OnMetadataChangeListener;
import com.woodblockwithoutco.remotecontroller.OnPlaybackStateChangeListener;
import com.woodblockwithoutco.remotecontroller.OnRemoteControlFeaturesChangeListener;
import com.woodblockwithoutco.remotecontroller.PlayState;
import com.woodblockwithoutco.remotecontroller.RemoteControlFeature;


import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Implementation of Handler.Callback interface to transfer necessary data to
 * RemoteMetadataProvider. Shouldn't be used explicitly by user.
 */
class MetadataUpdaterCallbackV14 implements Handler.Callback {

	/*
	 * Information about current client.
	 */
	private int mGenerationId;
	private List<RemoteControlFeature> mFeatureList = null;
	private OnArtworkChangeListener mArtworkChangeListener;
	private OnMetadataChangeListener mMetadataChangeListener;
	private OnPlaybackStateChangeListener mPlaybackStateChangeListener;
	private OnRemoteControlFeaturesChangeListener mRemoteControlFeaturesChangeListener;
	private RemoteControlDisplayImplV14 mProxyImpl;

	public MetadataUpdaterCallbackV14(RemoteControlDisplayImplV14 proxy) {
		mFeatureList = new ArrayList<RemoteControlFeature>();
		mProxyImpl = proxy;
	}
	
	public void setArtworkChangeListener(OnArtworkChangeListener l) {
		mArtworkChangeListener = l;
	}
	
	public void setMetadataChangeListener(OnMetadataChangeListener l) {
		mMetadataChangeListener = l;
	}
	
	public void setPlaybackStateChangeListener(OnPlaybackStateChangeListener l) {
		mPlaybackStateChangeListener = l;
	}
	
	public void setRemoteControlFeaturesChangeListener(OnRemoteControlFeaturesChangeListener l) {
		mRemoteControlFeaturesChangeListener = l;
	}

	/**
	 * @param bundle
	 * @param key
	 * @return Will return null if we request duration.
	 */
	private String getMetadataString(Bundle bundle, int key) {
		if (key != MediaMetadataRetriever.METADATA_KEY_DURATION) {
			return bundle.getString(String.valueOf(key));
		} else {
			return null;
		}
	}

	private long getDuration(Bundle bundle) {
		return bundle.getLong(String.valueOf(MediaMetadataRetriever.METADATA_KEY_DURATION));
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case RemoteControlDisplayV14.MSG_SET_GENERATION_ID:
			mGenerationId = msg.arg1;
			mProxyImpl.setCurrentClientPendingIntent((PendingIntent) msg.obj);
			return true;
		case RemoteControlDisplayV14.MSG_SET_METADATA:
			if (mGenerationId == msg.arg1) {
				if (mMetadataChangeListener != null) {
					Bundle metadata = (Bundle) msg.obj;
					mMetadataChangeListener.onMetadataChanged(getMetadataString(metadata, MediaMetadataRetriever.METADATA_KEY_ARTIST), 
							getMetadataString(metadata, MediaMetadataRetriever.METADATA_KEY_TITLE), 
							getMetadataString(metadata, MediaMetadataRetriever.METADATA_KEY_ALBUM), 
							getMetadataString(metadata, MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST), 
							getDuration(metadata));
				}
			}
			return true;
		case RemoteControlDisplayV14.MSG_SET_TRANSPORT_CONTROLS:
			if (mGenerationId == msg.arg1) {
				if (mRemoteControlFeaturesChangeListener != null) {
					int flags = msg.arg2;
					mFeatureList.clear();
					if ((flags | RemoteControlClient.FLAG_KEY_MEDIA_FAST_FORWARD) == flags) mFeatureList.add(RemoteControlFeature.USES_FAST_FORWARD);
					if ((flags | RemoteControlClient.FLAG_KEY_MEDIA_NEXT) == flags) mFeatureList.add(RemoteControlFeature.USES_NEXT);
					if ((flags | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE) == flags) mFeatureList.add(RemoteControlFeature.USES_PAUSE);
					if ((flags | RemoteControlClient.FLAG_KEY_MEDIA_PLAY) == flags) mFeatureList.add(RemoteControlFeature.USES_PLAY);
					if ((flags | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE) == flags) mFeatureList.add(RemoteControlFeature.USES_PLAY_PAUSE);
					if ((flags | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS) == flags) mFeatureList.add(RemoteControlFeature.USES_PREVIOUS);
					if ((flags | RemoteControlClient.FLAG_KEY_MEDIA_REWIND) == flags) mFeatureList.add(RemoteControlFeature.USES_REWIND);
					if ((flags | RemoteControlClient.FLAG_KEY_MEDIA_STOP) == flags) mFeatureList.add(RemoteControlFeature.USES_STOP);
					mRemoteControlFeaturesChangeListener.onFeaturesChanged(mFeatureList);
				}
			}
			return true;
		case RemoteControlDisplayV14.MSG_SET_ARTWORK:
			if (mGenerationId == msg.arg1) {
				if (mArtworkChangeListener != null) {

					Bitmap artwork = (Bitmap) msg.obj;
					mArtworkChangeListener.onArtworkChanged(artwork);
				}
			}
			return true;
		case RemoteControlDisplayV14.MSG_UPDATE_STATE:
			if (mGenerationId == msg.arg1) {
				if (mPlaybackStateChangeListener != null) {
					switch (msg.arg2) {
					case RemoteControlClient.PLAYSTATE_BUFFERING:
						mPlaybackStateChangeListener.onPlaybackStateChanged(PlayState.BUFFERING);
						break;
					case RemoteControlClient.PLAYSTATE_ERROR:
						mPlaybackStateChangeListener.onPlaybackStateChanged(PlayState.ERROR);
						break;
					case RemoteControlClient.PLAYSTATE_FAST_FORWARDING:
						mPlaybackStateChangeListener.onPlaybackStateChanged(PlayState.FAST_FORWARDING);
						break;
					case RemoteControlClient.PLAYSTATE_PAUSED:
						mPlaybackStateChangeListener.onPlaybackStateChanged(PlayState.PAUSED);
						break;
					case RemoteControlClient.PLAYSTATE_PLAYING:
						mPlaybackStateChangeListener.onPlaybackStateChanged(PlayState.PLAYING);
						break;
					case RemoteControlClient.PLAYSTATE_REWINDING:
						mPlaybackStateChangeListener.onPlaybackStateChanged(PlayState.REWINDING);
						break;
					case RemoteControlClient.PLAYSTATE_SKIPPING_BACKWARDS:
						mPlaybackStateChangeListener.onPlaybackStateChanged(PlayState.SKIPPING_BACKWARDS);
						break;
					case RemoteControlClient.PLAYSTATE_SKIPPING_FORWARDS:
						mPlaybackStateChangeListener.onPlaybackStateChanged(PlayState.SKIPPING_FORWARDS);
						break;
					case RemoteControlClient.PLAYSTATE_STOPPED:
						mPlaybackStateChangeListener.onPlaybackStateChanged(PlayState.STOPPED);
						break;
					}
				}
			}
			return true;
		}
		return false;
	}
}
