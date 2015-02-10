import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.Graphics;
import java.io.File;

public class SpecialAlien{
	// The special red alien that periodically flies overhead.
	
	public static BufferedImage ALIEN, DEAD;
	private static final int MAX_DEATH_FRAME = 100;
	
	static{
		try{
			ALIEN = ImageIO.read(new File("images/alien4.png"));
			DEAD = ImageIO.read(new File("images/alien4death.png"));
		} catch (IOException e){
		}
	}
	
	private static int SPEED = 3;
	private int x, y, direction;
	
	private int dying_frame = 0;
	
	public SpecialAlien(final int x, final int y, final int direction){
		this.x = x;
		this.y = y;
		this.direction = direction;
	}
	
	public void move(){
		x += direction*SPEED;
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
	
	public boolean collideWith(Bullet b){
		final int bx = b.getX(), by = b.getY();
		return (x <= bx && bx <= x + 96 && y <= by && by <= y + 42);
	}
	
	public void draw(Graphics g){
		if (dying_frame > 0){
			g.drawImage(DEAD, x, y, null);
			dying_frame += 1;
		}
		else{
			g.drawImage(ALIEN, x, y, null);
		}
	}
}