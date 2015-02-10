/*
 * SPACE INVADERS
 * Michael Ala, 2/10/2015
 *
 * This is a clone of the 1978 smash hit Space Invaders. It attempts
 * to accurately emulate the original game (although slightly more
 * danmaku).
 *
 */
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;

import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.util.ArrayList;

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
		PLAYING, WINNING, DYING, INITIALIZING, GAMEOVER;
	}		

	private final Game game;
	
	// Game Related Fields //
	
	private static final String HIGH_SCORE_FILENAME = "highscore.txt"
		
	private static final double SPECIAL_ALIEN_SPAWN_CHANCE = 1.0/2000;
	
	// Global Game Variables
	
	// Current game state.
	private GameState current_state = GameState.INITIALIZING; 
	
	// The current game frame. Increments by 1 everytime the game is updated.
	private int current_frame = 0;
	
	// The global alien speed. Decreases as aliens die.
	private int global_alien_speed = 10;
	
	// Total number of aliens in the game.
	private int global_total_aliens = 55;
	
	// Total number of player lives.
	private int player_lives = 1;
	
	// The current wave of enemies.
	private int current_wave = 0;
	
	// The counter/timer for the INITIALIZING game state.
	private int init_counter = 0; 
		
	// The counter/timer for the WINNING game state.
	private int winning_counter = 0;
	
	// The counter/timer for the DYING game state.
	private int dying_counter = 0;
	
	// The counter/timer for the GAMEOVER game state.
	private int game_over_counter = 0;
	
	// The score for killing alien1.
	private int alien1_score = 50;
	
	// The score for killing alien2.
	private int alien2_score = 20;
	
	// The score for killing alien3.
	private int alien3_score = 10;
	
	// The score for killing the special alien.
	private int special_score = 150;
	
	// The total score.
	private int total_score = 0;
	
	// The highest score (in game or from file).
	private int high_score = 0;
	
	// The difficulty of the current wave.
	private int difficulty = 0;
	
	private Direction current_alien_direction = Direction.RIGHT;
	
	// Game Objects
	private Shooter shooter; // The player.
	private ArrayList<ArrayList<Alien>> aliens = new ArrayList<ArrayList<Alien>>(); // 2D array of
	private ArrayList<AlienBullet> alien_bullets = new ArrayList<AlienBullet>();
	private ArrayList<Barrier> barriers = new ArrayList<Barrier>();
	private ShooterBullet shooter_bullet = null;
	private SpecialAlien special_alien = null;
	
	private Font font = new Font("Courier New", Font.BOLD, 30);
	
	// Input Related Fields
	public boolean[] key_states = new boolean[KeyEvent.KEY_LAST + 1];
	
	// Initialize high score from file
	static{
		int previous_high_score;
		
		try{
			FileReader file_reader = new FileReader(HIGH_SCORE_FILENAME);
			
			BufferedReader buffered_reader = new BufferedReader(file_reader);
			
			previous_high_score = Integer.parseInt(buffered_reader.readLine());
			
			buffered_reader.close();
		} catch (FileNotFoundException exc){
			previous_high_score = 0;
		} catch (IOException | NumberFormatException exc){
			
		}
		
		high_score = previous_high_score;
	}
	
	public GamePanel(Game game){
		this.game = game;
		
		shooter = new Shooter(50, 680, this);

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
    
    // Convert an Alien to an int that determines the score received for killing it.
    public int getScoreFromAlien(Alien a){
    	switch (a.getType()){
    		case TOP:    return alien1_score;
    		case MIDDLE: return alien2_score;
    		case BOTTOM: return alien3_score;
    	}
    	return 0;
    }
    
    // Update the game.
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
    		case GAMEOVER:	   runGameOver();
    						   break;
    	}
    	current_frame += 1;
    }
    
    /* 
    Initialize the game world.
    
    This is called at the start of each wave (including upon first loading the game).
    */
    public void runInitialization(){
    	if (init_counter == 0){
    		
    		aliens.clear();
    		barriers.clear();
    		alien_bullets.clear();
    		
    		// Create Aliens
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
			
			// Create Barriers
			for (int i = 0; i < 4; i++){
				barriers.add(new Barrier(110 + 220*i, 550));
			}
			
			player_lives = Math.min(player_lives + 1, 5);
			current_wave += 1;
			difficulty += 1;
    	}
    	init_counter += 1;
    	if (init_counter > 1000){
    		current_state = GameState.PLAYING;
    		init_counter = 0;
    	}
    }
    
    /*
    Run the game.
    
    This handles all gameplay elements.
    */
    public void runGame(){
    	// Movement
    	if (key_states[KeyEvent.VK_A])
    		shooter.move(Direction.LEFT);
    	else if (key_states[KeyEvent.VK_D])
    		shooter.move(Direction.RIGHT);
    	
    	// Check if we need to make a new shot.
    	if (key_states[KeyEvent.VK_SPACE] && shooter_bullet == null)
    		// NOTE: 26 is half the width of the shooter sprite.
    		shooter_bullet = new ShooterBullet(shooter.getX() + 26, shooter.getY());
    	
    	// Move the player's bullet.
    	if (shooter_bullet != null){
    		shooter_bullet.move();
    		if (shooter_bullet.getY() < -5){
    			shooter_bullet = null;
    		}
    	}
    	
    	// Randomly spawn a special alien.
    	if (special_alien == null){
    		if (Math.random() < SPECIAL_ALIEN_SPAWN_CHANCE){
    			// 50% chance of spawning from either side.
    			if (Math.random() < 0.5)
    				special_alien = new SpecialAlien(-100, 50, 1);
    			else 
    				special_alien = new SpecialAlien(1060, 50, -1);
    			
    		}
    	}
    	
    	// Move the special alien (if applicable).
    	if (special_alien != null){
    		// If it's dying, it can't be interacted with.
    		if (!special_alien.isDying()){
    			special_alien.move();
    			if (shooter_bullet != null){
    				// Check for collision between player bullet and special alien.
    				if (special_alien.collideWith(shooter_bullet)){
    					special_alien.setDying();
    					total_score += special_score;
    					shooter_bullet = null;
	    			}
    			}
    		}
    		
    		// Check if it's dead or way off screen.
    		if (special_alien.isDead() || special_alien.getX() > 1800|| special_alien.getX() < -600){
    			special_alien = null;
    		}
    	}
    	
    	// The list of aliens to delete.
    	ArrayList<Alien> to_kill = new ArrayList<Alien>();
    	
    	// The min and max x-positions of the aliens. This is used to determine if the
    	// aliens need to change direction.
    	int min_alien_x = Integer.MAX_VALUE, max_alien_x = Integer.MIN_VALUE, crnt_x;
    	int total_aliens = 0;
    	
    	// By determining the min and max x positions of the aliens, we can
    	// determine when to change the direction of the aliens.
    	for (ArrayList<Alien> row : aliens){
    		for (Alien alien : row){
    			// Check for collision between player bullet and alien.
    			if (shooter_bullet != null && shooter_bullet.collideWith(alien) && !alien.isDying()){
    				shooter_bullet = null;
    				alien.setDying();
    				total_score += getScoreFromAlien(alien);
    			}
    			
    			// If an alien is dying, it can't be interacted with.
    			if (!alien.isDying()){
    				alien.move(current_alien_direction);
    				crnt_x = alien.getX();
    				min_alien_x = crnt_x < min_alien_x ? crnt_x : min_alien_x;
    				max_alien_x = crnt_x + 48 > max_alien_x ? crnt_x : max_alien_x;
    			
    				total_aliens += 1;
    			}
    			 
    			/*
    			 * If an alien is dead, add it to the list to be deleted.
    			 * NOTE: We don't delete an alien as soon as it gets hit
    			 * with a bullet. Instead we set it's internal state to
    			 * "dying", which tells paintComponent to draw it as an
    			 * explosion sprite, and tells runGame() to ignore it
    			 */   			
    			if (alien.isDead()){
    				to_kill.add(alien);
    			}
    		}
    	}
    	global_total_aliens = total_aliens;
    	
    	global_alien_speed = Math.max(total_aliens, 1);
    	
    	// Delete dead aliens.
    	for (Alien alien : to_kill){
    		for (ArrayList<Alien> row : aliens){
    			row.remove(alien);
    		}
    	}
    	
    	if (total_aliens > 5){
    		//global_alien_speed = total_aliens - 5;
    	}
    	
    	// Check if the player has won.
    	if (total_aliens == 0){
    		current_state = GameState.WINNING;
    		return;
    	}
    	
    	// Check if the direction of the aliens has to be reversed.
    	boolean move_down = false;
    	if (
    		// NOTE: 862 is the right boundary of the screen - 48, the width of the alien sprite.
    		(current_alien_direction == Direction.RIGHT && max_alien_x >= 862) ||
    		(current_alien_direction == Direction.LEFT  && min_alien_x <= 50)
    		) {
    		current_alien_direction = Direction.getOpposite(current_alien_direction);
    		move_down = true;
    	}
    	
    	// Move the aliens down (if applicable).
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
    	
    	// List of alien bullets that may have gone off screen.
    	ArrayList<AlienBullet> dead_bullets = new ArrayList<AlienBullet>();
    	
    	// Check if any alien bullets have collided with the player.
    	for (AlienBullet bullet : alien_bullets){
    		bullet.move();
    		if (bullet.collideWith(shooter)){
    			shooter.setDead();
    			current_state = GameState.DYING;
    		}
    		if (bullet.getY() > 800)
    			dead_bullets.add(bullet);
    	}
    	
    	// Check if any of the bullets have collided with any of the barriers.
    	for (Barrier barrier : barriers){
    		for (AlienBullet bullet : alien_bullets){
    			if (barrier.collideWith(bullet))
    				dead_bullets.add(bullet);
    		}
    		if (shooter_bullet != null && barrier.collideWith(shooter_bullet))
    			shooter_bullet = null;
    	}
    	
    	// Delete old bullets.
    	for (AlienBullet bullet : dead_bullets){
    		alien_bullets.remove(bullet);
    	}
    }
    
    /*
     * Run the winning screen.
     */
    public void runWinning(){
    	winning_counter += 1;
    	
    	if (winning_counter > 700){
    		current_state = GameState.INITIALIZING;
			
			alien1_score += 5;
			alien2_score += 2;
			alien3_score += 1;
			special_score += 50;
			
			winning_counter = 0;
			
			return;
    	}
    }
    
    /*
     * Run the dying script.
     *
     * NOTE: This is not the same as GAMEOVER.
     */
    public void runDying(){
    	if (dying_counter == 0){
    		alien_bullets.clear();
    		player_lives -= 1;
    	}
    	dying_counter += 1;
    	
    	if (dying_counter > 150){
    		dying_counter = 0;
    		if (player_lives >= 0){
    			current_state = GameState.PLAYING;
    			shooter.revive();	
    		} else 
    			current_state = GameState.GAMEOVER;
    	}
    }
    
    /*
     * Run the game over screen.
     */
    public void runGameOver(){
    	game_over_counter += 1;
    	if (total_score > high_score){
    		high_score = total_score;
    	}
    	
    	// The player wishes to play again.
    	if (game_over_counter > 300 && key_states[KeyEvent.VK_ENTER]){
    		// Reset everything.
    		game_over_counter = 0;
    		current_state = GameState.INITIALIZING;
    		shooter.revive();
    		current_wave = 0;
    		difficulty = 0;
    		alien1_score = 50;
    		alien2_score = 20;
    		alien3_score = 10;
    		special_score = 150;
    		player_lives = 1;
    		total_score = 0;
    	}
    	
    	// The player realizes they have better things to do with their life than play a space invaders clone.
    	if (game_over_counter > 300 && key_states[KeyEvent.VK_ESCAPE]){
    		game_over_counter = 0;
    		try{
    			FileWriter file_writer = new FileWriter(HIGH_SCORE_FILENAME);
    			
    			BufferedWriter buffered_writer = new BufferedWriter(file_writer);
    			
    			buffered_writer.write(Integer.toString(high_score));
    			
    			buffered_writer.close();
    		} catch (FileNotFoundException | IOException){
    		}
    		System.exit(0);
    	}
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
		// Background
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		// GUI
		g.setFont(font);
		g.setColor(Color.white);
		g.drawString("Score:" + total_score, 20, 30);
		g.drawString("High Score:" + high_score, 240, 30);
		g.drawString("Lives: ", 520, 30);
		for (int i = 0; i < player_lives; i++){
			g.drawImage(Shooter.SHOOTER, 628 + 60*i, 3, null);
		}
		
		// GameState specific drawing.
		switch (current_state){
			case INITIALIZING: paintInitialization(g);
							   break;
			case PLAYING:	   paintPlaying(g);
							   break;
			case WINNING:	   paintWinning(g);
							   break;
			case DYING:		   paintDying(g);
							   break;
			case GAMEOVER:	   paintGameOver(g);
							   break;
		}
	}
	
	/*
	 * Draw the initialization sequence.
	 */
	public void paintInitialization(Graphics g){
		g.setFont(font);
		g.setColor(Color.white);
		g.drawString("Wave " + current_wave, 400, 200);
		g.drawString("Enemies", 390, 300);
		g.drawString("-------", 390, 320);
		
		// Display the different enemies and their associated point values.
		
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
		if (init_counter > 400)
			g.drawImage(SpecialAlien.ALIEN, 320, 490, null);
		if (init_counter > 450){
			g.setColor(Color.red);
			g.drawString(" - " + special_score + " Points", 436, 515);
		}
		
		// Display the controls.
		if (init_counter > 700){
			g.setColor(Color.white);
			g.drawString("Controls", 390, 580);
			g.drawString("--------", 390, 600);
			g.drawString("A - Left, D - Right, SPACE - Shoot", 160, 620);
		}
	}
	
	/*
	 * Draw the game.
	 */
	public void paintPlaying(Graphics g){
		shooter.draw(g);
		
		for (ArrayList<Alien> row : aliens){
			for (Alien alien : row){
				alien.draw(g);
			}			
		}
		
		if (shooter_bullet != null)
			shooter_bullet.draw(g);
			
		if (special_alien != null)
			special_alien.draw(g);
		
		for (AlienBullet bullet : alien_bullets){
			bullet.draw(g);
		}
		
		g.setFont(font);
		g.setColor(Color.white);
		g.drawString("Wave " + current_wave, 20, 760);
		
		for (Barrier b : barriers){
			b.draw(g);
		}
	}
	
	/*
	 * Draw the winning screen.
	 */
	public void paintWinning(Graphics g){
		g.setFont(font);
		g.setColor(Color.white);
		// This repeated substringing of the message is what gives it the slow scroll.
		g.drawString("You win this time Earthling".substring(0, Math.min(winning_counter/10, 27)), 240, 100);
		if (winning_counter > 500 && (winning_counter/50 % 2 == 0))
			g.drawString("Wave " + (current_wave + 1), 400, 200);
		shooter.draw(g);
		for (Barrier b : barriers){
			b.draw(g);
		}
	}
	
	/*
	 * Draw the dying sequence.
	 */
	public void paintDying(Graphics g){
		for (ArrayList<Alien> row : aliens){
			for (Alien alien : row){
				alien.draw(g);
			}			
		}
		
		if (shooter_bullet != null)
			shooter_bullet.draw(g);
			
		if (special_alien != null)
			special_alien.draw(g);
		
		for (AlienBullet bullet : alien_bullets){
			bullet.draw(g);
		}
		
		g.setFont(font);
		g.setColor(Color.white);
		g.drawString("Wave " + current_wave, 20, 760);
		
		for (Barrier b : barriers){
			b.draw(g);
		}
		if (dying_counter/10 % 2 == 0){
			shooter.draw(g);
		}
	}
	
	/*
	 * Draw the game over screen.
	 */
	public void paintGameOver(Graphics g){
		g.setFont(font);
		g.setColor(Color.white);
		g.drawString("Game Over", 390, 300);
		if (game_over_counter > 50 && total_score >= high_score)
			g.drawString("New High Score!", 340, 330);
		if (game_over_counter > 200){
			g.drawString("Continue?", 390, 400);
			g.drawString("(ENTER - Yes, ESC - No)", 270, 430);
		}
	}
}

