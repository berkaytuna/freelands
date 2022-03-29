package com.badlogic.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.IsometricTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.sun.tools.sjavac.Log;

import jdk.javadoc.internal.doclets.formats.html.markup.Table;

public class Drop implements ApplicationListener
{
	Texture img;
	TiledMap tiledMap;
	OrthographicCamera camera;
	TiledMapRenderer tiledMapRenderer;

	Stage stage;
	Touchpad touchpad;

	private static final int FRAME_COLS = 32, FRAME_ROWS = 8;

	Animation<TextureRegion> idleAnimRight;
	Animation<TextureRegion> walkAnimLeft;
	Animation<TextureRegion> walkAnimRight;
	Animation<TextureRegion> walkAnimUp;
	Animation<TextureRegion> walkAnimDown;
	Texture walkSheet;
	SpriteBatch spriteBatch;
	float stateTime;

	TextureRegion[] getFrames(TextureRegion[][] tmp,
							  int row_begin,
	                          int row_end,
							  int col_begin,
							  int col_end)
	{
		TextureRegion[] frames = new TextureRegion[(col_end - col_begin)
												   * (row_end - row_begin)];
		int index = 0;
		for (int i = row_begin; i < row_end; i++)
		{
			for (int j = col_begin; j < col_end; j++)
			{
				frames[index++] = tmp[i][j];
			}
		}

		return frames;
	}

	void drawFrame(Animation<TextureRegion> anim,
				   SpriteBatch spriteBatch,
				   float x,
				   float y,
				   float width,
				   float height)
	{
		TextureRegion currentFrame = anim.getKeyFrame(stateTime, true);
		spriteBatch.begin();
		spriteBatch.draw(currentFrame, x, y, width, height);
		spriteBatch.end();
	}

	@Override
	public void create()
	{
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);

		Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

		touchpad = new Touchpad(20, skin);
		touchpad.setBounds(15, 15, 350, 350);
		stage.addActor(touchpad);

		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, w / 4, h / 4);
		camera.update();
		tiledMap = new TmxMapLoader().load("untitled.tmx");
		tiledMapRenderer = new IsometricTiledMapRenderer(tiledMap);

		walkSheet = new Texture(Gdx.files.internal("clothes.png"));

		TextureRegion[][] tmp = TextureRegion.split(walkSheet,
													walkSheet.getWidth() / FRAME_COLS,
													walkSheet.getHeight() / FRAME_ROWS);

		TextureRegion[] idleFramesRight = getFrames(tmp, 4, 5, 0, 4);
		TextureRegion[] walkFramesLeft = getFrames(tmp, 0, 1, 4, 12);
		TextureRegion[] walkFramesRight = getFrames(tmp, 4, 5, 4, 12);
		TextureRegion[] walkFramesUp = getFrames(tmp, 2, 3, 4, 12);
		TextureRegion[] walkFramesDown = getFrames(tmp, 6, 7, 4, 12);

		idleAnimRight = new Animation<TextureRegion>(0.2f, idleFramesRight);
		walkAnimLeft = new Animation<TextureRegion>(0.07f, walkFramesLeft);
		walkAnimRight = new Animation<TextureRegion>(0.07f, walkFramesRight);
		walkAnimUp = new Animation<TextureRegion>(0.07f, walkFramesUp);
		walkAnimDown = new Animation<TextureRegion>(0.07f, walkFramesDown);

		spriteBatch = new SpriteBatch();
		stateTime = 0f;
	}

	@Override
	public void render()
	{
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stateTime += Gdx.graphics.getDeltaTime();

		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();

		float cameraSpeed = 4;
		if (touchpad.getKnobPercentX() < -0.5
			&& touchpad.getKnobPercentY() <= 0.5
			&& touchpad.getKnobPercentY() >= -0.5)
		{
			camera.translate(-1 * cameraSpeed, 0);
			drawFrame(walkAnimLeft, spriteBatch, 400, 400, 500, 500);
		}
		else if (touchpad.getKnobPercentX() > 0.5
				 && touchpad.getKnobPercentY() <= 0.5
				 && touchpad.getKnobPercentY() >= -0.5)
		{
			camera.translate(cameraSpeed, 0);
			drawFrame(walkAnimRight, spriteBatch, 400, 400, 500, 500);
		}
		else if (touchpad.getKnobPercentY() > 0.5
				 && touchpad.getKnobPercentX() <= 0.5
				 && touchpad.getKnobPercentX() >= -0.5)
		{
			camera.translate(0, cameraSpeed);
			drawFrame(walkAnimUp, spriteBatch, 400, 400, 500, 500);
		}
		else if (touchpad.getKnobPercentY() < -0.5
				&& touchpad.getKnobPercentX() <= 0.5
				&& touchpad.getKnobPercentX() >= -0.5)
		{
			camera.translate(0, -1 * cameraSpeed);
			drawFrame(walkAnimDown, spriteBatch, 400, 400, 500, 500);
		}
		else
		{
			drawFrame(idleAnimRight, spriteBatch, 400, 400, 500, 500);
		}

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	@Override
	public void resize(int a, int b)
	{
		stage.getViewport().update(a, b, true);
	}

	@Override
	public void pause()
	{
	}

	@Override
	public void resume()
	{
	}

	@Override
	public void dispose()
	{
		spriteBatch.dispose();
		walkSheet.dispose();
	}
}