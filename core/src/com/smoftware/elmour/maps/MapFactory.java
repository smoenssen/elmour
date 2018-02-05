package com.smoftware.elmour.maps;

import java.util.Hashtable;

import static com.smoftware.elmour.maps.MapFactory.MapType.TOP_WORLD;

public class MapFactory {
    //All maps for the game
    private static Hashtable<MapType,Map> _mapTable = new Hashtable<MapType, Map>();

    public static enum MapType{
        ARMORY,
        COMPASS,
        ELMOUR,
        GRASS_TEMPLE,
        JERBADIA,
        MAP1,
        MAP2,
        MAP3,
        MAP4,
        MAP5,
        MAP6,
        SHNARFULAPOGUS,
        TOP_WORLD,
        TOWN,
        TOWN1,
        TOWN_SQUARE,
        CASTLE_OF_DOOM
    }

    static public Map getMap(MapType mapType){
        Map map = null;
        switch(mapType){
            case ARMORY:
                map = _mapTable.get(MapType.ARMORY);
                if( map == null ){
                    map = new Armory();
                    _mapTable.put(MapType.ARMORY, map);
                }
                break;
            case COMPASS:
                map = _mapTable.get(MapType.COMPASS);
                if( map == null ){
                    map = new Compass();
                    _mapTable.put(MapType.COMPASS, map);
                }
                break;
            case ELMOUR:
                map = _mapTable.get(MapType.ELMOUR);
                if( map == null ){
                    map = new Elmour();
                    _mapTable.put(MapType.ELMOUR, map);
                }
                break;
            case GRASS_TEMPLE:
                map = _mapTable.get(MapType.GRASS_TEMPLE);
                if( map == null ){
                    map = new GrassTemple();
                    _mapTable.put(MapType.GRASS_TEMPLE, map);
                }
                break;
            case JERBADIA:
                map = _mapTable.get(MapType.JERBADIA);
                if( map == null ){
                    map = new Jerbadia();
                    _mapTable.put(MapType.JERBADIA, map);
                }
                break;
            case MAP1:
                map = _mapTable.get(MapType.MAP1);
                if( map == null ){
                    map = new Map1();
                    _mapTable.put(MapType.MAP1, map);
                }
                break;
            case MAP2:
                map = _mapTable.get(MapType.MAP2);
                if( map == null ){
                    map = new Map2();
                    _mapTable.put(MapType.MAP2, map);
                }
                break;
            case MAP3:
                map = _mapTable.get(MapType.MAP3);
                if( map == null ){
                    map = new Map3();
                    _mapTable.put(MapType.MAP3, map);
                }
                break;
            case MAP4:
                map = _mapTable.get(MapType.MAP4);
                if( map == null ){
                    map = new Map4();
                    _mapTable.put(MapType.MAP4, map);
                }
                break;
            case MAP5:
                map = _mapTable.get(MapType.MAP5);
                if( map == null ){
                    map = new Map5();
                    _mapTable.put(MapType.MAP5, map);
                }
                break;
            case MAP6:
                map = _mapTable.get(MapType.MAP6);
                if( map == null ){
                    map = new Map6();
                    _mapTable.put(MapType.MAP6, map);
                }
                break;
            case SHNARFULAPOGUS:
                map = _mapTable.get(MapType.SHNARFULAPOGUS);
                if( map == null ){
                    map = new Shnarfulapogus();
                    _mapTable.put(MapType.SHNARFULAPOGUS, map);
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
