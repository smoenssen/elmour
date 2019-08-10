package com.smoftware.elmour.profile;

import com.badlogic.gdx.utils.Array;

public class ProfileSubject {

    private Array<ProfileObserver> _observers;

    public ProfileSubject(){
        _observers = new Array<ProfileObserver>();
    }

    public void addObserver(ProfileObserver profileObserver){
        _observers.add(profileObserver);
    }

    public void removeObserver(ProfileObserver profileObserver){
        _observers.removeValue(profileObserver, true);
    }

    public void removeAllObservers(){
        _observers.removeAll(_observers, true);
    }

    protected void notify(final ProfileManager profileManager, ProfileObserver.ProfileEvent event){
        // can't use an iterator here because it might be nested
        for (int i = 0; i < _observers.size; i++) {
            ProfileObserver observer = _observers.get(i);
            observer.onNotify(profileManager, event);
        }
    }

}
