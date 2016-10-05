/*
Copyright (C) 2011-2013 Maksim Petrov

Redistribution and use in source and binary forms, with or without
modification, are permitted for widgets, plugins, applications and other software
which communicate with Poweramp application on Android platform.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.maxmpz.poweramp.player;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;


/**
 * Poweramp intent based API.
 */
public final class PowerampAPI {
	/**
	 * Defines PowerampAPI version, which could be also 200 and 210 for older Poweramps.
	 */
	public static final int VERSION = 533;
	
	/**
	 * No id flag.
	 */
	public static final int NO_ID = 0;
	
	public static final String AUTHORITY = "com.maxmpz.audioplayer.data";
	
	public static final Uri ROOT_URI = new Uri.Builder().scheme("content").authority(AUTHORITY).build();
	
	/**
	 * Uri query parameter - filter.
	 */
	public static final String PARAM_FILTER = "flt";
	/**
	 * Uri query parameter - shuffle mode.
	 */
	public static final String PARAM_SHUFFLE = "shf";
	
	
	/**
	 * Poweramp Control action.
	 * Should be sent with sendBroadcast().
	 * Extras:
	 * 	- cmd - int - command to execute.
	 */
	public static final String ACTION_API_COMMAND = "com.maxmpz.audioplayer.API_COMMAND";
	
	public static Intent newAPIIntent() {
		return new Intent(ACTION_API_COMMAND).setComponent(PLAYER_SERVICE_COMPONENT_NAME);
	}
	
	/**
	 * ACTION_API_COMMAND extra.
	 * Int.
	 */
	public static final String COMMAND = "cmd";
	
	
	/** 
	 * 
	 * Commonm extras:
	 * - beep - boolean - (optional) if true, Poweramp will beep on playback command
	 */
	public static final class Commands {
		/**
		 * Extras:
		 * - keepService - boolean - (optional) if true, Poweramp won't unload player service. Notification will be appropriately updated.
		 */
		public static final int TOGGLE_PLAY_PAUSE = 1;
		/**
		 * Extras:
		 * - keepService - boolean - (optional) if true, Poweramp won't unload player service. Notification will be appropriately updated.
		 */
		public static final int PAUSE = 2;
		public static final int RESUME = 3;
		/**
		 * NOTE: subject to 200ms throttling.
		 */
		public static final int NEXT = 4;
		/**
		 * NOTE: subject to 200ms throttling.
		 */
		public static final int PREVIOUS = 5;
		/**
		 * NOTE: subject to 200ms throttling.
		 */
		public static final int NEXT_IN_CAT = 6;
		/**
		 * NOTE: subject to 200ms throttling.
		 */
		public static final int PREVIOUS_IN_CAT = 7;
		/**
		 * Extras:
		 * - showToast - boolean - (optional) if false, no toast will be shown. Applied for cycle only.
		 * - repeat - int - (optional) if exists, appropriate mode will be directly selected, otherwise modes will be cycled, see Repeat class.
		 */
		public static final int REPEAT = 8;
		/**
		 * Extras:
		 * - showToast - boolean - (optional) if false, no toast will be shown. Applied for cycle only.
		 * - shuffle - int - (optional) if exists, appropriate mode will be directly selected, otherwise modes will be cycled, see Shuffle class.
		 */
		public static final int SHUFFLE = 9;
		public static final int BEGIN_FAST_FORWARD = 10;
		public static final int END_FAST_FORWARD = 11;
		public static final int BEGIN_REWIND = 12;
		public static final int END_REWIND = 13;
		public static final int STOP = 14;
		/**
		 * Extras:
		 * - pos - int - seek position in seconds.
		 */
		public static final int SEEK = 15;
		public static final int POS_SYNC = 16;
		
