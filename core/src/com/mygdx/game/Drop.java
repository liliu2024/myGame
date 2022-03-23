package com.mygdx.game;
//package com.badlogic.drop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.graphics.Color;

import java.util.Iterator;
import java.util.Scanner;
import static com.badlogic.gdx.graphics.g3d.particles.ParticleChannels.Color;
public class Drop extends ApplicationAdapter {
	private Texture dropImage;
	private Texture bucketImage;
	private Texture pImage;
	private Texture buttonImage;
	private Texture heartImage;
	private Texture bombImage;
	private Texture bossImage;
	private Sound dropSound;
	private Music rainMusic;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Rectangle bucket;
	private Array<Rectangle> rainDrops;
	private Array<Rectangle> bombs;
	private Array<Rectangle> health;

	private long lastDropTime;
	private long lastBombDropTime;
	private int score = 0;
	private float bossX;
	private float bossY;
	private float bossXV;
	private float bossXA;
	private float bossYV;
	private float bombX = 0;
	private float bombY = 0;
	private BitmapFont font;
	private String player;
	private Box2D box;
	private boolean gameover = true;
	private boolean endScreen = false;
	private boolean drawBoss = false;

//	private Rectangle boss;

//code boss
	//

	@Override
	//happens once at the beginning
	public void create () {
		player = "bucket.png";
		font = new BitmapFont();

		dropImage = new Texture(Gdx.files.internal("Rainbow.png"));
		heartImage = new Texture(Gdx.files.internal("heart.png"));
		bucketImage = new Texture(Gdx.files.internal(player));
		buttonImage = new Texture(Gdx.files.internal("Button.png"));
		bombImage = new Texture(Gdx.files.internal("bomb2.png"));
		bossImage = new Texture(Gdx.files.internal("cat.png"));
//		pImage = new Texture(Gdx.files.internal("lollipop.com"));

		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		camera = new OrthographicCamera();



		batch = new SpriteBatch();


	}

	@Override
	//is called repeatedly
	public void render () {

		switchPlayer();
		//The arguments for ScreenUtils.clear(r, g, b, a) are the red, green,
		// blue and alpha component of that color, each within the range [0, 1].

		ScreenUtils.clear(0, 0, 0.2f, 1);
		if (Gdx.input.isTouched() && gameover) {
			setupGame();
			gameover = false;
		}




		camera.update();

//The first line tells the SpriteBatch to use the coordinate system specified by the camera.
// this is done with something called a matrix, to be more specific, a projection matrix.
		if(!gameover) {
			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			if(!gameover) {
				batch.draw(bucketImage, bucket.x, bucket.y);
				for (Rectangle rainDrop : rainDrops) {
					batch.draw(dropImage, rainDrop.x, rainDrop.y);
				}
				for (Rectangle bomb : bombs) {
					batch.draw(bombImage, bomb.x, bomb.y);
				}
				for (int i = 0; i < health.size; i++) {
					batch.draw(heartImage, 30 + 30 * (i), 390);
				}

				buttonDraw();
				font.draw(batch, "Score: " + Integer.toString(score), 40, 40);
				bossSequence();

			}
			batch.end();

			//hovers under mouse
			Vector3 pos = new Vector3();
			if(!gameover) {
				pos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			}
			camera.unproject(pos);
			bucket.x = pos.x - 64/2;


			//ensures the bucket stays within the panel
			if(bucket.y < 0)
				bucket.y = 0;
			if(bucket.y > 480-64)
				bucket.y = 480-64;

			//checks how much time has passed and spawns a new raindrop
			if(!gameover) {
				if(TimeUtils.nanoTime() - lastDropTime > (1000000000-1000000))
					spawnRainDrop();
				moveRain();

				if(TimeUtils.nanoTime() - lastBombDropTime > (1000000000-1000000))
					spawnBomb();
				moveBomb();

				if(health.size == 0){
					gameover = true;
					endScreen = true;
				}
			}

		} else {
			batch.begin();
			endScreen();
			batch.end();
		}

	}
//
//	@Override public void draw(Batch batch, float parentAlpha) {
//
//		batch.end();
//
//		if (shapeRenderer == null) {
//			shapeRenderer = new ShapeRenderer();
//		}
//
//		Gdx.gl.glEnable(GL20.GL_BLEND);
//		shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
//		shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
//		shapeRenderer.setColor(mColor.r, mColor.g, mColor.b, mColor.a * parentAlpha);
//
//		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
//		shapeRenderer.circle(getX() + getWidth()/2 , getY() + getHeight()/2 , Math.min(getWidth(),getHeight())/2 );
//		shapeRenderer.end();
//		Gdx.gl.glDisable(GL20.GL_BLEND);
//
//		batch.begin();
//	}

