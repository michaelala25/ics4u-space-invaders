import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;

@SuppressWarnings("serial")
public final class Game extends JFrame implements ActionListener{
	
	private final javax.swing.Timer clock;
	private final GamePanel game;

	public Game(){
		super("Space Invaders");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setSize(960, 800);
		
		clock = new javax.swing.Timer(10, this);
		game  = new GamePanel(this);
		add(game);

		setResizable(false);
		setVisible(true);
	}
	
	public void start(){
		clock.start();
	}

	public void actionPerformed(ActionEvent evt){
		game.update();
		game.repaint();
	}

	public static void main(String[] args){
		new Game();
	}
}

final class GamePanel extends JPanel implements KeyListener{

	// Possible game states.	
	public enum GameState{
		PLAYING, WINNING, DYING, INITIALIZING;
	}		

	private final Game game;
	
	// Game Related Fields
	
	// Global Game Variables
	private GameState current_state = GameState.INITIALIZING; // Current game state.
	private int current_frame = 0; // The current game frame. Increments by 1 everytime the game is updated.
	private int global_alien_speed = 10; // The global alien speed. Decreases as aliens die.
	private int global_total_aliens = 55; // Total number of aliens in the game.
	private int player_lives = 2; // Total number of player lives.
	private int current_wave = 1; // The current wave of enemies.
	private int init_counter = 0; // The counter/timer for the INITIALIZING game state.
	private int winning_counter = 0; // The counter/timer for the WINNING game state.
	private int dying_counter = 0; // The counter/timer for the DYING game state.
	private int alien1_score = 50; // The score for killing alien1.
	private int alien2_score = 20; // The score for killing alien2.
	private int alien3_score = 10; // The score for killing alien3.
	private int special_score = 150; // The score for killing the special alien.
	private int total_score = 0; // The total score.
	private int high_score = 0; // The highest score so far.
	private int difficulty = 0; // The difficulty of the current wave.
	
	private Direction current_alien_direction = Direction.RIGHT;
	
	// Game Objects
	private Shooter shooter;
	private ArrayList<ArrayList<Alien>> aliens = new ArrayList<ArrayList<Alien>>();
	private ArrayList<AlienBullet> alien_bullets = new ArrayList<AlienBullet>();
	
	private ShooterBullet shooter_bullet = null;
	
	private Font font = new Font("Courier New", Font.BOLD, 30);
	
	// Input Related Fields
	public boolean[] key_states = new boolean[KeyEvent.KEY_LAST + 1];
	
	static{
		
	}
	
	public GamePanel(Game game){
		this.game = game;
		
		// Game Object Initialization
		shooter = new Shooter(50, 700, this);
		
		Alien.AlienType type;
		ArrayList<Alien> current_row;
		for (int i = 0; i < 5; i++){
			current_row = new ArrayList<Alien>();
			if (i == 0)
				type = Alien.AlienType.TOP;
			else if (i == 1 || i == 2)
				type = Alien.AlienType.MIDDLE;
			else
				type = Alien.AlienType.BOTTOM;
			
			for (int j = 0; j < 11; j++){
				current_row.add(new Alien(50 + j*64, 50 + i*64, type, this));
			}
			aliens.add(current_row);
		}
		
		// Misc Swing/AWT Related Bull

		addKeyListener(this);
		setSize(960, 800);
	}
	
	 public void addNotify() {
        super.addNotify();
        requestFocus();
        game.start();
    }
    
    public int getTotalAlienCount(){
    	return global_total_aliens;
    }
    
    public int getCurrentFrame(){
    	return current_frame;
    }
    
    public int getGlobalAlienSpeed(){
    	return global_alien_speed;
    }
    
    public int getDifficulty(){
    	return difficulty;
    }
    
    public int getScoreFromAlien(Alien a){
    	switch (a.getType()){
    		case TOP:    return alien1_score;
    		case MIDDLE: return alien2_score;
    		case BOTTOM: return alien3_score;
    	}
    	return 0;
    }
    