		/**
		 * Data:
		 * - uri, following URIs are recognized:
		 * 	- file://path
		 * 	- content://com.maxmpz.audioplayer.data/... (see below)
		 * 
		 * # means some numeric id (track id for queries ending with /files, otherwise - appropriate category id). 
		 * If song id (in place of #) is not specified, Poweramp plays whole list starting from the specified song,
		 * or from first one, or from random one in shuffle mode.
		 * 
		 * All queries support following params (added as URL encoded params, e.g. content://com.maxmpz.audioplayer.data/files?lim=10&flt=foo):
		 * lim - integer - SQL LIMIT, which limits number of rows returned
		 * flt - string - filter substring. Poweramp will return only matching rows (the same way as returned in Poweramp lists UI when filter is used).
		 * hier - long - hierarchy folder id. Used only to play in shuffle lists/shuffle songs mode while in hierarchy folders view. This is the target folder id
		 *               which will be shuffled with the all subfolders in it as one list.
		 * shf - integer - shuffle mode (see ShuffleMode class)
		 * ssid - long - shuffle session id (for internal use)
		 * 
		 * Each /files/meta subquery returns special crafted query with some metainformation provided (it differs in each category, you can explore it by analizing the cols returned).                
		
		- All Songs:
		content://com.maxmpz.audioplayer.data/files
		content://com.maxmpz.audioplayer.data/files/meta
		content://com.maxmpz.audioplayer.data/files/#

		- Most Played
		content://com.maxmpz.audioplayer.data/most_played
		content://com.maxmpz.audioplayer.data/most_played/files
		content://com.maxmpz.audioplayer.data/most_played/files/meta
		content://com.maxmpz.audioplayer.data/most_played/files/#
		
		- Top Rated
		content://com.maxmpz.audioplayer.data/top_rated
		content://com.maxmpz.audioplayer.data/top_rated/files
		content://com.maxmpz.audioplayer.data/top_rated/files/meta
		content://com.maxmpz.audioplayer.data/top_rated/files/#

		- Recently Added
		content://com.maxmpz.audioplayer.data/recently_added
		content://com.maxmpz.audioplayer.data/recently_added/files
		content://com.maxmpz.audioplayer.data/recently_added/files/meta
		content://com.maxmpz.audioplayer.data/recently_added/files/#
		
		- Recently Played
		content://com.maxmpz.audioplayer.data/recently_played
		content://com.maxmpz.audioplayer.data/recently_played/files
		content://com.maxmpz.audioplayer.data/recently_played/files/meta
		content://com.maxmpz.audioplayer.data/recently_played/files/#
		
		- Plain folders view (just files in plain folders list)
		content://com.maxmpz.audioplayer.data/folders
		content://com.maxmpz.audioplayer.data/folders/#
		content://com.maxmpz.audioplayer.data/folders/#/files
		content://com.maxmpz.audioplayer.data/folders/#/files/meta
		content://com.maxmpz.audioplayer.data/folders/#/files/#
		
		- Hierarchy folders view (files and folders intermixed in one cursor)
		content://com.maxmpz.audioplayer.data/folders/#/folders_and_files
		content://com.maxmpz.audioplayer.data/folders/#/folders_and_files/meta
		content://com.maxmpz.audioplayer.data/folders/#/folders_and_files/#
		content://com.maxmpz.audioplayer.data/folders/files // All folder files, sorted as folders_files sort (for mass ops).
		
		- Genres
		content://com.maxmpz.audioplayer.data/genres
		content://com.maxmpz.audioplayer.data/genres/#/files
		content://com.maxmpz.audioplayer.data/genres/#/files/meta
		content://com.maxmpz.audioplayer.data/genres/#/files/#
		content://com.maxmpz.audioplayer.data/genres/files

		- Artists
		content://com.maxmpz.audioplayer.data/artists
		content://com.maxmpz.audioplayer.data/artists/#
		content://com.maxmpz.audioplayer.data/artists/#/files
		content://com.maxmpz.audioplayer.data/artists/#/files/meta
		content://com.maxmpz.audioplayer.data/artists/#/files/#
		content://com.maxmpz.audioplayer.data/artists/files
		
		- Composers
		content://com.maxmpz.audioplayer.data/composers
		content://com.maxmpz.audioplayer.data/composers/#
		content://com.maxmpz.audioplayer.data/composers/#/files
		content://com.maxmpz.audioplayer.data/composers/#/files/#
		content://com.maxmpz.audioplayer.data/composers/#/files/meta
		content://com.maxmpz.audioplayer.data/composers/files
		
		- Albums 
    	content://com.maxmpz.audioplayer.data/albums
    	content://com.maxmpz.audioplayer.data/albums/#/files
    	content://com.maxmpz.audioplayer.data/albums/#/files/#
    	content://com.maxmpz.audioplayer.data/albums/#/files/meta
    	content://com.maxmpz.audioplayer.data/albums/files
    	
    	- Albums by Genres
    	content://com.maxmpz.audioplayer.data/genres/#/albums
    	content://com.maxmpz.audioplayer.data/genres/#/albums/meta
    	content://com.maxmpz.audioplayer.data/genres/#/albums/#/files
    	content://com.maxmpz.audioplayer.data/genres/#/albums/#/files/#
    	content://com.maxmpz.audioplayer.data/genres/#/albums/#/files/meta
    	content://com.maxmpz.audioplayer.data/genres/#/albums/files
    	content://com.maxmpz.audioplayer.data/genres/albums

    	- Albums by Artists
    	content://com.maxmpz.audioplayer.data/artists/#/albums
    	content://com.maxmpz.audioplayer.data/artists/#/albums/meta
    	content://com.maxmpz.audioplayer.data/artists/#/albums/#/files
    	content://com.maxmpz.audioplayer.data/artists/#/albums/#/files/#
    	content://com.maxmpz.audioplayer.data/artists/#/albums/#/files/meta
    	content://com.maxmpz.audioplayer.data/artists/#/albums/files
    	content://com.maxmpz.audioplayer.data/artists/albums

    	- Albums by Composers
    	content://com.maxmpz.audioplayer.data/composers/#/albums
    	content://com.maxmpz.audioplayer.data/composers/#/albums/meta
    	content://com.maxmpz.audioplayer.data/composers/#/albums/#/files
    	content://com.maxmpz.audioplayer.data/composers/#/albums/#/files/#
    	content://com.maxmpz.audioplayer.data/composers/#/albums/#/files/meta
    	content://com.maxmpz.audioplayer.data/composers/#/albums/files
    	content://com.maxmpz.audioplayer.data/composers/albums

    	- Artists Albums
    	content://com.maxmpz.audioplayer.data/artists_albums
    	content://com.maxmpz.audioplayer.data/artists_albums/meta
    	content://com.maxmpz.audioplayer.data/artists_albums/#/files
    	content://com.maxmpz.audioplayer.data/artists_albums/#/files/#
    	content://com.maxmpz.audioplayer.data/artists_albums/#/files/meta
    	content://com.maxmpz.audioplayer.data/artists_albums/files
		
		- Playlists
		content://com.maxmpz.audioplayer.data/playlists
		content://com.maxmpz.audioplayer.data/playlists/#
		content://com.maxmpz.audioplayer.data/playlists/#/files
		content://com.maxmpz.audioplayer.data/playlists/#/files/#
		content://com.maxmpz.audioplayer.data/playlists/#/files/meta
		content://com.maxmpz.audioplayer.data/playlists/files
		
		- Library Search
		content://com.maxmpz.audioplayer.data/search
		
		- Equalizer Presets
		content://com.maxmpz.audioplayer.data/eq_presets
		content://com.maxmpz.audioplayer.data/eq_presets/#
		content://com.maxmpz.audioplayer.data/eq_presets_songs
		content://com.maxmpz.audioplayer.data/queue
		content://com.maxmpz.audioplayer.data/queue/#

		 *  
		 * Extras:
		 * - paused - boolean - (optional) default false. OPEN_TO_PLAY command starts playing the file immediately, unless "paused" extra is true.
		 *                       (see PowerampAPI.PAUSED)
		 * 
		 * - pos - int - (optional) seek to this position in song before playing (see PowerampAPI.Track.POSITION)
		 */
		public static final int OPEN_TO_PLAY = 20;
		