	public void bossSequence(){
		if(score == 3 && !drawBoss) {
			bossX = 400;
			bossY = 420;
			drawBoss = true;
		}
		if(drawBoss){
			batch.draw(bossImage, bossX, bossY);
			bossXA = (float)(Math.random()*5) - 2.5f;
			bossXV += (400 - bossX)/200;
			bossXV *= 0.99;
			bossX+=bossXV;
			bossXV+=bossXA;
		}

		//need to quit current moveBomb Sequence and change to a moveBomb
		// from which the x and y depend on each other
	}
	public void endScreen(){
		if (gameover && endScreen){
			ScreenUtils.clear(0, 0, 0, 1);
			font.draw(batch, "prom?", 380, 240);
			rainMusic.pause();
			if(Gdx.input.isTouched()) {
				gameover = false;
				endScreen = false;
			}

		}

	}
	public void moveRain(){
		for(Iterator<Rectangle> iter = rainDrops.iterator(); iter.hasNext();){
			Rectangle rainDrop = iter.next();
			rainDrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if(rainDrop.y < -64)
				iter.remove();
			if(rainDrop.overlaps(bucket)){
				dropSound.play();
				score++;
				iter.remove();
			}
		}
	}
	public void moveBomb(){
		for(Iterator<Rectangle> iter = bombs.iterator(); iter.hasNext();){
			Rectangle bomb = iter.next();
			bomb.y -= 200 * Gdx.graphics.getDeltaTime();
			if(bomb.y < -64)
				iter.remove();
			if(bomb.overlaps(bucket)){
//				dropSound.play();
				if(health.size>0)
					health.removeIndex(0);
				score++;
				iter.remove();
			}
		}
	}
	public void buttonPress(){
		int x = Gdx.input.getX();
		int y = Gdx.input.getY();
		if ( x < 650+128 && x > 650 && y < 480 && y > 450 ){



		}
	}
	public void setupGame(){
		score = 0;
		drawBoss = false;

		rainMusic.setLooping(true);
		rainMusic.play();
		Color pink = new Color(0xEC7878);
		box = new Box2D(50, 50, 50, 50, pink);
//		box.draw(batch, 100);
//The x/y coordinates of the bucket define the bottom left corner of the bucket,
// the origin for drawing is located in the bottom left corner of the screen.
		bucket = new Rectangle();
		bucket.x = 64;
		bucket.y = 20;
		bucket.width = 64;
		bucket.height = 64;

		//instantiates the rectangle array
		rainDrops = new Array<Rectangle>();
		spawnRainDrop();

		bombs = new Array<Rectangle>();
		spawnBomb();

		health = new Array<Rectangle>();
		for (int i = 0; i < 3; i++){
			health.add(new Rectangle());
		}
	}

	public void buttonDraw(){

		batch.draw(buttonImage, 650, 420);
		font.draw(batch,"MENU", 690, 450);
	}
	public void switchPlayer(){

		if(score > 0){
			player = "cat.png";
			bucketImage = new Texture(Gdx.files.internal(player));

		}
	}
	public void spawnRainDrop(){
		Rectangle rainDrop = new Rectangle();
		rainDrop.y = 480-64;
		rainDrop.x = MathUtils.random(0, 800-64); //why isn't it 480-20?
		rainDrop.width = 64;
		rainDrop.height = 64;
		rainDrops.add(rainDrop);
		lastDropTime = TimeUtils.nanoTime();


	}
	public void spawnBomb(){
		if(!drawBoss){
		Rectangle bomb = new Rectangle();
		bomb.y = 480-64;
		bomb.x = MathUtils.random(0, 800-64);
		bomb.width = 64;
		bomb.height = 64;
		bombs.add(bomb);
		lastBombDropTime = TimeUtils.nanoTime();
		}

		if(drawBoss){

				Rectangle bomb = new Rectangle();
				bomb.y = 480-64;
//				bombY = bomb.y;
				bomb.x = bossX;
//				bombX = bomb.x;
				bomb.width = 64;
				bomb.height = 64;
				bombs.add(bomb);
				lastBombDropTime = TimeUtils.nanoTime();
//				bossX = bomb.x;
//				bossY = bomb.y;

		}

	}

	@Override
	public void dispose() {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}
}
