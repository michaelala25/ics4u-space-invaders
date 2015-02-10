import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.Graphics;
import java.io.File;

public class Barrier{
	// A destructible barrier.
	
	private final BarrierPiece[][] pieces;
	
	public Barrier(final int x, final int y){
		/* Initialize the pieces of the barrier. Each is a 16 x 16 sprite
		 * represented by a BarrierPiece object. The Barrier is broken up
		 * into BarrierPieces so as to make destruction more manageable;
		 * only individual pieces can be damaged.
		 */
		final BarrierPiece[][] pieces = {
			{new BarrierPiece(x, y, 2),      new BarrierPiece(x + 16, y, 1),      new BarrierPiece(x + 32, y, 1),      new BarrierPiece(x + 48, y, 1),      new BarrierPiece(x + 64, y, 3)},
			{new BarrierPiece(x, y + 16, 1), new BarrierPiece(x + 16, y + 16, 1), new BarrierPiece(x + 32, y + 16, 1), new BarrierPiece(x + 48, y + 16, 1), new BarrierPiece(x + 64, y + 16, 1)},
			{new BarrierPiece(x, y + 32, 1), new BarrierPiece(x + 16, y + 32, 4), null,                                new BarrierPiece(x + 48, y + 32, 5), new BarrierPiece(x + 64, y + 32, 1)},
			{new BarrierPiece(x, y + 48, 1), null,						          null,							       null,						        new BarrierPiece(x + 64, y + 48, 1)}
		};
		this.pieces = pieces;
	}
	
	// Check if the barrier collides with a bullet and return whether or not an intersection occurred.
	public boolean collideWith(Bullet b){
		for (BarrierPiece[] row : pieces){
			for (BarrierPiece piece : row){
				if (piece == null || piece.isDestroyed())
					continue;
				if (piece.collideWith(b)){
					piece.dealDamage();
					return true;
				}
			}
		}
		return false;
	}
	
	public void draw(Graphics g){
		for (BarrierPiece[] row : pieces){
			for (BarrierPiece piece : row){
				if (piece == null || piece.isDestroyed())
					continue;
				piece.draw(g);
			}
		}
	}
	
}

class BarrierPiece{
	
	private static BufferedImage[] B1, B2, B3, B4, B5;
	
	static{
		try{
			B1 = new BufferedImage[] {
				ImageIO.read(new File("images/b11.png")),
				ImageIO.read(new File("images/b12.png"))
			};
			B2 = new BufferedImage[] {
				ImageIO.read(new File("images/b21.png")),
				ImageIO.read(new File("images/b22.png"))
			};
			B3 = new BufferedImage[] {
				ImageIO.read(new File("images/b31.png")),
				ImageIO.read(new File("images/b32.png"))
			};
			B4 = new BufferedImage[] {
				ImageIO.read(new File("images/b41.png")),
				ImageIO.read(new File("images/b42.png"))
			};
			B5 = new BufferedImage[] {
				ImageIO.read(new File("images/b51.png")),
				ImageIO.read(new File("images/b52.png"))
			};
		} catch (IOException e){
		}
	}
	
	private final int id, x, y;
	private int health = 2;
	
	private BufferedImage[] imgs;
	
	public BarrierPiece(final int x, final int y, final int id){
		this.x = x;
		this.y = y;
		this.id = id;
		BufferedImage[] imgs;
		switch(id){
			case 1:  imgs = B1;
					 break;
			case 2:  imgs = B2;
					 break;
			case 3:  imgs = B3;
					 break;
			case 4:  imgs = B4;
					 break;
			default: imgs = B5;
					 break;
		}
		this.imgs = imgs;
	}
	
	public void dealDamage(){
		health -= 1;
	}
	
	public boolean isDestroyed(){
		return health == 0;
	}
	
	public void draw(Graphics g){
		g.drawImage(imgs[2 - health], x, y, null);	
	}
	
	public boolean collideWith(Bullet b){
		final int bx = b.getX(), by = b.getY();
		return (x <= bx && x + 16 >= bx && y <= by && y + 16 >= by);
	}
	
}