		/**
		 * Extras:
		 * - id - long - preset ID
		 */
		public static final int SET_EQU_PRESET = 50;

		/**
		 * Extras:
		 * - value - string - equalizer values, see ACTION_EQU_CHANGED description.
		 */
		public static final int SET_EQU_STRING = 51;

		/**
		 * Extras:
		 * - name - string - equalizer band (bass/treble/preamp/31/62../8K/16K) name
		 * - value - float - equalizer band value (bass/treble/, 31/62../8K/16K => -1.0...1.0, preamp => 0..2.0)
		 */
		public static final int SET_EQU_BAND = 52;
		
		/**
		 * Extras:
		 * - equ - boolean - if exists and true, equalizer is enabled
		 * - tone - boolean - if exists and true, tone is enabled
		 */
		public static final int SET_EQU_ENABLED = 53;
		
		/**
		 * Used by Notification controls to stop pending/paused service/playback and unload/remove notification.
		 * Since 2.0.6
		 */
		public static final int STOP_SERVICE = 100;
	}
	
	/**
	 * Extra.
	 * Mixed.
	 */
	public static final String API_VERSION = "api";

	/**
	 * Extra.
	 * Mixed.
	 */
	public static final String CONTENT = "content";
	
	/**
	 * Extra.
	 * String.
	 */
	public static final String PACKAGE = "pak";
	
