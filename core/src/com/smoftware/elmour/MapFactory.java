package com.smoftware.elmour;

import java.util.Hashtable;

import static com.smoftware.elmour.MapFactory.MapType.TOP_WORLD;

public class MapFactory {
    //All maps for the game
    private static Hashtable<MapType,Map> _mapTable = new Hashtable<MapType, Map>();

    public static enum MapType{
        MAP1,
        TOP_WORLD,
        TOWN,
        CASTLE_OF_DOOM
    }

    static public Map getMap(MapType mapType){
        Map map = null;
        switch(mapType){
            case MAP1:
                map = _mapTable.get(MapType.MAP1);
                if( map == null ){
                    map = new Map1();
                    _mapTable.put(MapType.MAP1, map);
                }
                break;
            case TOP_WORLD:
                map = _mapTable.get(TOP_WORLD);
                if( map == null ){
                    map = new TopWorldMap();
                    _mapTable.put(TOP_WORLD, map);
                }
                break;
            case TOWN:
                map = _mapTable.get(MapType.TOWN);
                if( map == null ){
                    map = new TownMap();
                    _mapTable.put(MapType.TOWN, map);
                }
                break;
            case CASTLE_OF_DOOM:
                map = _mapTable.get(MapType.CASTLE_OF_DOOM);
                if( map == null ){
                    map = new CastleDoomMap();
                    _mapTable.put(MapType.CASTLE_OF_DOOM, map);
                }
                break;
            default:
                break;
        }
        return map;
    }

    public static void clearCache(){
        for( Map map: _mapTable.values()){
            map.dispose();
        }
        _mapTable.clear();
    }
}
