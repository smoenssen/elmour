package com.smoftware.elmour.profile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityConfig;
import com.smoftware.elmour.EntityFactory;

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
            String encodedString = fileData;//Base64Coder.encodeString(fileData);
            file.writeString(encodedString, !overwrite);
        }

        _profiles.put(profileName, file);
    }

    public void setProperty(String key, Object object){
        _profileProperties.put(key, object);
    }

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
        writeProfileToStorage(_profileName, text, true);
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

            String decodedFile = s;//Base64Coder.decodeString(s);

            _profileProperties = _json.fromJson(ObjectMap.class, decodedFile);

            // load default stats
            setStatProperties(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CARMEN), false);
            setStatProperties(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CHARACTER_1), false);
            setStatProperties(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.CHARACTER_2), false);
            setStatProperties(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.DOUGLAS), false);
            setStatProperties(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.JUSTIN), false);
            setStatProperties(EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.JAXON), false);

            notify(this, ProfileObserver.ProfileEvent.PROFILE_LOADED);
            _isNewProfile = false;
            isLoaded = true;
        }
    }

    public void setStatProperties(Entity entity, boolean update) {
        String entityID = entity.getEntityConfig().getEntityID();
        String key;
        String property;

        // set stat properties only if they are not already set, or if an update is being made
        key = entityID + EntityConfig.EntityProperties.HP.toString();
        property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP.toString().toString());
        if (!_profileProperties.containsKey(key) || update) {
            _profileProperties.put(key, property);

            // we can assume all other status values need to be set since they are always done in a batch here
            key = entityID + EntityConfig.EntityProperties.HP_MAX.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.HP_MAX.toString().toString());
            _profileProperties.put(key, property);

            key = entityID + EntityConfig.EntityProperties.MP.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP.toString().toString());
            _profileProperties.put(key, property);

            key = entityID + EntityConfig.EntityProperties.MP_MAX.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MP_MAX.toString().toString());
            _profileProperties.put(key, property);

            key = entityID + EntityConfig.EntityProperties.ATK.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.ATK.toString().toString());
            _profileProperties.put(key, property);

            key = entityID + EntityConfig.EntityProperties.MagicATK.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MagicATK.toString().toString());
            _profileProperties.put(key, property);

            key = entityID + EntityConfig.EntityProperties.DEF.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.DEF.toString().toString());
            _profileProperties.put(key, property);

            key = entityID + EntityConfig.EntityProperties.MagicDEF.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.MagicDEF.toString().toString());
            _profileProperties.put(key, property);

            key = entityID + EntityConfig.EntityProperties.SPD.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.SPD.toString().toString());
            _profileProperties.put(key, property);

            key = entityID + EntityConfig.EntityProperties.ACC.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.ACC.toString().toString());
            _profileProperties.put(key, property);

            key = entityID + EntityConfig.EntityProperties.LCK.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.LCK.toString().toString());
            _profileProperties.put(key, property);

            key = entityID + EntityConfig.EntityProperties.AVO.toString();
            property = entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.AVO.toString().toString());
            _profileProperties.put(key, property);
        }
    }

    public void getStatProperties(Entity entity) {
        String entityID = entity.getEntityConfig().getEntityID();
        String key;

        key = entityID + EntityConfig.EntityProperties.HP.toString();
        if (_profileProperties.containsKey(key)){
            String property = (String)_profileProperties.get(key);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.HP.toString(), property);

            // we can assume all other status values exists since they are always set in a batch in setStatProperties
            key = entityID + EntityConfig.EntityProperties.HP_MAX.toString();
            property = (String)_profileProperties.get(key);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.HP_MAX.toString(), property);

            key = entityID + EntityConfig.EntityProperties.MP.toString();
            property = (String)_profileProperties.get(key);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.MP.toString(), property);

            key = entityID + EntityConfig.EntityProperties.MP_MAX.toString();
            property = (String)_profileProperties.get(key);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.MP_MAX.toString(), property);

            key = entityID + EntityConfig.EntityProperties.ATK.toString();
            property = (String)_profileProperties.get(key);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.ATK.toString(), property);

            key = entityID + EntityConfig.EntityProperties.MagicATK.toString();
            property = (String)_profileProperties.get(key);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.MagicATK.toString(), property);

            key = entityID + EntityConfig.EntityProperties.DEF.toString();
            property = (String)_profileProperties.get(key);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.DEF.toString(), property);

            key = entityID + EntityConfig.EntityProperties.MagicDEF.toString();
            property = (String)_profileProperties.get(key);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.MagicDEF.toString(), property);

            key = entityID + EntityConfig.EntityProperties.SPD.toString();
            property = (String)_profileProperties.get(key);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.SPD.toString(), property);

            key = entityID + EntityConfig.EntityProperties.ACC.toString();
            property = (String)_profileProperties.get(key);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.ACC.toString(), property);

            key = entityID + EntityConfig.EntityProperties.LCK.toString();
            property = (String)_profileProperties.get(key);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.LCK.toString(), property);

            key = entityID + EntityConfig.EntityProperties.AVO.toString();
            property = (String)_profileProperties.get(key);
            entity.getEntityConfig().setPropertyValue(EntityConfig.EntityProperties.AVO.toString(), property);
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

}