	/**
	 * Extra.
	 * String.
	 */
	public static final String LABEL = "label";
	
	/**
	 * Extra.
	 * Boolean.
	 */
	public static final String AUTO_HIDE = "autoHide";
	
	/**
	 * Extra.
	 * Bitmap.
	 */
	public static final String ICON = "icon";

	/**
	 * Extra.
	 * Boolean.
	 */
	public static final String MATCH_FILE = "matchFile";
	
	/**
	 * Extra.
	 * Boolean
	 */
	public static final String SHOW_TOAST = "showToast";
	
	/**
	 * Extra.
	 * String.
	 */
	public static final String NAME = "name";

	/**
	 * Extra.
	 * Mixed.
	 */
	public static final String VALUE = "value";
	
	/**
	 * Extra.
	 * Boolean.
	 */
	public static final String EQU = "equ";

	/**
	 * Extra.
	 * Boolean.
	 */
	public static final String TONE = "tone";
	
	/**
	 * Extra.
	 * Boolean.
	 * Since 2.0.6
	 */
	public static final String KEEP_SERVICE = "keepService"; 
	
	/**
	 * Extra.
	 * Boolean
	 * Since build 533
	 */
	public static final String BEEP = "beep";
	
	
	/**
	 * Poweramp track changed.
	 * Sticky intent.
	 * Extras:
	 * - track - bundle - Track bundle, see Track class.
	 * - ts - long - timestamp of the event (System.currentTimeMillis()).
	 *  Note, that by default Poweramp won't search/download album art when screen is OFF, but will do that on next screen ON event.  
	 */
	public static final String ACTION_TRACK_CHANGED = "com.maxmpz.audioplayer.TRACK_CHANGED";
	
	/**
	 * Album art was changed. Album art can be the same for whole album/folder, thus usually it will be updated less frequently comparing to TRACK_CHANGE.
	 * If both aaPath and aaBitmap extras are missing that means no album art exists for the current track(s).
	 * Note that there is no direct Album Art to track relation, i.e. both track and album art can change independently from each other -
	 * for example - when new album art asynchronously downloaded from internet or selected by user.
	 * Sticky intent.
	 * Extras:
	 * - aaPath - String - (optional) if exists, direct path to the cached album art is available.
	 * - aaBitmap - Bitmap - (optional)	if exists, some rescaled up to 500x500 px album art bitmap is available.
	 *              There will be aaBitmap if aaPath is available, but image is bigger than 600x600 px.
	 * - delayed - boolean - (optional) if true, this album art was downloaded or selected later by user.
              
	 * - ts - long - timestamp of the event (System.currentTimeMillis()).
	 */
	public static final String ACTION_AA_CHANGED = "com.maxmpz.audioplayer.AA_CHANGED";

	/**
	 * Poweramp playing status changed (track started/paused/resumed/ended, playing ended).
	 * Sticky intent.
	 * Extras: 
	 * - status - string - one of the STATUS_* values
	 * - pos - int - (optional) current in-track position in seconds.
	 * - ts - long - timestamp of the event (System.currentTimeMillis()).
	 * - additional extras - depending on STATUS_ value (see STATUS_* description below).
	 */
	public static final String ACTION_STATUS_CHANGED = "com.maxmpz.audioplayer.STATUS_CHANGED";
	
	/**
	 * NON sticky intent.
	 * - pos - int - current in-track position in seconds. 
	 */
	public static final String ACTION_TRACK_POS_SYNC = "com.maxmpz.audioplayer.TPOS_SYNC";
	
