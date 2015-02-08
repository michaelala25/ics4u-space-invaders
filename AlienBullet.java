import java.awt.Graphics;
import java.awt.Color;

public class AlienBullet{
	
	private static final int Y_MOVEMENT = 5;
	
	private int x, y;
	
	public AlienBullet(final int x, final int y){
		this.x = x;
		this.y = y;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public void move(){
		y += Y_MOVEMENT;
	}
	
	public boolean collideWith(Shooter shooter){
		final int ax = shooter.getX(), ay = shooter.getY();
		return (ax < x && ax + 52 > x && ay < y && ay + 32 > y);
	}
	
	public void draw(Graphics g){
		g.setColor(Color.gray);
		g.fillRect(x - 2, y - 15, 5, 15);
	}
}