    public void update(){
    	switch (current_state){
    		case INITIALIZING: runInitialization();
    						   break;
    		case PLAYING:	   runGame();
    						   break;
    		case WINNING:	   runWinning();
    						   break;
    		case DYING:		   runDying();
    						   break;
    	}
    	current_frame += 1;
    }
    
    public void runInitialization(){
    	init_counter += 1;
    	if (init_counter > 700){
    		current_state = GameState.PLAYING;
    		init_counter = 0;
    	}
    }
    
    public void runGame(){
    	if (key_states[KeyEvent.VK_A])
    		shooter.move(Direction.LEFT);
    	else if (key_states[KeyEvent.VK_D])
    		shooter.move(Direction.RIGHT);
    		
    	if (key_states[KeyEvent.VK_SPACE] && shooter_bullet == null)
    		// NOTE: 26 is half the width of the shooter sprite.
    		shooter_bullet = new ShooterBullet(shooter.getX() + 26, shooter.getY());
    		
    	if (shooter_bullet != null){
    		shooter_bullet.move();
    		if (shooter_bullet.getY() < -5){
    			shooter_bullet = null;
    		}
    	}
    	
    	ArrayList<Alien> to_kill = new ArrayList<Alien>();
    	int min_alien_x = Integer.MAX_VALUE, max_alien_x = Integer.MIN_VALUE, crnt_x;
    	int total_aliens = 0;
    	
    	// By determining the min and max x positions of the aliens, we can
    	// determine when to change the direction of the aliens.
    	for (ArrayList<Alien> row : aliens){
    		for (Alien alien : row){
    			if (shooter_bullet != null && shooter_bullet.collideWith(alien) && !alien.isDying()){
    				shooter_bullet = null;
    				alien.setDying();
    				total_score += getScoreFromAlien(alien);
    			}
    			
    			if (!alien.isDying()){
    				alien.move(current_alien_direction);
    				crnt_x = alien.getX();
    				min_alien_x = crnt_x < min_alien_x ? crnt_x : min_alien_x;
    				max_alien_x = crnt_x + 48 > max_alien_x ? crnt_x : max_alien_x;
    			
    				total_aliens += 1;
    			}
    			    			
    			if (alien.isDead()){
    				to_kill.add(alien);
    			}
    		}
    	}
    	global_total_aliens = total_aliens;
    	
    	global_alien_speed = Math.max(total_aliens, 1);
    	
    	for (Alien alien : to_kill){
    		for (ArrayList<Alien> row : aliens){
    			row.remove(alien);
    		}
    	}
    	
    	if (total_aliens > 5){
    		//global_alien_speed = total_aliens - 5;
    	}
    	
    	if (total_aliens == 0){
    		current_state = GameState.WINNING;
    		return;
    	}
    	
    	boolean move_down = false;
    	if (
    		// NOTE: 862 is the right boundary of the screen - 48, the width of the alien sprite.
    		(current_alien_direction == Direction.RIGHT && max_alien_x >= 862) ||
    		(current_alien_direction == Direction.LEFT  && min_alien_x <= 50)
    		) {
    		current_alien_direction = Direction.getOpposite(current_alien_direction);
    		move_down = true;
    	}
    	
    	for (ArrayList<Alien> row : aliens){
    		for (Alien alien : row){
    			if (alien.isDying())
    				continue;
    			if (move_down)
    				alien.moveDown();
    			if (Math.random() < 1.0/(Math.pow(total_aliens, Math.max(2 - 0.15*difficulty, 0.1)) + 100)){
    				alien.fire(alien_bullets);
    			}
    		}
    	}
    	
    	ArrayList<AlienBullet> dead_bullets = new ArrayList<AlienBullet>();
    	
    	for (AlienBullet bullet : alien_bullets){
    		bullet.move();
    		if (bullet.collideWith(shooter)){
    			shooter.setDying();
    			current_state = GameState.DYING;
    		}
    		if (bullet.getY() > 800)
    			dead_bullets.add(bullet);
    	}
    	
    	for (AlienBullet bullet : dead_bullets){
    		alien_bullets.remove(bullet);
    	}
    }
    
