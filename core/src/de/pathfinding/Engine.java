package de.pathfinding;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by jannis on 09.04.17.
 */
public class Engine {

    ArrayList<PNode> openList = new ArrayList<>();
    ArrayList<PNode> closedList = new ArrayList<>();

    boolean[][] grid; // true = wall
    private Vector2 start = new Vector2();
    private Vector2 dest = new Vector2();

    ArrayList<Vector2> path = new ArrayList<>();

    boolean pathFound = false;
    boolean diagonalAllowed = true;
    boolean cornersAllowed = false; // can you cross corners?

    private int operations = 0;


    public Engine(boolean[][] grid) {
        this.grid = grid;
    }

    public void setStart(Vector2 v) {
        start = v;

        openList.clear();
        openList.add(new PNode((int) start.x, (int) start.y, null));
        openList.get(0).isStart = true;
        openList.get(0).setValues(0, 0);
    }

    public void setDest(Vector2 v) {
        dest = v;
    }

    /**
     * Process one node (with the smallest f value) in the open list
     */
    public void next() {

        // either there is no way to the destination vector or it just takes too long
        // we'll just calculate the shortest path to a point which is closest to our destination
        if(openList.size()==0 && closedList.size() > 0 || operations >= grid.length*grid[0].length*0.15) {
            System.out.println("No path found");
            System.out.println("Operations: "+operations);
            pathFound = true;

            PNode closest = closedList.get(0);
            double l = getLength(closest.getVector(), dest);

            // find closest node in regards to absolute value between node and dest point
            for(PNode p : closedList) {
                double pl = getLength(p.getVector(), dest);
                if( l > pl ) {
                    closest = p;
                    l = pl;
                } else if( l == pl ) {
                    closest = p.f > closest.f ? closest : p;
                }
            }

            // reconstruct path
            while( closest.relatedTo != null ) {

                path.add(new Vector2(closest.x, closest.y));
                closest = closest.relatedTo;

            }

            return;
        }


        PNode closest = findSmallestF(openList);

        // remove current processed node from openList
        Iterator<PNode> iter = openList.iterator();
        boolean tmp = true;
        while(iter.hasNext() && tmp) {
            PNode p = iter.next();
            if(p.x==closest.x && p.y==closest.y) {
                iter.remove();
                tmp = false;
            }
        }
        // and put it in our closedList
        closedList.add(closest);

        addNeighbours(closest);

        // got the destination point in our closedList?
        if(closest.x == dest.x && closest.y == dest.y) {
            pathFound = true;
            System.out.println("Operations: "+operations);

            // reconstruct path
            while( closest.relatedTo != null ) {

                path.add(new Vector2(closest.x, closest.y));
                closest = closest.relatedTo;

            }

        }

        operations++;

    }

    /**
     * Get the absolute value of two vectors
     * @param v1
     * @param v2
     * @return
     */
    private double getLength(Vector2 v1, Vector2 v2) {
        double x = Math.abs(v1.x-v2.x);
        double y = Math.abs(v1.y-v2.y);
        return Math.sqrt(x*x+y*y);
    }

    private void addNeighbours(PNode cl) {

        if(diagonalAllowed) {
            addOneNeighbour(cl.x - 1, cl.y + 1, cl );
            addOneNeighbour(cl.x + 1, cl.y + 1, cl );
        }
        addOneNeighbour( cl.x, cl.y+1, cl );

        addOneNeighbour( cl.x-1, cl.y, cl );
        addOneNeighbour( cl.x+1, cl.y, cl );

        if(diagonalAllowed) {
            addOneNeighbour(cl.x - 1, cl.y - 1, cl );
            addOneNeighbour(cl.x + 1, cl.y - 1, cl );
        }
        addOneNeighbour( cl.x, cl.y-1, cl );

    }

    private void addOneNeighbour(int x, int y, PNode rt) {

        // if the current node is already in the openList
        // special rules apply to this one
        for(PNode p : openList) {
            if (p.x == x && p.y == y) {
                // already in openList

                if( p.x-x==0 || p.y-y==0 ) {
                    // sidewise -> g = 10
                    if( p.g > rt.g+10) {
                        p.setValues(10 + rt.g, p.h);
                        p.relatedTo = rt;
                    }
                } else {
                    // diagonally -> g = 14
                    if( p.g > rt.g+14) {
                        p.setValues(14 + rt.g, p.h);
                        p.relatedTo = rt;
                    }
                }

                return;
            }
        }

        // node is already in closedList
        for(PNode p : closedList)
            if(p.x==x && p.y==y)
                return;

        // check if current node coorindates aren't out of bounds and that field isn't a wall
        if( x >= 0 && x<grid.length && y>=0 && y<grid[0].length && !grid[x][y]) {
            if( !cornersAllowed && isCorner(new Vector2(x,y), new Vector2(rt.x,rt.y)) )
                return;

            PNode n = new PNode(x, y, rt);

            if( x-rt.x==0 || y-rt.y==0 ) {
                // sidewise
                n.setValues(10+rt.g, manhattanDistance(new Vector2(x,y)));
            } else {
                // diagonally
                n.setValues( 14+rt.g, manhattanDistance(new Vector2(x,y)));
            }

            openList.add(n);

        }
    }

    private int manhattanDistance(Vector2 vector2) {
        return 10*( (int) Math.abs(vector2.x-dest.x) + (int) Math.abs(vector2.y-dest.y) );
    }

    private PNode findSmallestF(ArrayList<PNode> in) {
        PNode minF = in.get(0);

        for(PNode p : in) {
            if(p.f < minF.f) {

                minF = p;
            }
        }
        return minF;

    }

    public boolean isCorner(Vector2 v1, Vector2 v2) {
        return grid[(int)v1.x][(int)v2.y] || grid[(int)v2.x][(int)v1.y];
    }

    public void flush() {
        openList.clear();
        closedList.clear();
        path.clear();
        pathFound = false;
        operations = 0;

        setStart(start);
    }
}