	/**
	 * Poweramp repeat or shuffle mode changed.
	 * Sticky intent.
	 * Extras:
	 * - repeat - int - new repeat mode. See RepeatMode class.
	 * - shuffle - int - new shuffle mode. See ShuffleMode class.
	 * - ts - long - timestamp of the event (System.currentTimeMillis()).	 * 
	 */
	public static final String ACTION_PLAYING_MODE_CHANGED = "com.maxmpz.audioplayer.PLAYING_MODE_CHANGED";

	/**
	 * Poweramp equalizer settings changed.
	 * Sticky intent.
	 * Extras:
	 * - name - string - preset name. If no name extra exists, it's not a preset.
	 * - id - long - preset id. If no id extra exists, it's not a preset.
	 * - value - string - equalizer and tone values in format:
	 *   	bass=pos_float|treble=pos_float|31=float|62=float|....|16K=float|preamp=0.0 ... 2.0
	 *      where float = -1.0 ... 1.0, pos_float = 0.0 ... 1.0
	 * - equ - boolean - true if equalizer bands are enabled
	 * - tone - boolean - truel if tone bands are enabled
	 * - ts - long - timestamp of the event (System.currentTimeMillis()).
	 */
	public static final String ACTION_EQU_CHANGED = "com.maxmpz.audioplayer.EQU_CHANGED";
	
	/**
	 * Special actions for com.maxmpz.audioplayer.PlayerUIActivity only.
	 */
	public static final String ACTION_SHOW_CURRENT = "com.maxmpz.audioplayer.ACTION_SHOW_CURRENT";
	public static final String ACTION_SHOW_LIST  = "com.maxmpz.audioplayer.ACTION_SHOW_LIST";
	

	public static final String PACKAGE_NAME = "com.maxmpz.audioplayer";
	public static final String PLAYER_SERVICE_NAME = "com.maxmpz.audioplayer.player.PlayerService";
	
	public static final ComponentName PLAYER_SERVICE_COMPONENT_NAME = new ComponentName(PACKAGE_NAME, PLAYER_SERVICE_NAME);
	
	public static final String ACTIVITY_PLAYER_UI = "com.maxmpz.audioplayer.PlayerUIActivity";
	public static final String ACTIVITY_EQ = "com.maxmpz.audioplayer.EqActivity";
	
	/**
	 * If com.maxmpz.audioplayer.ACTION_SHOW_LIST action is sent to this activity, it will react to some extras.
	 * Extras:
	 * Data:
	 * - uri - uri of the list to display. 
	 */
	public static final String ACTIVITY_PLAYLIST = "com.maxmpz.audioplayer.PlayListActivity";
	public static final String ACTIVITY_SETTINGS = "com.maxmpz.audioplayer.preference.SettingsActivity";
	
	/**
	 * Extra.
	 * String.
	 */
	public static final String ALBUM_ART_PATH = "aaPath";
	
	/**
	 * Extra.
	 * Bitmap. 
	 */
	public static final String ALBUM_ART_BITMAP = "aaBitmap";

	/**
	 * Extra.
	 * boolean. 
	 */
	public static final String DELAYED = "delayed";


	/**
	 * Extra.
	 * long.
	 */
	public static final String TIMESTAMP = "ts";
	
	/**
	 * STATUS_CHANGED extra. See Status class for values.
	 * Int.
	 */
	public static final String STATUS = "status";	

	/**
	 * STATUS extra values.
	 */
	public static final class Status {
		/**
		 * STATUS_CHANGED status value - track has been started to play or has been paused.
		 * Note that Poweramp will start track immediately into this state when it's just loaded to avoid STARTED => PAUSED transition. 
		 * Additional extras:
		 * 	track - bundle - track info 
		 * 	paused - boolean - true if track paused, false if track resumed
		 */
		public static final int TRACK_PLAYING = 1;
		
		/**
		 * STATUS_CHANGED status value - track has been ended. Note, this intent will NOT be sent for just finished song IF Poweramp advances to the next song.
		 * Additional extras:
		 * 	track - bundle - track info 
		 *  failed - boolean - true if track failed to play
		 */
		public static final int TRACK_ENDED = 2;

		/**
		 * STATUS_CHANGED status value - Poweramp finished playing some list and stopped.
		 */
		public static final int PLAYING_ENDED = 3;
	}
	
	
	/**
	 * STATUS_CHANGED trackEnded extra. 
	 * Boolean. True if track failed to play.
	 */
	public static final String FAILED = "failed";

