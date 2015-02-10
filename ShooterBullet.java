import java.awt.Graphics;
import java.awt.Color;

public class ShooterBullet extends Bullet{
	// A bullet shot by the player.
	
	public ShooterBullet(final int x, final int y){
		super(x, y);
	}
	
	public int getDeltaY(){
		return -10;
	}
	
	public void draw(Graphics g){
		g.setColor(Color.white);
		g.fillRect(getX() - 2, getY(), 5, 15);
	}
	
}