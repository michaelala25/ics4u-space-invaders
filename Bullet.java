import java.awt.Graphics;

public abstract class Bullet{

	private int x, y;
	
	public Bullet(final int x, final int y){
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
		y += getDeltaY();
	}
	
	public abstract int getDeltaY();
	
	public boolean collideWith(Alien alien){
		final int ax = alien.getX(), ay = alien.getY();
		return (ax < x && ax + 48 > x && ay < y && ay + 48 > y);
	}
	
	public boolean collideWith(Shooter shooter){
		final int ax = shooter.getX(), ay = shooter.getY();
		return (ax + 10 < x && ax + 42 > x && ay < y && ay + 32 > y);
	}
	
	public abstract void draw(Graphics g);
	
}