package de.pathfinding;

/**
 * Created by jannis on 09.04.17.
 */
public class Field {

    boolean isWall = false;
    boolean isStart = false;
    boolean isDest = false;

    public Field(boolean b) {
        isWall = b;
    }
}