    public void runWinning(){
    	winning_counter += 1;
    	
    	if (winning_counter > 700){
    		current_state = GameState.INITIALIZING;
    		
    		Alien.AlienType type;
			ArrayList<Alien> current_row;
			
			for (int i = 0; i < 5; i++){
				current_row = new ArrayList<Alien>();
				if (i == 0)
					type = Alien.AlienType.TOP;
				else if (i == 1 || i == 2)
					type = Alien.AlienType.MIDDLE;
				else
					type = Alien.AlienType.BOTTOM;
				
				for (int j = 0; j < 11; j++){
					current_row.add(new Alien(50 + j*64, 50 + i*64, type, this));
				}
				aliens.add(current_row);
			}
			
			alien_bullets.clear();
			
			player_lives = Math.min(player_lives + 1, 5);
			current_wave += 1;
			difficulty += 1;
			
			alien1_score += 5;
			alien2_score += 2;
			alien3_score += 1;
			
			winning_counter = 0;
			
			return;
    	}
    }
    
    public void runDying(){
    	
    }
	
	public void keyPressed(KeyEvent evt) {
        key_states[evt.getKeyCode()] = true;
    }
    
    public void keyReleased(KeyEvent evt) {
        key_states[evt.getKeyCode()] = false;
    }
    
    public void keyTyped(KeyEvent e){
    }
	
	@Override
	public void paintComponent(Graphics g){
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		g.setFont(font);
		g.setColor(Color.white);
		g.drawString("Score:" + total_score, 20, 30);
		g.drawString("High Score:" + high_score, 240, 30);
		g.drawString("Lives: ", 520, 30);
		for (int i = 0; i < player_lives; i++){
			g.drawImage(Shooter.SHOOTER, 628 + 60*i, 3, null);
		}
		
		switch (current_state){
			case INITIALIZING: paintInitialization(g);
							   break;
			case PLAYING:	   paintPlaying(g);
							   break;
			case WINNING:	   paintWinning(g);
							   break;
			case DYING:		   paintDying(g);
							   break;
		}
	}
	
	public void paintInitialization(Graphics g){
		g.setFont(font);
		g.setColor(Color.white);
		g.drawString("Wave " + current_wave, 400, 200);
		g.drawString("Enemies", 390, 300);
		g.drawString("-------", 390, 320);
		if (init_counter > 100)
			g.drawImage(Alien.ALIEN3_A, 340, 340, null);
		if (init_counter > 150)
			g.drawString(" - " + alien3_score + " Points", 436, 365);
		if (init_counter > 200)
			g.drawImage(Alien.ALIEN2_A, 340, 390, null);
		if (init_counter > 250)
			g.drawString(" - " + alien2_score + " Points", 436, 415);
		if (init_counter > 300)
			g.drawImage(Alien.ALIEN1_A, 340, 440, null);
		if (init_counter > 350)
			g.drawString(" - " + alien1_score + " Points", 436, 465);
	}
	
	public void paintPlaying(Graphics g){
		shooter.draw(g);
		
		for (ArrayList<Alien> row : aliens){
			for (Alien alien : row){
				alien.draw(g);
			}			
		}
		
		if (shooter_bullet != null)
			shooter_bullet.draw(g);
		
		for (AlienBullet bullet : alien_bullets){
			bullet.draw(g);
		}
		
		g.setFont(font);
		g.setColor(Color.white);
		g.drawString("Wave " + current_wave, 20, 760);
	}
	
	public void paintWinning(Graphics g){
		g.setFont(font);
		g.setColor(Color.white);
		g.drawString("You win this time Earthling".substring(0, Math.min(winning_counter/10, 27)), 240, 100);
		if (winning_counter > 500 && (winning_counter/50 % 2 == 0))
			g.drawString("Wave " + (current_wave + 1), 400, 200);
		shooter.draw(g);
	}
	
	public void paintDying(Graphics g){
		
	}
}

