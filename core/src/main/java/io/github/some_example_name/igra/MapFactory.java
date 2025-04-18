package io.github.some_example_name.igra;

public class MapFactory {
    public static BaseMap createMap(String mapPath, Player player) {
        //if (mapPath.endsWith("forest.tmx")) return new ForestMap(player);

        return new DefaultMap(mapPath, player); // Fallback
    }
}
