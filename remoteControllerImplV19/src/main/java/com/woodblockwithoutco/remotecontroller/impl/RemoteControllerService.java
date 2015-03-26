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


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.woodblockwithoutco.remotecontroller.MediaCommand;
import com.woodblockwithoutco.remotecontroller.PlayState;
import com.woodblockwithoutco.remotecontroller.RemoteControlFeature;
import com.woodblockwithoutco.remotecontroller.RemoteControllerIntents;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.RemoteController;
import android.media.RemoteController.MetadataEditor;
import android.os.SystemClock;

@TargetApi(VERSION_CODES.KITKAT)
public class RemoteControllerService extends NotificationListenerService implements RemoteController.OnClientUpdateListener {

	private static final String TAG = "MusicControlService";


	private static final Intent MUSIC_SERVICE_BIND_INTENT = new Intent(RemoteControllerIntents.MUSIC_SERVICE_BIND_ACTION);
	private static final Intent MUSIC_SERVICE_UNBIND_INTENT = new Intent(RemoteControllerIntents.MUSIC_SERVICE_UNBIND_ACTION);

	private RemoteController mRemoteController;
	private List<String> mFeatureList;
	private Field mPendingIntentField;

	private Method mSetSynchronizationMethod;
	private Field mRcdField;

	@Override
	public void onCreate() {
		mRemoteController = new RemoteController(getApplicationContext(), this);
		mFeatureList = new ArrayList<String>();

		try {
			mPendingIntentField = RemoteController.class.getDeclaredField("mClientPendingIntentCurrent");
			mPendingIntentField.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new NullPointerException("Can't find RemoteControl pending intent field!");
		}

		Class<?> IRCDclass;
		try {
			IRCDclass = Class.forName("android.media.IRemoteControlDisplay");
		} catch (ClassNotFoundException e) {
			throw new NullPointerException("Can't find IRemoteControlDisplay class!");
		}

		try {
			mSetSynchronizationMethod = AudioManager.class.getDeclaredMethod("remoteControlDisplayWantsPlaybackPositionSync", IRCDclass, boolean.class);
			mSetSynchronizationMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new NullPointerException("Can't find synchronization method!");
		}

		try {
			mRcdField = RemoteController.class.getDeclaredField("mRcd");
			mRcdField.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new NullPointerException("Can't find mRcd field!");
		}

        ServiceHolder.setService(this);
        sendBroadcast(MUSIC_SERVICE_BIND_INTENT);

	}

	@Override
	public void onDestroy() {
		sendBroadcast(MUSIC_SERVICE_UNBIND_INTENT);
        ServiceHolder.setService(null);
	}

	@Override
	public void onNotificationPosted(StatusBarNotification notification) {
		return;
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification notification) {
		return;
	}

	public boolean setSynchronizationEnabled(boolean enabled) {
		AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		boolean result = false;
		try {
			Object rcd = mRcdField.get(mRemoteController);
			mSetSynchronizationMethod.invoke(manager, rcd, enabled);
			return true;
		} catch (IllegalAccessException e) {
			result = false;
		} catch (IllegalArgumentException e) {
			result = false;
		} catch (InvocationTargetException e) {
			result = false;
		}
		return result;
	}

	@Override
	public void onClientChange(boolean clearing) {
		if(clearing) {
			Intent clearingIntent = new Intent(RemoteControlIntent.ACTION_METADATA_CHANGED);
			clearingIntent.putExtra("artist", (String)null);
			clearingIntent.putExtra("albumArtist", (String)null);
			clearingIntent.putExtra("title", (String)null);
			clearingIntent.putExtra("album", (String)null);
			clearingIntent.putExtra("duration", (long)-1);
			LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(clearingIntent);
		}
	}

	@Override
	public void onClientMetadataUpdate(MetadataEditor editor) {
		String artist = editor.getString(MediaMetadataRetriever.METADATA_KEY_ARTIST, null);
		String albumArtist = editor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, null);
		String title = editor.getString(MediaMetadataRetriever.METADATA_KEY_TITLE, null);
		String album = editor.getString(MediaMetadataRetriever.METADATA_KEY_ALBUM, null);
		long duration = editor.getLong(MediaMetadataRetriever.METADATA_KEY_DURATION, -1);

