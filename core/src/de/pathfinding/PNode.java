package de.pathfinding;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by jannis on 09.04.17.
 */
public class PNode {

    public int x;
    public int y;

    public int g;
    public int h;
    public int f;

    public boolean isStart = false;
    public boolean isDest = false;

    public PNode relatedTo;

    public PNode(int x, int y, PNode rt) {
        this.x = x;
        this.y = y;
        relatedTo = rt;
    }

    public void setValues(int vg, int vh) {
        g = vg;
        h = vh;
        f= g+h;
    }

    public void setValuesToStart(Vector2 start, int vh) {
        g = (int) ( Math.abs(start.x-x)+Math.abs(start.y-y) );
        h = vh;
        f = g+h;

    }

    public int getSingleG() {
        return g-relatedTo.g;
    }


    public Vector2 getVector() {
        return new Vector2(x,y);
    }
}
