import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.Graphics;
import java.io.File;

public class Shooter{
	
	private static final int SPEED = 3;
	
	public static BufferedImage SHOOTER, DEAD;
	private static final int MAX_DEATH_FRAME = 300;
	
	
	static{
		try{
			SHOOTER = ImageIO.read(new File("images/shooter.png"));
			DEAD    = ImageIO.read(new File("images/shooterdeath.png"));
		} catch (IOException e){
		}
	}
	
	private final GamePanel game;
	private int x, y;
	
	private int dying_frame = 0;
	
	public Shooter(final int x, final int y, final GamePanel game){
		this.x = x;
		this.y = y;
		this.game = game;	
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
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
		x += direction == Direction.LEFT ? -SPEED : SPEED;
		// NOTE: 858 is the right boundary of the screen - 52, the width of the shooter sprite.
		x = Math.max(50, Math.min(858, x));
	}
	
	public void draw(Graphics g){
		if (dying_frame > 1){
			g.drawImage(DEAD, x, y, null);
			dying_frame += 1;
		}
		else{
			g.drawImage(SHOOTER, x, y, null);
		}
	}
	
}