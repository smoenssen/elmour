package com.smoftware.elmour.profile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.Enumeration;
import java.util.Hashtable;

public class ProfileManager extends ProfileSubject {
    private static final String TAG = ProfileManager.class.getSimpleName();

    private Json _json;
    private static ProfileManager _profileManager;
    private Hashtable<String,FileHandle> _profiles = null;
    private ObjectMap<String, Object> _profileProperties = new ObjectMap<String, Object>();
    private String _profileName;
    private boolean _isNewProfile = false;
    private boolean isLoaded = false;

    private static final String SAVEGAME_SUFFIX = ".sav";
    public static final String SAVED_GAME_PROFILE = "saved_game";
    public static final String NEW_GAME_PROFILE = "new_game";
    public static String CUSTOM_PROFILE = "";


    private ProfileManager(){
        _json = new Json();
        _profiles = new Hashtable<String,FileHandle>();
        _profiles.clear();
        _profileName = NEW_GAME_PROFILE;
        storeAllProfiles();
    }

    public static final ProfileManager getInstance(){
        if( _profileManager == null){
            _profileManager = new ProfileManager();
        }
        return _profileManager;
    }

    public void setIsNewProfile(boolean isNewProfile){
        this._isNewProfile = isNewProfile;
    }

    public boolean getIsNewProfile(){
        return this._isNewProfile;
    }

    public Array<String> getProfileList(){
        Array<String> profiles = new Array<String>();
        for (Enumeration<String> e = _profiles.keys(); e.hasMoreElements();){
            profiles.add(e.nextElement());
        }
        return profiles;
    }

    public FileHandle getProfileFile(String profile){
        if( !doesProfileExist(profile) ){
            return null;
        }
        return _profiles.get(profile);
    }

    public void storeAllProfiles(){
        if( Gdx.files.isLocalStorageAvailable() ){
            FileHandle[] files = Gdx.files.local(".").list(SAVEGAME_SUFFIX);

            for(FileHandle file: files) {
                _profiles.put(file.nameWithoutExtension(), file);
            }
        }else{
            //TODO: try external directory here
            return;
        }
    }

    public boolean doesProfileExist(String profileName){
        return _profiles.containsKey(profileName);
    }

    public void writeProfileToStorage(String profileName, String fileData, boolean overwrite){
        String fullFilename = profileName+SAVEGAME_SUFFIX;

        boolean localFileExists = Gdx.files.local(fullFilename).exists();

        //If we cannot overwrite and the file exists, exit
        if( localFileExists && !overwrite ){
            return;
        }

        FileHandle file =  null;

        if( Gdx.files.isLocalStorageAvailable() ) {
            file = Gdx.files.local(fullFilename);
            String encodedString = fileData;//todo Base64Coder.encodeString(fileData);
            file.writeString(encodedString, !overwrite);
        }

        _profiles.put(profileName, file);
    }

    public void setProperty(String key, Object object){
        _profileProperties.put(key, object);
    }

    public void removeProperty(String key){
        _profileProperties.remove(key);
    }

    public void setIsLoaded(boolean isLoaded) { this.isLoaded = isLoaded; }

    public <T extends Object> T getProperty(String key, Class<T> type){
        T property = null;
        if( !_profileProperties.containsKey(key) ){
            return property;
        }
        property = (T)_profileProperties.get(key);
        return property;
    }

    public void saveProfile(){
        notify(this, ProfileObserver.ProfileEvent.SAVING_PROFILE);
        String text = _json.prettyPrint(_json.toJson(_profileProperties));
        saveThread(text);
    }

    public void saveCustomProfile(String customProfileName) {
        notify(this, ProfileObserver.ProfileEvent.SAVING_PROFILE);
        _profileName = customProfileName;
        String text = _json.prettyPrint(_json.toJson(_profileProperties));
        saveThread(text);
    }

    private void saveThread(final String text) {
        Runnable r = new Runnable() {
            public void run() {
                try { Thread.sleep(750); } catch (InterruptedException e) { e.printStackTrace(); }
                writeProfileToStorage(_profileName, text, true);
                sendSavedNotification();
            }
        };

        new Thread(r).start();
    }

    private void sendSavedNotification() {
        notify(this, ProfileObserver.ProfileEvent.SAVED_PROFILE);
    }

    public void loadProfile(String profileName) {
        _profileName = profileName;
        loadProfile();
    }

    public void loadProfile(){
        // only load profile once during the game. any changes are set in memory (_profileProperties)
        // and saved if the user chooses to do so
        if (!isLoaded) {
            if (_isNewProfile) {
                notify(this, ProfileObserver.ProfileEvent.CLEAR_CURRENT_PROFILE);
                saveProfile();
            }

            String fullProfileFileName = _profileName + SAVEGAME_SUFFIX;
            boolean doesProfileFileExist = Gdx.files.local(fullProfileFileName).exists();

            if (!doesProfileFileExist) {
                //System.out.println("File doesn't exist!");
                return;
            }

            FileHandle encodedFile = _profiles.get(_profileName);
            String s = encodedFile.readString();

            String decodedFile = s;//todo Base64Coder.decodeString(s);

            _profileProperties = _json.fromJson(ObjectMap.class, decodedFile);

            try { Thread.sleep(250); } catch (InterruptedException e) { e.printStackTrace(); }

            notify(this, ProfileObserver.ProfileEvent.PROFILE_LOADED);
            _isNewProfile = false;
            isLoaded = true;
        }
    }

    public void setCurrentProfile(String profileName){
        /*
        if( doesProfileExist(profileName) ){
            _profileName = profileName;
        }else{
            _profileName = NEW_GAME_PROFILE;
        }
        */
        _profileName = profileName;
    }

    public boolean currentChapterInRange(String chapterRange) {
        int minChapter = 1;
        int maxChapter = 0x7fffffff;
        if (chapterRange != null) {
            String [] sa = chapterRange.split("-");
            if (sa.length > 0) {
                minChapter = Integer.parseInt(sa[0].trim());
            }
            if (sa.length > 1) {
                maxChapter = Integer.parseInt(sa[1].trim());
            }
        }

        Integer currentChapter = getProperty("currentChapter", Integer.class);
        return (currentChapter >= minChapter && currentChapter <= maxChapter);
    }
}