	/**
	 * STATUS_CHANGED trackStarted/trackPausedResumed extra.
	 * Boolean. True if track is paused.
	 */
	public static final String PAUSED = "paused";
	
	/**
	 * PLAYING_MODE_CHANGED extra. See ShuffleMode class.
	 * Integer.
	 */
	public static final String SHUFFLE = "shuffle";

	/**
	 * PLAYING_MODE_CHANGED extra. See RepeatMode class.
	 * Integer.
	 */
	public static final String REPEAT = "repeat";
	
	
	/**
	 * Extra.
	 * Long.
	 */
	public static final String ID = "id";

	/**
	 * STATUS_CHANGED track extra.
	 * Bundle.
	 */
	public static final String TRACK = "track";
	
	
	/**
	 * shuffle extras values.
	 */
	public static final class ShuffleMode {
		public static final int SHUFFLE_NONE = 0;
		public static final int SHUFFLE_ALL = 1;
		public static final int SHUFFLE_SONGS = 2;
		public static final int SHUFFLE_CATS = 3; // Songs in order.
		public static final int SHUFFLE_SONGS_AND_CATS = 4; // Songs shuffled.
	}
	
	/**
	 * repeat extras values.
	 */
	public static final class RepeatMode {
		public static final int REPEAT_NONE = 0;
		public static final int REPEAT_ON = 1;
		public static final int REPEAT_ADVANCE = 2;
		public static final int REPEAT_SONG = 3;	
	}
	
	
	/**
	 * STATUS_CHANGED track extra fields.
	 */
	public static final class Track {
		/**
		 * Id of the current track.
		 * Can be a playlist entry id.
		 * Long.
		 */
		public static final String ID = "id";
		
		/**
		 * "Real" id. In case of playlist entry, this is always resolved to Poweramp folder_files table row ID or System Library MediaStorage.Audio._ID. 
		 * Long.
		 */
		public static final String REAL_ID = "realId";
		
		/**
		 * Category type.
		 * See Track.Type class.
		 * Int.
		 */
		public static final String TYPE = "type";
		
		/**
		 * Category URI match.
		 * Int.
		 */
		public static final String CAT = "cat";
		
		/**
		 * Boolean.
		 */
		public static final String IS_CUE = "isCue";
		
		/**
		 * Category URI.
		 * Uri.
		 */
		public static final String CAT_URI = "catUri";
		
		/**
		 * File type. See Track.FileType.
		 * Integer.
		 */
		public static final String FILE_TYPE = "fileType";
	
		/**
		 * Song file path.
		 * String
		 */
		public static final String PATH = "path";
		
		/**
		 * Song title.
		 * String
		 */
		public static final String TITLE = "title";
		
		/**
		 * Song album.
		 * String.
		 */
		public static final String ALBUM = "album";
		
		/**
		 * Song artist.
		 * String.
		 */
		public static final String ARTIST = "artist";
		
		/**
		 * Song duration in seconds.
		 * Int.
		 */
		public static final String DURATION = "dur";
		
		/**
		 * Position in song in seconds.
		 * Int.
		 */
		public static final String POSITION = "pos";
		
		/**
		 * Position in a list.
		 * Int.
		 */
		public static final String POS_IN_LIST = "posInList";

		/**
		 * List size.
		 * Int.
		 */
		public static final String LIST_SIZE = "listSize";

		/**
		 * Song sample rate.
		 * Int.
		 */
		public static final String SAMPLE_RATE = "sampleRate";
		
		/**
		 * Song number of channels.
		 * Int.
		 */
		public static final String CHANNELS = "channels";
		
		/**
		 * Song average bitrate.
		 * Int.
		 */
		public static final String BITRATE = "bitRate";
		
		/**
		 * Resolved codec name for the song.
		 * Int.
		 */
		public static final String CODEC = "codec";
		
		/**
		 * Track flags.
		 * Int.
		 */
		public static final String FLAGS = "flags";
		
		/**
		 * Track.fileType values.
		 */
		public static final class FileType {
			public static final int mp3 = 0; 
			public static final int flac = 1;
			public static final int m4a = 2;
			public static final int mp4 = 3;
			public static final int ogg = 4;
			public static final int wma = 5;
			public static final int wav = 6;
			public static final int tta = 7;
			public static final int ape = 8;
			public static final int wv = 9;
			public static final int aac = 10;
			public static final int mpga = 11;
			public static final int amr = 12;
			public static final int _3gp = 13;
			public static final int mpc = 14;
			public static final int aiff = 15;
			public static final int aif = 16;
		}
		
