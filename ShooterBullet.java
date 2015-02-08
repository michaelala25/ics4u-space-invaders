import java.awt.Graphics;
import java.awt.Color;

public class ShooterBullet{
	
	private final static int Y_MOVEMENT = 10;
	
	private int x, y;
	
	public ShooterBullet(final int x, final int y){
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
		y -= Y_MOVEMENT;
	}
	
	public boolean collideWith(Alien alien){
		final int ax = alien.getX(), ay = alien.getY();
		return (ax < x && ax + 48 > x && ay < y && ay + 48 > y);
	}
	
	public void draw(Graphics g){
		g.setColor(Color.white);
		g.fillRect(x - 2, y, 5, 15);
	}
	
}