package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/*
NEW VERSION (ADVANCED):
-infinite playing field (or can be limited as well) (like option 10x10 or 20x20 with leaderboards)
-moving removes tile
-random positions for spikes
-if gap -> jump gap (only if on screen)
-when direction points onto spike (press SPACE to avoid and get new dir)
 */


public class GameScreen implements Screen {
    public int points;
    public int rows;
    private boolean isTileAdjacent;
    private float timeSinceLastMove;
    private float maxMoveTime;

    AssetManager assetManager;
    Texture backdrop;
    Texture playerTexture;
    Texture circleTexture;
    Texture exclamationTexture;
    Texture bgtileTexture;
    Texture tileTexture;
    NinePatch timebarFG;
    NinePatch timebarBG;
    Sound hopSound;
    Sound hurtSound;
    Music bgMusic;

    final MyGame game;
    public Viewport vp1;
    public Viewport vp2;
    public Viewport vp3;

    public int screenWidth = 800;
    public int screenHeight = 480;
    Vector2 screenCentre = new Vector2(screenWidth/2, screenHeight/2);
    public Rectangle[] tiles;
    public Rectangle player;
    public Rectangle bgtiles;

    public GameScreen(MyGame myGame, boolean shouldMusicPlay){
        this.game = myGame;

        //loading assets
        assetManager = new AssetManager();
        assetManager.load("Sprites/th_backdrop.png", Texture.class);
        assetManager.load("Sprites/th_player.png", Texture.class);
        assetManager.load("Sprites/th_circle.png", Texture.class);
        assetManager.load("Sprites/th_exclamation.png", Texture.class);
        assetManager.load("Sprites/th_bgtile.png", Texture.class);
        assetManager.load("Sprites/th_timebar_fg.png", Texture.class);
        assetManager.load("Sprites/th_timebar_bg.png", Texture.class);
        assetManager.load("Sfx/hop.wav", Sound.class);
        assetManager.load("Sfx/hurt.wav", Sound.class);
        assetManager.load("Music/nightshade.mp3", Music.class);
        assetManager.finishLoading();
        backdrop = assetManager.get("Sprites/th_backdrop.png", Texture.class);
        playerTexture = assetManager.get("Sprites/th_player.png", Texture.class);
        circleTexture = assetManager.get("Sprites/th_circle.png", Texture.class);
        exclamationTexture = assetManager.get("Sprites/th_exclamation.png", Texture.class);
        bgtileTexture = assetManager.get("Sprites/th_bgtile.png", Texture.class);
        timebarBG = new NinePatch(assetManager.get("Sprites/th_timebar_bg.png", Texture.class),3,3,2,2);
        timebarFG = new NinePatch(assetManager.get("Sprites/th_timebar_fg.png", Texture.class),3,3,2,2);
        hopSound = assetManager.get("Sfx/hop.wav", Sound.class);
        hurtSound = assetManager.get("Sfx/hurt.wav", Sound.class);
        bgMusic = assetManager.get("Music/nightshade.mp3", Music.class);
        bgMusic.setVolume(0.1f);
        scaleNinePatch(timebarFG, 8);
        scaleNinePatch(timebarBG, 8);

        rows = 5;

        player = new Rectangle();
        player.x = screenCentre.x -32;
        player.y = screenCentre.y - 32;
        player.width = 64;
        player.height = 64;
        tiles = new Rectangle[2];
        tiles[0] = new Rectangle();
        tiles[1] = new Rectangle();
        tiles[0].width =64;
        tiles[0].height = 64;
        tiles[1].width =64;
        tiles[1].height = 64;
        bgtiles = new Rectangle();
        bgtiles.width = rows * 64;
        bgtiles.height = rows * 64;
        bgtiles.setCenter(screenCentre);

        points = 0;
        timeSinceLastMove = 0;
        maxMoveTime = 1f;

        vp1 = new FitViewport(screenWidth, screenHeight);
        vp1.getCamera().position.set(screenWidth / 2f, screenHeight / 2f, 0);
        vp2 = new FitViewport(screenWidth, screenHeight);
        vp2.getCamera().position.set(screenWidth / 2f, screenHeight / 2f, 0);
        vp3 = new FitViewport(screenWidth, screenHeight);
        vp3.getCamera().position.set(screenWidth / 2f, screenHeight / 2f, 0);

        if(shouldMusicPlay){
            bgMusic.play();
            bgMusic.setLooping(true);
        }
        moveTiles();
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        if(timeSinceLastMove > maxMoveTime){
            gameOver();
            return;
        }
        if(points > 0)  timeSinceLastMove += Gdx.graphics.getDeltaTime();
        //inputs
        handleInput();

        //clearing
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);

        //viewport2 (backdrop)
        vp2.update(screenWidth, screenHeight);
        vp2.getCamera().update();
        vp2.apply();
        game.batch.setProjectionMatrix(vp2.getCamera().combined);
        game.batch.begin();
        game.batch.draw(backdrop, 0, 0, screenWidth, screenHeight);
        game.batch.end();

        //viewport1 (main stage)
        vp1.update(screenWidth, screenHeight);
        vp1.getCamera().update();
        vp1.apply();
        game.batch.setProjectionMatrix(vp1.getCamera().combined);
        game.batch.begin();
        generateBgtiles(rows);
        game.batch.draw(playerTexture, player.x, player.y, 64, 64);
        game.batch.draw(tileTexture, tiles[1].x, tiles[1].y, 64, 64);
        game.batch.end();

