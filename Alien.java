import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.Graphics;
import java.io.File;

public final class Alien{
	
	public enum AlienType{
		BOTTOM, MIDDLE, TOP; // Corresponds to the three different possible types of aliens.
	}
	
	public static BufferedImage ALIEN1_A, ALIEN1_B, ALIEN2_A, ALIEN2_B, ALIEN3_A, ALIEN3_B, DEAD;
	private static int MAX_DEATH_FRAME = 25;
	
	// Load all the pictures.
	static{
		try{
			ALIEN1_A = ImageIO.read(new File("images/alien1a.png"));
			ALIEN1_B = ImageIO.read(new File("images/alien1b.png"));
			ALIEN2_A = ImageIO.read(new File("images/alien2a.png"));
			ALIEN2_B = ImageIO.read(new File("images/alien2b.png"));
			ALIEN3_A = ImageIO.read(new File("images/alien3a.png"));
			ALIEN3_B = ImageIO.read(new File("images/alien3b.png"));
			DEAD     = ImageIO.read(new File("images/aliendeath.png"));
		} catch (IOException e){
		}
	}
	
	private static final int X_MOVEMENT = 8;
	private static final int Y_MOVEMENT = 20;
	
	private BufferedImage img1, img2;
	private int current_frame = 0;
	private int dying_frame = 0;
	private int x, y;
	
	private final GamePanel game;
	private final AlienType type;
	
	public Alien(final int x, final int y, final AlienType type, GamePanel game){
		this.x = x;
		this.y = y;
		
		switch (type){
			case BOTTOM: img1 = ALIEN3_A;
						 img2 = ALIEN3_B;
						 break;
			case MIDDLE: img1 = ALIEN2_A;
						 img2 = ALIEN2_B;
						 break;
			case TOP:	 img1 = ALIEN1_A;
						 img2 = ALIEN1_B;
						 break;
		}
		
		this.game = game;
		this.type = type;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public AlienType getType(){
		return type;
	}
	
	public void setDying(){
		dying_frame = 1;
	}
	
	public boolean isDying(){
		return dying_frame > 0;
	}
	
	public boolean isDead(){
		return dying_frame > MAX_DEATH_FRAME;
	}
	
	public void move(Direction direction){
		if (game.getCurrentFrame() % game.getGlobalAlienSpeed() == 0)
			x += (int)(direction == Direction.LEFT ? -X_MOVEMENT : X_MOVEMENT) * Math.min(
				Math.max((2 + game.getDifficulty())/game.getGlobalAlienSpeed(), 1), 1.2 + 0.1*game.getDifficulty());
	}
	
	public void moveDown(){
		y += Y_MOVEMENT;
	}
	
	public void fire(ArrayList<AlienBullet> bullets){
		bullets.add(new AlienBullet(x + 24, y + 32));
	}
	
	public void draw(Graphics g){
		if (dying_frame > 0){
			g.drawImage(DEAD, x, y, null);
			dying_frame += 1;
		}
		else{
			g.drawImage(current_frame == 0 ? img1 : img2, x, y, null);
		}
		
		// This ensures that we update the aliens faster and faster as
		// the global alien speed increases.
		if (game.getCurrentFrame() % game.getGlobalAlienSpeed() == 0)
			current_frame = 1 - current_frame;
	}
	
}