		/**
		 * Track.flags bitset values. First 3 bits = FLAG_ADVANCE_* 
		 */
		public static final class Flags {
			public static final int FLAG_ADVANCE_NONE = 0;
			public static final int FLAG_ADVANCE_FORWARD = 1;
			public static final int FLAG_ADVANCE_BACKWARD = 2;
			public static final int FLAG_ADVANCE_FORWARD_CAT = 3;
			public static final int FLAG_ADVANCE_BACKWARD_CAT = 4;
			
			public static final int FLAG_ADVANCE_MASK = 0x7; // 111
			
			public static final int FLAG_NOTIFICATION_UI = 0x20;
			public static final int FLAG_FIRST_IN_PLAYER_SESSION = 0x40; // Currently used just to indicate that track is first in playerservice session.			
		}
	}
	
	public static final class Cats {
		public static final int ROOT = 0;
		public static final int FOLDERS = 10;
		public static final int GENRES_ID_ALBUMS = 210;
		public static final int ALBUMS = 200;
		public static final int GENRES = 320;
		public static final int ARTISTS = 500;
		public static final int ARTISTS_ID_ALBUMS = 220;		
		public static final int ARTISTS__ALBUMS = 250;
		public static final int COMPOSERS = 600;
		public static final int COMPOSERS_ID_ALBUMS = 230;
		public static final int PLAYLISTS = 100;
		public static final int QUEUE = 800;
		public static final int MOST_PLAYED = 43;
		public static final int TOP_RATED = 48;
		public static final int RECENTLY_ADDED = 53;
		public static final int RECENTLY_PLAYED = 58;
	}
	

	public static final class Scanner {
		 
		/**
		 * Poweramp Scanner action.
		 * 
		 * Poweramp Scanner scanning process is 2 step:
		 * 1. Folders scan.
		 *    Checks filesystem and updates DB with folders/files structure.
		 * 2. Tags scan.
		 *    Iterates over files in DB with TAG_STATUS == TAG_NOT_SCANNED and scans them with tag scanner.
		 *    
		 * Poweramp Scanner is a IntentService, this means multiple scan requests at the same time (or during another scans) are queued.  
		 * ACTION_SCAN_DIRS actions are prioritized and executed before ACTION_SCAN_TAGS.
		 * 
		 * Poweramp main scan action, which scans either incrementally or from scratch the set of folders, which is configured by user in Poweramp Settings.
		 * Poweramp will always do ACTION_SCAN_TAGS automatically after ACTION_SCAN_DIRS is finished and some changes are required to song tags in DB.
		 * Unless, fullRescan specified, Poweramp will not remove songs if they are missing from filesystem due to unmounted storages.
		 * Normal menu => Rescan calls ACTION_SCAN_DIRS without extras
		 * 
		 * Poweramp Scanner sends appropriate broadcast intents:
		 * ACTION_DIRS_SCAN_STARTED (sticky), ACTION_DIRS_SCAN_FINISHED, ACTION_TAGS_SCAN_STARTED (sticky), ACTION_TAGS_SCAN_PROGRESS, ACTION_TAGS_SCAN_FINISHED, or ACTION_FAST_TAGS_SCAN_FINISHED.
		 * 
		 * Extras:
		 * - fastScan - Poweramp will not check folders and scan files which hasn't been modified from previous scan. Based on files last modified timestamp.
         *				 Poweramp doesn;t send 
		 *               
		 * - eraseTags - Poweramp will clean all tags from exisiting songs. This causes each song to be re-scanned for tags.
		 *               Warning: as a side effect, cleans CUE tracks from user created playlists. 
		 *               This is because scanner can't incrementaly re-scan CUE sheets, so they are deleted from DB.
		 *               
		 * - fullRescan - Poweramp will also check for folders/files from missing/unmounted storages and will remove them from DB. 
		 *                Warning: removed songs also disappear from user created playlists.
		 *                Used in Poweramp only when user specificaly goes to Settings and does Full Rescan (after e.g. SD card change).                
		 *                                  
		 */
		public static final String ACTION_SCAN_DIRS = "com.maxmpz.audioplayer.ACTION_SCAN_DIRS";