        //viewport3 (UI)
        vp3.update(screenWidth, screenHeight);
        vp3.getCamera().update();
        vp3.apply();
        game.batch.setProjectionMatrix(vp3.getCamera().combined);
        game.batch.begin();
        game.font.draw(game.batch, "Points: " + points, 20,screenHeight - 20);
        game.font.draw(game.batch, "FPS: " + getFps(), 20, screenHeight - 40);
        timebarBG.draw(game.batch, screenWidth/2f - 150, 10, 300, 48);
        timebarFG.draw(game.batch, screenWidth/2f - 150, 10,  Math.max(300*(maxMoveTime-timeSinceLastMove)/maxMoveTime, 5*8), 48);

        game.batch.end();
    }

    private void moveTiles(){
        tiles[0].x = player.x;
        tiles[0].y = player.y;
        int randomDir = randomNumber(0,3, false);
        switch(randomDir){
            case 0:
                placeTile("left");
                tileTexture = circleTexture;
                break;
            case 1:
                placeTile("right");
                tileTexture = circleTexture;
                break;
            case 2:
                placeTile("top");
                tileTexture = circleTexture;
                break;
            case 3:
                placeTile("bottom");
                tileTexture = circleTexture;
                break;
            }
        if(!isInBorder(tiles[1])){
            placeTile("centre");
            tileTexture = exclamationTexture;
        }
    }

    private void handleInput(){
        boolean hasMoved = false;
        if(Gdx.input.isKeyJustPressed(Input.Keys.W)){
            if (!isTileAdjacent) {
                gameOver();
                return;
            }
            player.y += 64;
            hasMoved = true;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.S)){
            if (!isTileAdjacent) {
                gameOver();
                return;
            }
            player.y += -64;
            hasMoved = true;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.D)){
            if (!isTileAdjacent) {
                gameOver();
                return;
            }
            player.x += 64;
            hasMoved = true;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.A)){
            if (!isTileAdjacent) {
                gameOver();
                return;
            }
            player.x += -64;
            hasMoved = true;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
            if (isTileAdjacent) {
                gameOver();
                return;
            }
            player.x = screenCentre.x - 32;
            player.y = screenCentre.y - 32;
            int newRows;
            do{
               newRows = randomNumber(3,7,true);
            } while (newRows == rows);
            rows = newRows;
            maxMoveTime -= maxMoveTime/10;
            updateBgtiles();
            refreshScreenSize();
            ((OrthographicCamera) vp1.getCamera()).zoom = (float) rows/5; //because 5 rows is "default"
            hasMoved = true;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || !(player.overlaps(tiles[0]) || player.overlaps(tiles[1]))){
            gameOver();
            return;
        }
        if(hasMoved){
            timeSinceLastMove = 0;
            moveTiles();
            points ++;
            hopSound.play();
        }
    }

    private void placeTile(String direction){
        switch (direction){
            case "top":
                tiles[1].x = player.x;
                tiles[1].y = player.y + 64;
                isTileAdjacent = true;
                break;
            case "bottom":
                tiles[1].x = player.x;
                tiles[1].y = player.y - 64;
                isTileAdjacent = true;
                break;
            case "left":
                tiles[1].x = player.x -64;
                tiles[1].y = player.y;
                isTileAdjacent = true;
                break;
            case "right":
                tiles[1].x = player.x + 64;
                tiles[1].y = player.y;
                isTileAdjacent = true;
                break;
            case "centre":
                tiles[1].x = screenCentre.x - 32;
                tiles[1].y = screenCentre.y - 32;
                isTileAdjacent = false;
                break;
        }
    }

    private boolean isInBorder(Rectangle rec){
        if (rec.overlaps(bgtiles)){
            return true;
        }
        return false;
    }

    private int getFps(){
        float deltaTime = Gdx.graphics.getDeltaTime();
        return (int) (1/deltaTime);
    }

    private void generateBgtiles(int rows){
        //number of rows cant be even!! (need centre tile)
        for(int y = 0; y < rows; y++){
            for(int x = 0; x < rows; x++){
                int tilePosX = (int) screenCentre.x - 32 - ((rows - 1)/2) * 64 + (x * 64);
                int tilePosY = (int) screenCentre.y - 32 - ((rows - 1)/2) * 64 + (y * 64);
                game.batch.draw(bgtileTexture, tilePosX, tilePosY, 64f,64f);
            }
        }
    }

    private void refreshScreenSize(){
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        screenCentre.x = screenWidth/2;
        screenCentre.y = screenHeight/2;
    }

    private void updateBgtiles(){
        bgtiles.width = rows * 64;
        bgtiles.height = rows * 64;
        bgtiles.setCenter(screenCentre);
    }

    private void gameOver(){
        hurtSound.play();
        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("TimeUnit Sleeper didnt work.");
        }
        game.setScreen(new MainMenuScreen(game, points));
        dispose();
    }

    private int randomNumber(int min, int max, boolean onlyUneven){
        int randInt;
        if (!onlyUneven) return ThreadLocalRandom.current().nextInt(min, max + 1);
        do {
            randInt = randomNumber(min, max, false);
        }
        while(randInt%2 == 0);
        return randInt;
    }

    private void scaleNinePatch(NinePatch ninePatch, float scale){
        ninePatch.setLeftWidth(ninePatch.getLeftWidth() *scale);
        ninePatch.setRightWidth(ninePatch.getRightWidth() * scale);
        ninePatch.setTopHeight(ninePatch.getTopHeight() * scale);
        ninePatch.setBottomHeight(ninePatch.getBottomHeight() * scale);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        hurtSound.dispose();
        hopSound.dispose();
        bgMusic.dispose();
        playerTexture.dispose();
        circleTexture.dispose();
        exclamationTexture.dispose();
        bgtileTexture.dispose();
        tileTexture.dispose();
        backdrop.dispose();
        timebarBG.getTexture().dispose();
        timebarFG.getTexture().dispose();

    }
}
