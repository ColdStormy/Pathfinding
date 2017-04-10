package de.pathfinding;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Pathfinding extends ApplicationAdapter {
	private SpriteBatch batch;

	// the grid whether it's traversable(false) or not(true)
    private boolean[][] grid;

    // start and end point in vectors
    private Vector2 sta = new Vector2();
    private Vector2 end = new Vector2();

    // probability per gridfield for being a wall when generating a new grid
    private double density = 0.2;

    // engine calculates the paths and so on
    private Engine engine;

    private BitmapFont font;

    private int frame = 0;

    // run animation?
    private boolean play = false;


	@Override
	public void create () {
		batch = new SpriteBatch();
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		grid = new boolean[100][50];

		for(int i=0; i<grid.length; i++) {
			for(int k=0; k<grid[i].length; k++) {
				grid[i][k] = Math.random()<density;
			}
		}

		engine = new Engine(grid);

	}

	@Override
	public void render () {

        // create a wall when clicking
		if(Gdx.input.isTouched()) {
			int x = Gdx.input.getX()/10;
			int y = (Gdx.graphics.getHeight()-Gdx.input.getY())/10;
			grid[x][y] = true;
		}

		// set startvector
		if(!Gdx.input.isTouched() && Gdx.input.isKeyJustPressed(Input.Keys.A)) {

			int x = Gdx.input.getX()/10;
			int y = (Gdx.graphics.getHeight()-Gdx.input.getY())/10;

			sta = new Vector2(x, y);
			engine.setStart(sta);


		}

        // set endvector
		if(!Gdx.input.isTouched() && Gdx.input.isKeyJustPressed(Input.Keys.D)) {

			int x = Gdx.input.getX()/10;
			int y = (Gdx.graphics.getHeight()-Gdx.input.getY())/10;

			end = new Vector2(x, y);
			engine.setDest(end);

		}

		// start animation (and restart the engine; otherwise we get to see weird errors of
        // mixing the old and new path)
		if(!Gdx.input.isTouched() && Gdx.input.isKeyJustPressed(Input.Keys.S)) {
			System.out.println("Starting");
			play = true;
			engine.flush();
		}

		// update every 2 frames the next(!) step of our engine
		if( play && frame % 2 == 0 && !engine.pathFound ) {
		    // Uncomment this block to see immediatly a result plus its processing time

		    //long t = System.currentTimeMillis();
		    //while(!engine.pathFound)
					engine.next();
		    //System.out.println("It took me "+(System.currentTimeMillis()-t)+"ms to find the best path");
        }

        // Clear the screen and restart the engine
        if(!Gdx.input.isTouched() && Gdx.input.isKeyJustPressed(Input.Keys.C)) {
		    //clear screen

            engine.flush();

            for(int i=0; i<grid.length; i++)
                for(int k=0; k<grid[i].length; k++)
                    grid[i][k] = false;
            engine.grid = grid;

            play = false;
        }




		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        ShapeRenderer sr = new ShapeRenderer();

		sr.begin(ShapeRenderer.ShapeType.Filled);


		for(int i=0; i<grid.length; i++) {
			for(int k=0; k<grid[i].length; k++) {
				if(grid[i][k]) {
					sr.setColor(0,0,0, 0.3f);
					sr.rect(i*10, k*10, 10, 10);
				}
			}
		}


		if( play ) {
            // draw openList
            sr.setColor(0,0.5f,0.5f,0.5f);
            for(PNode p : engine.openList) {
                sr.rect( p.x*10, p.y*10, 10, 10);
            }

            // draw closedList
            sr.setColor(0,0.5f,0.5f,1f);
            for(PNode p : engine.closedList) {
                sr.rect( p.x*10, p.y*10, 10, 10);
            }

            // draw the final path
            if(engine.pathFound) {
                sr.setColor(1,0,1,1);
                for(int i=0; i<engine.path.size(); i++) {
                    sr.rect(engine.path.get(i).x*10, engine.path.get(i).y*10, 10, 10);
                }
            }

        }


        // draw start vector
		sr.setColor(0,1,0, 1);
		sr.rect(((int)sta.x)*10, ((int)sta.y)*10, 10, 10);

		// draw end vector
		sr.setColor(1, 0, 0, 1);
		sr.rect(((int) end.x)*10, ((int)end.y)*10, 10, 10);

		// everything below is in regard of the blue box.
        sr.setColor(0,0,.5f, .7f);
		sr.rect(0, 0, 250, 250);

		sr.end();

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        batch.begin();
        font.draw(batch, "[1] Cross corners: "+engine.cornersAllowed, 10, 240);
        font.draw(batch, "[2] Allow diagonal: "+engine.diagonalAllowed, 10, 220);
        font.draw(batch, "[A] Set start", 10, 200);
        font.draw(batch, "[D] Set end", 10, 180);
        font.draw(batch, "[C] Clear walls", 10, 160);
        font.draw(batch, "Click to draw walls manually", 10, 140);
        font.draw(batch, "[R] Generate new random map", 10, 120);
        font.draw(batch, "[N] Increase density: "+df.format(density), 10, 100);
        font.draw(batch, "[M] Decrease density: "+df.format(density), 10, 80);
        font.draw(batch, "[S] Run animation", 10, 50);
        batch.end();

        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            engine.cornersAllowed = !engine.cornersAllowed;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            engine.diagonalAllowed = !engine.diagonalAllowed;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            for(int i=0; i<grid.length; i++) {
                for(int k=0; k<grid[i].length; k++) {
                    grid[i][k] = Math.random()<density;
                }
            }

            engine.grid = grid;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            System.out.print("+");
            if(density<0.95)
                density += 0.05;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            System.out.print("-");
            if(density>0.05)
                density -= 0.05;
        }




		frame++;
	}
	
	@Override
	public void dispose () {
		font.dispose();
	    batch.dispose();
	    font.dispose();
	}
}
