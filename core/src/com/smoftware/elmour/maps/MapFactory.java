package com.smoftware.elmour.maps;

import java.util.Hashtable;

import static com.smoftware.elmour.maps.MapFactory.MapType.TOP_WORLD;

public class MapFactory {
    //All maps for the game
    private static Hashtable<MapType,Map> _mapTable = new Hashtable<MapType, Map>();

    public static enum MapType{
        ARMORY,
        BARREN_ROOM,
        CASTLE,
        COMPASS,
        COURTYARD,
        ELMOUR,
        GRASS_BATTLE,
        GRASS_TEMPLE,
        INN,
        JERBADIA,
        MAP1,
        MAP2,
        MAP3,
        MAP4,
        MAP5,
        MAP6,
        MAP7,
        MAP8,
        MAP9,
        MAP10,
        MAP11,
        MAP14,
        M6_CAVE,
        M6_CAVE_A,
        M6_CAVE_B,
        PORTAL_ROOM,
        SHNARFULAPOGUS,
        TARPING_TOWN,
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
            case BARREN_ROOM:
                map = _mapTable.get(MapType.BARREN_ROOM);
                if( map == null ){
                    map = new Barren_Room();
                    _mapTable.put(MapType.BARREN_ROOM, map);
                }
                break;
            case CASTLE:
                map = _mapTable.get(MapType.CASTLE);
                if( map == null ){
                    map = new Castle();
                    _mapTable.put(MapType.CASTLE, map);
                }
                break;
            case COMPASS:
                map = _mapTable.get(MapType.COMPASS);
                if( map == null ){
                    map = new Compass();
                    _mapTable.put(MapType.COMPASS, map);
                }
                break;
            case COURTYARD:
                map = _mapTable.get(MapType.COURTYARD);
                if( map == null ){
                    map = new Courtyard();
                    _mapTable.put(MapType.COURTYARD, map);
                }
                break;
            case ELMOUR:
                map = _mapTable.get(MapType.ELMOUR);
                if( map == null ){
                    map = new Elmour();
                    _mapTable.put(MapType.ELMOUR, map);
                }
                break;
            case GRASS_BATTLE:
                map = _mapTable.get(MapType.GRASS_BATTLE);
                if( map == null ){
                    map = new GrassBattle();
                    _mapTable.put(MapType.GRASS_BATTLE, map);
                }
                break;
            case GRASS_TEMPLE:
                map = _mapTable.get(MapType.GRASS_TEMPLE);
                if( map == null ){
                    map = new GrassTemple();
                    _mapTable.put(MapType.GRASS_TEMPLE, map);
                }
                break;
            case INN:
                map = _mapTable.get(MapType.INN);
                if( map == null ){
                    map = new Inn();
                    _mapTable.put(MapType.INN, map);
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
            case MAP7:
                map = _mapTable.get(MapType.MAP7);
                if( map == null ){
                    map = new Map7();
                    _mapTable.put(MapType.MAP7, map);
                }
                break;
            case MAP8:
                map = _mapTable.get(MapType.MAP8);
                if( map == null ){
                    map = new Map8();
                    _mapTable.put(MapType.MAP8, map);
                }
                break;
            case MAP9:
                map = _mapTable.get(MapType.MAP9);
                if( map == null ){
                    map = new Map9();
                    _mapTable.put(MapType.MAP9, map);
                }
                break;
            case MAP10:
                map = _mapTable.get(MapType.MAP10);
                if( map == null ){
                    map = new Map10();
                    _mapTable.put(MapType.MAP10, map);
                }
                break;
            case MAP11:
                map = _mapTable.get(MapType.MAP11);
                if( map == null ){
                    map = new Map11();
                    _mapTable.put(MapType.MAP11, map);
                }
                break;
            case MAP14:
                map = _mapTable.get(MapType.MAP14);
                if( map == null ){
                    map = new Map14();
                    _mapTable.put(MapType.MAP14, map);
                }
                break;
            case M6_CAVE:
                map = _mapTable.get(MapType.M6_CAVE);
                if( map == null ){
                    map = new M6_Cave();
                    _mapTable.put(MapType.M6_CAVE, map);
                }
                break;
            case M6_CAVE_A:
                map = _mapTable.get(MapType.M6_CAVE_A);
                if( map == null ){
                    map = new M6_Cave_A();
                    _mapTable.put(MapType.M6_CAVE_A, map);
                }
                break;
            case M6_CAVE_B:
                map = _mapTable.get(MapType.M6_CAVE_B);
                if( map == null ){
                    map = new M6_Cave_B();
                    _mapTable.put(MapType.M6_CAVE_B, map);
                }
                break;
            case PORTAL_ROOM:
                map = _mapTable.get(MapType.PORTAL_ROOM);
                if( map == null ){
                    map = new Portal_Room();
                    _mapTable.put(MapType.PORTAL_ROOM, map);
                }
                break;
            case SHNARFULAPOGUS:
                map = _mapTable.get(MapType.SHNARFULAPOGUS);
                if( map == null ){
                    map = new Shnarfulapogus();
                    _mapTable.put(MapType.SHNARFULAPOGUS, map);
                }
                break;
            case TARPING_TOWN:
                map = _mapTable.get(MapType.TARPING_TOWN);
                if( map == null ){
                    map = new TarpingTown();
                    _mapTable.put(MapType.TARPING_TOWN, map);
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