		/**
		 * Poweramp Scanner action.
		 * Secondary action, only checks songs with TAG_STATUS set to TAG_NOT_SCANNED. Useful for rescanning just songs (which are already in Poweramp DB) with editied file tag info.  
		 *
		 * Extras:
		 * - fastScan - If true, scanner doesn't send ACTION_TAGS_SCAN_STARTED/ACTION_TAGS_SCAN_PROGRESS/ACTION_TAGS_SCAN_FINISHED intents, 
		 *               just sends ACTION_FAST_TAGS_SCAN_FINISHED when done.
		 *               It doesn't modify scanning logic otherwise.  
		 */
		public static final String ACTION_SCAN_TAGS = "com.maxmpz.audioplayer.ACTION_SCAN_TAGS";
		

		/**
		 * Broadcast.
		 * Poweramp Scanner started folders scan.
		 * This is sticky broadcast, so Poweramp folder scanner running status can be polled via registerReceiver() return value.
		 */
		public static final String ACTION_DIRS_SCAN_STARTED = "com.maxmpz.audioplayer.ACTION_DIRS_SCAN_STARTED";
		/**
		 * Broadcast.
		 * Poweramp Scanner finished folders scan.
		 */
		public static final String ACTION_DIRS_SCAN_FINISHED = "com.maxmpz.audioplayer.ACTION_DIRS_SCAN_FINISHED";
		/**
		 * Broadcast.
		 * Poweramp Scanner started tag scan.
		 * This is sticky broadcast, so Poweramp tag scanner running status can be polled via registerReceiver() return value.
		 */
		public static final String ACTION_TAGS_SCAN_STARTED = "com.maxmpz.audioplayer.ACTION_TAGS_SCAN_STARTED";
		/**
		 * Broadcast.
		 * Poweramp Scanner tag scan in progess.
		 * Extras:
		 * - progress - 0-100 progress of scanning.
		 */
		public static final String ACTION_TAGS_SCAN_PROGRESS = "com.maxmpz.audioplayer.ACTION_TAGS_SCAN_PROGRESS";
		/**
		 * Broadcast.
		 * Poweramp Scanner finished tag scan.
		 * Extras:
		 * - track_content_changed - boolean - true if at least on track has been scanned, false if no tags scanned (probably, because all files are up-to-date).
		 */
		public static final String ACTION_TAGS_SCAN_FINISHED = "com.maxmpz.audioplayer.ACTION_TAGS_SCAN_FINISHED";
		/**
		 * Broadcast.
		 * Poweramp Scanner finished fast tag scan. Only fired when ACTION_SCAN_TAGS is called with extra fastScan = true.
		 * Extras:
		 * - trackContentChanged - boolean - true if at least on track has been scanned, false if no tags scanned (probably, because all files are up-to-date).
		 */
		public static final String ACTION_FAST_TAGS_SCAN_FINISHED = "com.maxmpz.audioplayer.ACTION_FAST_TAGS_SCAN_FINISHED";
		
		/**
		 * Extra.
		 * Boolean.
		 */
		public static final String EXTRA_FAST_SCAN = "fastScan";
		/**
		 * Extra.
		 * Int.
		 */
		public static final String EXTRA_PROGRESS = "progress";
		/**
		 * Extra.
		 * Boolean.
		 */
		public static final String EXTRA_TRACK_CONTENT_CHANGED = "trackContentChanged";	

		/**
		 * Extra.
		 * Boolean.
		 */
		public static final String EXTRA_ERASE_TAGS = "eraseTags";
		
		/**
		 * Extra.
		 * Boolean.
		 */
		public static final String EXTRA_FULL_RESCAN = "fullRescan";
		
		/**
		 * Extra.
		 * String.
		 */
		public static final String EXTRA_CAUSE = "cause";
	}
	
	public static final class Settings {
		public static final String ACTION_EXPORT_SETTINGS = "com.maxmpz.audioplayer.ACTION_EXPORT_SETTINGS";
		public static final String ACTION_IMPORT_SETTINGS = "com.maxmpz.audioplayer.ACTION_IMPORT_SETTINGS";

		public static final String EXTRA_UI = "ui";
	}
	
}