		Intent metadataChangeIntent = new Intent(RemoteControlIntent.ACTION_METADATA_CHANGED);
		metadataChangeIntent.putExtra("artist", artist);
		metadataChangeIntent.putExtra("albumArtist", albumArtist);
		metadataChangeIntent.putExtra("title", title);
		metadataChangeIntent.putExtra("album", album);
		metadataChangeIntent.putExtra("duration", duration);
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(metadataChangeIntent);

		Intent artworkChangeIntent = new Intent(RemoteControlIntent.ACTION_ARTWORK_CHANGED);
		artworkChangeIntent.putExtra("artwork", editor.getBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, null));
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(artworkChangeIntent);
	}

	@Override
	public void onClientPlaybackStateUpdate(int state) {
		onClientPlaybackStateUpdate(state, 0, 0, 1.0f);
	}

	@Override
	public void onClientPlaybackStateUpdate(int state, long stateChangeTimeMs, long currentPosMs, float speed) {
		PlayState resultState = null;
		switch(state) {
		case RemoteControlClient.PLAYSTATE_BUFFERING:
			resultState = PlayState.BUFFERING;
			break;
		case RemoteControlClient.PLAYSTATE_ERROR:
			resultState =  PlayState.ERROR;
			break;
		case RemoteControlClient.PLAYSTATE_FAST_FORWARDING:
			resultState = PlayState.FAST_FORWARDING;
			break;
		case RemoteControlClient.PLAYSTATE_PAUSED:
			resultState = PlayState.PAUSED;
			break;
		case RemoteControlClient.PLAYSTATE_PLAYING:
			resultState = PlayState.PLAYING;
			break;
		case RemoteControlClient.PLAYSTATE_REWINDING:
			resultState = PlayState.REWINDING;
			break;
		case RemoteControlClient.PLAYSTATE_SKIPPING_BACKWARDS:
			resultState = PlayState.SKIPPING_BACKWARDS;
			break;
		case RemoteControlClient.PLAYSTATE_SKIPPING_FORWARDS:
			resultState = PlayState.SKIPPING_FORWARDS;
			break;
		case RemoteControlClient.PLAYSTATE_STOPPED:
		default:
			resultState = PlayState.STOPPED;
			break;
		}

		Intent playStateChangeIntent = new Intent(RemoteControlIntent.ACTION_PLAYBACK_STATE_CHANGED);
		playStateChangeIntent.putExtra("state", resultState);
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(playStateChangeIntent);
	}


	@Override
	public void onClientTransportControlUpdate(int flags) {
		mFeatureList.clear();
		if ((flags | RemoteControlClient.FLAG_KEY_MEDIA_FAST_FORWARD) == flags) mFeatureList.add(RemoteControlFeature.USES_FAST_FORWARD.name());
		if ((flags | RemoteControlClient.FLAG_KEY_MEDIA_NEXT) == flags) mFeatureList.add(RemoteControlFeature.USES_NEXT.name());
		if ((flags | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE) == flags) mFeatureList.add(RemoteControlFeature.USES_PAUSE.name());
		if ((flags | RemoteControlClient.FLAG_KEY_MEDIA_PLAY) == flags) mFeatureList.add(RemoteControlFeature.USES_PLAY.name());
		if ((flags | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE) == flags) mFeatureList.add(RemoteControlFeature.USES_PLAY_PAUSE.name());
		if ((flags | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS) == flags) mFeatureList.add(RemoteControlFeature.USES_PREVIOUS.name());
		if ((flags | RemoteControlClient.FLAG_KEY_MEDIA_REWIND) == flags) mFeatureList.add(RemoteControlFeature.USES_REWIND.name());
		if ((flags | RemoteControlClient.FLAG_KEY_MEDIA_STOP) == flags) mFeatureList.add(RemoteControlFeature.USES_STOP.name());
		if ((flags | RemoteControlClient.FLAG_KEY_MEDIA_POSITION_UPDATE) == flags) mFeatureList.add(RemoteControlFeature.USES_SCRUBBING.name());


		Intent remoteControlFeaturesChangeIntent = new Intent(RemoteControlIntent.ACTION_REMOTE_CONTROL_FEATURES_CHANGED);
		remoteControlFeaturesChangeIntent.putExtra("features", mFeatureList.toArray(new String[mFeatureList.size()]));
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(remoteControlFeaturesChangeIntent);
	}

	public boolean registerRemoteControls() {
		AudioManager manager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		return manager.registerRemoteController(mRemoteController);
	}

	public boolean registerRemoteControls(int w, int h) {
		AudioManager manager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		mRemoteController.setArtworkConfiguration(w, h);
		return manager.registerRemoteController(mRemoteController);
	}

	public void unregisterRemoteControls() {
		AudioManager manager = (AudioManager)getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		manager.unregisterRemoteController(mRemoteController);
	}

	public long getPosition() {
		return mRemoteController.getEstimatedMediaPosition();
	}

	public boolean seekTo(long position) {
		return mRemoteController.seekTo(position);
	}


	public void sendBroadcastMediaCommand(MediaCommand command) {
		switch (command) {
		case REWIND:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_REWIND);
			break;
		case PREVIOUS:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
			break;
		case PLAY:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_PLAY);
			break;
		case PAUSE:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_PAUSE);
			break;
		case PLAY_PAUSE:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
			break;
		case STOP:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_STOP);
			break;
		case NEXT:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_NEXT);
			break;
		case FAST_FORWARD:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD);
			break;
		}
	}


	public boolean sendMediaCommand(MediaCommand command) {
		switch (command) {
		case REWIND:
			return sendButton(KeyEvent.KEYCODE_MEDIA_REWIND);
		case PREVIOUS:
			return sendButton(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
		case PLAY:
			return sendButton(KeyEvent.KEYCODE_MEDIA_PLAY);
		case PAUSE:
			return sendButton(KeyEvent.KEYCODE_MEDIA_PAUSE);
		case PLAY_PAUSE:
			return sendButton(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
		case STOP:
			return sendButton(KeyEvent.KEYCODE_MEDIA_STOP);
		case NEXT:
			return sendButton(KeyEvent.KEYCODE_MEDIA_NEXT);
		case FAST_FORWARD:
			return sendButton(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD);
		}
		return false;
	}

	private boolean sendButton(int keyCode) {
		KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
		Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
		mRemoteController.sendMediaKeyEvent(keyEvent);
		keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
		intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
		return mRemoteController.sendMediaKeyEvent(keyEvent);
	}

	private void sendBroadcastButton(int keyCode) {
		Context context = getApplicationContext();
		if(context != null) {
			long eventtime = SystemClock.uptimeMillis();
			Intent keyIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
			KeyEvent keyEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, keyCode, 0);
			keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
			context.sendOrderedBroadcast(keyIntent, null);
			keyEvent = KeyEvent.changeAction(keyEvent, KeyEvent.ACTION_UP);
			keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
			context.sendOrderedBroadcast(keyIntent, null);
		}
	}

	private void sendBroadcastButton(int keyCode, PendingIntent pintent) {
		Context context = getApplicationContext();
		if(context != null) {
			try {
				long eventtime = SystemClock.uptimeMillis();
				Intent keyIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
				KeyEvent keyEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, keyCode, 0);
				keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
				pintent.send(context, 0, keyIntent);
				keyEvent = KeyEvent.changeAction(keyEvent, KeyEvent.ACTION_UP);
				keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
				pintent.send(context, 0, keyIntent);
			} catch (CanceledException e) {
				Log.e(TAG, "Sending broadcast to specific player has been cancelled");
			}
		}
	}

	public Intent getCurrentClientIntent() {
		PendingIntent clientIntent;
		try {
			clientIntent = (PendingIntent) mPendingIntentField.get(mRemoteController);

			if(clientIntent == null) return null;

			String packageName = clientIntent.getCreatorPackage();

			if(packageName == null) return null;

			Intent result = getPackageManager().getLaunchIntentForPackage(packageName);

			if(result == null) return null;

			result.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			return result;
		} catch (IllegalAccessException e) {
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public boolean isClientActive() {
		PendingIntent clientIntent;
		try {
			clientIntent = (PendingIntent) mPendingIntentField.get(mRemoteController);
			return clientIntent != null;
		} catch (IllegalAccessException e) {
			return false;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public void sendBroadcastMediaCommand(MediaCommand command, PendingIntent intent) {
		switch (command) {
		case REWIND:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_REWIND, intent);
			break;
		case PREVIOUS:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_PREVIOUS, intent);
			break;
		case PLAY:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_PLAY, intent);
			break;
		case PAUSE:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_PAUSE, intent);
			break;
		case PLAY_PAUSE:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, intent);
			break;
		case STOP:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_STOP, intent);
			break;
		case NEXT:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_NEXT, intent);
			break;
		case FAST_FORWARD:
			sendBroadcastButton(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, intent);
			break;
		}
	}
}

