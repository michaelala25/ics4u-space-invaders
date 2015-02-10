import java.awt.Graphics;
import java.awt.Color;

public class AlienBullet extends Bullet{
	// A bullet shot by an Alien.
	
	public AlienBullet(final int x, final int y){
		super(x, y);
	}
	
	public int getDeltaY(){
		return 5;
	}
	
	public void draw(Graphics g){
		g.setColor(Color.gray);
		g.fillRect(getX() - 2, getY() - 15, 5, 15);
	}
}