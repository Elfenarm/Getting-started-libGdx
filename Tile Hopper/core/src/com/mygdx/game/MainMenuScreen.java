package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;

public class MainMenuScreen implements Screen {

    final MyGame game;
    public boolean shouldMusicPlay = true;
    public OrthographicCamera camera;
    public int screenWidth = 800;
    public int screenHeight = 480;

    private Integer lastScore;

    public MainMenuScreen(MyGame myGame) {
        game = myGame;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);
    }

    public MainMenuScreen(MyGame myGame, int lastScore){
        this(myGame);
        this.lastScore = lastScore;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        if(lastScore != null){
            game.font.draw(game.batch, "Most recent score: " + lastScore, screenWidth/2 -100, screenHeight/2 - 80);
        }
        game.font.draw(game.batch, "TILE HOPPER", screenWidth/2 - 100, screenHeight /2);
        game.font.draw(game.batch, "(press SPACE to play)", screenWidth/2 - 100, screenHeight /2 - 40);
        game.batch.end();

        //inputs
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            game.setScreen(new GameScreen(game, shouldMusicPlay));
            dispose();
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.M)){
            shouldMusicPlay = !shouldMusicPlay;
            System.out.println("Music played is: "+ shouldMusicPlay);
        }
    }

    @Override
    public void show() {

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

    }
}
