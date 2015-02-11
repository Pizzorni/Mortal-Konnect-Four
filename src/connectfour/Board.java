package connectfour;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import sun.audio.*;

/*
 *  WARNING: CONTAINS AUDIO! Please adjust volume/plug in headphones accordingly.
 * 
 * 
 * Konnect Four by Giorgio Pizzorni (Mortal Kombat + Connect Four = Konnect Four).
 * 
 * In terms of extra credit, I believe I have satisfied the following:
 * 1. Personalization, 2. Multiple Games (WITHOUT alternating turns). 3. Multiple matches. 5. Undo.
 * 6. Multiple Undo. 7. Multiple redo.
 * 
 * I've added a lot more then that, since I had quite a bit of fun with this. Other things that could
 * potentially warrant more extra credit, although I don't expect them to include:
 * 
 * 1. Music 2. Theme (includes easter eggs!) 3. Rematch button 4. Next-Gen Graphics 5. Humor.
 * 
 * Anyways, I hope I didn't overdo it with the images and sounds. If so, let me know and I can send a water-downed version.
 * 
 * 
 * One side note: I apologize if the code is messy. As I decided to have fun with this, I worked on it over a 2 week period so
 * I was inconsistent with coding style and variables at time. I'm not used to writing a 600 line program, so I apologize for
 * any lack of elegance/cleanliness.
 * 
 * 
 * Easter Eggs: 1. Leave player names null. Thematically appropriate hardwiring takes over. 2. Click "No" when asked if you wish to
 * play Connect Four.
 * 
 * Tips/Advice always appreciated. Was unable to add animations because I was dead-set on using my sprites, and LayeredPane was being incredibly unfriendly.
 * Not enough time to figure it out due to finals.
 * 
 * Lastly, I made all sprites. Downloaded gimp and fiddled around till gif's worked. Unfortunately, I'm not the very gifted individual who created the song.
 * 
 */



public class Board extends JFrame implements ActionListener {   //Workhorse. GUI and Game Logic.

	//Eclipse was giving me warnings. Looked it up and was recommended to serialize. Chose 42 because it is both the answer to everything, and it is in the documentation.
	static final long serialVersionUID = 42L; 

	//Declaration of Variables
	int WIDTH = 7; 				//	Dimensional Constants
	int HEIGHT = 7; 			// Dimensional Constants
	boolean gameover = false;  //Decides whether a match is over
	boolean matchover = false; //Decides whether a game is over. Game < Match. (ie. One match can contain 7 games)
	int matchnum;  				//number of games needed to win Best of X or Single match.
	int matchinput; 			//Used to convert JOptionPane input to a match num by using a switch statement.
	int scorep1 = 0; 			//Initial game score for Player 1.
	int scorep2 = 0; 			//Initial game score for player 2.
	int turn = 0;				//Checks for draws and selects player.
	int move; 					//sends user input to different methods
	int player;					//Represents player. Based on turn mod 2. 0 gets reassigned to 1 and 1 gets reassigned to make code easier to rea.
	int x,y;					// x,y coordinates of boards.
	JButton[][] pieces; 		//Array of pieces. 
	ImageIcon player1, player2, blank, player1win, player2win, space; //Images for playing pieces, blanks, and winning pieces.			
	JButton btn1, btn2, btn3, btn4, btn5, btn6, btn0, newgame, newmatch, undobtn, redobtn, rematchbtn, playbtn, stopbtn; //buttons for all sorts of actions
	int win = 4; //Win condition. Number of pieces in a row necessary.
	int numrow = 7;				//In theory replaced by WIDTH but kept for the sake of not re-writing old game logic.
	int numcol = 7;
	int draw = numrow * numcol; //Determines number of moves needed to be played for there to be a draw.
	char charboard[][];			//Game logic is done on hidden charboard. Easier to compare chars than image icons of Buttons.
	JLabel directions = new JLabel(); //used to set player directions
	JLabel score = new JLabel();  //Displayes score
	JLabel spacer = new JLabel(); //JLabel used to space North Panel of top level container
	JPanel panel, scorepanel, southpanel, panelception; //panels
	String player1name, player2name; //Player names
	JOptionPane input; 
	Stack<Integer> oldmoves = new Stack<Integer>(); //Keeps track of all moves played.
	Stack<Integer> undone = new Stack<Integer>();  //Keeps track of moves that have been undone.
	AudioStream as; //Plays song
	InputStream in; //Reads file input





	public Board() throws IOException { //Throws IO exception for audio

		// Basic JFrame Setup
		super("Connect Four"); //equivalent to setTitle("Connect Four")

		charboard = new char[numrow][numcol]; //Makes 7x7 playing char-board
		for (int j = 0; j < numrow; j++) {
			for (int k = 0; k < numcol; k++) {
				charboard[j][k] = '.'; //Sets empty spaces to be '.'. Board Initialized.
			}
		}

		setDefaultCloseOperation(EXIT_ON_CLOSE); //boiler plate

		// Set up the image icons for the different pieces
		player1 = new ImageIcon(getClass().getClassLoader().getResource("red.png"));
		player2 = new ImageIcon(getClass().getClassLoader().getResource("blue.png"));
		blank = new ImageIcon(getClass().getClassLoader().getResource("blank.png"));
		player1win = new ImageIcon(getClass().getClassLoader().getResource("redwin.gif"));
		player2win = new ImageIcon(getClass().getClassLoader().getResource("bluewin.gif"));
		space = new ImageIcon(getClass().getClassLoader().getResource("space.gif"));




		pieces = new JButton[WIDTH][HEIGHT]; // Array of Buttons ie. playing pieces

		// Preparing to add stuff to the JFrame

		Container c = getContentPane(); //Assigns our pane to a variable.
		c.setLayout(new BorderLayout()); //Sets border layout

		JPanel panel = new JPanel(new GridLayout(WIDTH, HEIGHT, 0, 0)); //Makes 7x7 grid panel for game pieces. Spacing set to 0 so no non-clickable space inbetween rows or columns.
		panel.setBackground(Color.YELLOW); //sets "board" color


		// Initialize the pieces array and fills JPanel with "blank" JButtons
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH; j++) {
				pieces[j][i] = new JButton(blank); //Initialization
				pieces[j][i].addActionListener(this); //so that one can click on columns

				//next 5 lines make buttons not look like buttons, essentially. 
				pieces[j][i].setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));  
				pieces[j][i].setBorderPainted(false);
				pieces[j][i].setContentAreaFilled(true);
				pieces[j][i].setFocusPainted(false);
				pieces[j][i].setFocusable(false);

				panel.add(pieces[j][i]); //adds buttons to panel effectively making an "empty" board
			}
		}

		JPanel textinfo = new JPanel( new FlowLayout()); //Layout for textual information
		JPanel scorepanel = new JPanel(new FlowLayout()); //Layout for score
		JPanel southpanel = new JPanel(new GridLayout(2,1,5,5)); //Panel for score and text info



		// Panel to offset spacing on the upper left corner and to provide invisble clickable buttons above columns

		panelception = new JPanel(new BorderLayout()); //creative variable names
		panelception.setBackground(Color.YELLOW); //match original Panel
		JLabel spacer = new JLabel(space); //unlike other labels, not used globally, so intialized here
		panelception.add(spacer,BorderLayout.WEST);


		JPanel labels = new JPanel(new GridLayout(1, WIDTH, 0, 0)); //Originally called labels as it labeled columns. Now just for input. 0 spacing to match up with board.

		/*
		 * Intializaton of Buttons above columns. Invisble and clickable. Gives
		 * use the choice of clicking on or above a column
		 */
		btn0 = new JButton();
		btn0.setOpaque(false);
		btn0.setContentAreaFilled(false);
		btn0.setBorderPainted(false);
		btn0.addActionListener(this);
		btn0.setPreferredSize(new Dimension(50,50));
		labels.add(btn0);
		btn0.setBackground(Color.YELLOW);

		btn1 = new JButton();
		btn1.setOpaque(false);
		btn1.setContentAreaFilled(false);
		btn1.setBorderPainted(false);
		btn1.addActionListener(this);
		btn1.setPreferredSize(new Dimension(50,50));
		labels.add(btn1);
		btn1.setBackground(Color.YELLOW);

		btn2 = new JButton();
		btn2.setOpaque(false);
		btn2.setContentAreaFilled(false);
		btn2.setBorderPainted(false);
		btn2.addActionListener(this);
		btn2.setPreferredSize(new Dimension(50,50));
		labels.add(btn2);
		btn2.setBackground(Color.YELLOW);

		btn3 = new JButton();
		btn3.setOpaque(false);
		btn3.setContentAreaFilled(false);
		btn3.setBorderPainted(false);
		btn3.addActionListener(this);
		btn3.setPreferredSize(new Dimension (50,50));
		labels.add(btn3);
		btn3.setBackground(Color.YELLOW);

		btn4 = new JButton();
		btn4.setOpaque(false);
		btn4.setContentAreaFilled(false);
		btn4.setBorderPainted(false);
		btn4.addActionListener(this);
		btn4.setPreferredSize(new Dimension (50,50));
		labels.add(btn4);
		btn4.setBackground(Color.YELLOW);

		btn5 = new JButton();
		btn5.setOpaque(false);
		btn5.setContentAreaFilled(false);
		btn5.setBorderPainted(false);
		btn5.addActionListener(this);
		btn5.setPreferredSize(new Dimension (50,50));
		labels.add(btn5);
		btn5.setBackground(Color.YELLOW);

		btn6 = new JButton();
		btn6.setOpaque(false);
		btn6.setContentAreaFilled(false);
		btn6.setBorderPainted(false);
		btn6.addActionListener(this);
		btn6.setPreferredSize(new Dimension(50,50));
		labels.add(btn6);
		btn6.setBackground(Color.YELLOW);

		labels.setBackground(Color.YELLOW); //to match panel
		panelception.add(labels,BorderLayout.EAST); //Adds buttons to panelception. 



		/*
		 * Initialization of extra buttons. Match/game controls, undo/redo, and audio control.
		 */
		JPanel extra = new JPanel( new GridLayout(HEIGHT,1,5,5)); //Panel for extra buttons
		newgame = new JButton("New Game");
		newmatch = new JButton("New Match");
		undobtn = new JButton("Undo");
		redobtn = new JButton("Redo");
		rematchbtn = new JButton("Rematch");
		playbtn = new JButton("Play Music");
		stopbtn = new JButton("Stop Music");
		extra.setBackground(Color.YELLOW);
		newgame.setPreferredSize(new Dimension(100,50));
		newgame.addActionListener(this);
		newmatch.setPreferredSize(new Dimension(100,50));
		newmatch.addActionListener(this);
		undobtn.setPreferredSize(new Dimension(100,50));
		undobtn.addActionListener(this);
		redobtn.setPreferredSize(new Dimension(100,50));
		redobtn.addActionListener(this);
		rematchbtn.setPreferredSize(new Dimension(100,50));
		rematchbtn.addActionListener(this);
		playbtn.addActionListener(this);
		stopbtn.addActionListener(this);
		extra.add(newgame);
		extra.add(newmatch);
		extra.add(rematchbtn);
		extra.add(undobtn);
		extra.add(redobtn);
		extra.add(playbtn);
		extra.add(stopbtn);

		/* 
		 * Audio Intialization. Best I managed to do with basic libraries. I apolgoize for the size of the .wav file
		 */

		InputStream in = this.getClass().getClassLoader().getResourceAsStream("MortalKombat.wav");
		as = new AudioStream(in);


		/*
		 * Player Customization and Launch
		 */

		int dialogButton = JOptionPane.YES_NO_CANCEL_OPTION;
		int dialogResult = JOptionPane.showConfirmDialog(this, "Are you ready to play Connect Four?!", "To Play, or not to Play?", dialogButton);
		if(dialogResult==0){
			JOptionPane.showMessageDialog(null, "Great! Here we go!");
		}
		if(dialogResult ==1){
			JOptionPane.showMessageDialog(null, "It's a good thing this isn't a democracy! We're playing anyways!"); //Creative freedom breeds this.
		}
		if(dialogResult == 2){
			System.exit(0);
		}

		matchInitialization(); //Resets scores and matchnum. Gets new player names.
		textinfo.add(directions); //Now that directions recorded, added to panel and container.
		score.setText(player1name + " " + scorep1 + " - " + scorep2 + " " + player2name); //sets up scoreboard
		scorepanel.add(score);
		southpanel.add(textinfo);
		southpanel.add(scorepanel);

		// Now we add everything to the container
		c.add(panelception, BorderLayout.NORTH); //Sets column labeling to be on top.
		c.add(panel, BorderLayout.CENTER); //Sets charboard pieces to be in the center
		c.add(extra, BorderLayout.WEST);
		c.add(southpanel, BorderLayout.PAGE_END);
		setLocationRelativeTo(null); //Sets frame in center of screen
		setResizable(false); //Resizing seriously messes up graphics.


		// Pack sets JFrame size to ideal based on spacing etc.
		pack();
		AudioPlayer.player.start(as); //starts music on launch! Comment out if you would prefer to start it manually
		setVisible(true); //The magic begins


		/********************************************************************
		 *                                                                   *
		 *                            Game Logic!                            *
		 *                                                                   *
		 *********************************************************************/


	}

	public void actionPerformed(ActionEvent e){ //writing method of our interface
		//Logic is a bit lengthy but straightforward
		if(!matchover){
			if(!gameover){
				getPlayer();
				if(e.getSource().equals(btn0) || e.getSource().equals(pieces[0][0]) || e.getSource().equals(pieces[0][1]) || e.getSource().equals(pieces[0][2]) || e.getSource().equals(pieces[0][3])|| e.getSource().equals(pieces[0][4])|| e.getSource().equals(pieces[0][5])|| e.getSource().equals(pieces[0][6])){
					move = 0;
					choosePlayer(move, player);
				}

				if(e.getSource().equals(btn1)  || e.getSource().equals(pieces[1][0]) || e.getSource().equals(pieces[1][1]) || e.getSource().equals(pieces[1][2]) || e.getSource().equals(pieces[1][3])|| e.getSource().equals(pieces[1][4])|| e.getSource().equals(pieces[1][5])|| e.getSource().equals(pieces[1][6])){
					move = 1;
					choosePlayer(move, player);
				}

				if(e.getSource().equals(btn2) || e.getSource().equals(pieces[2][0]) || e.getSource().equals(pieces[2][1]) || e.getSource().equals(pieces[2][2]) || e.getSource().equals(pieces[2][3])|| e.getSource().equals(pieces[2][4])|| e.getSource().equals(pieces[2][5])|| e.getSource().equals(pieces[2][6])){
					move = 2;
					choosePlayer(move, player);
				}

				if(e.getSource().equals(btn3)  || e.getSource().equals(pieces[3][0]) || e.getSource().equals(pieces[3][1]) || e.getSource().equals(pieces[3][2]) || e.getSource().equals(pieces[3][3])|| e.getSource().equals(pieces[3][4])|| e.getSource().equals(pieces[3][5])|| e.getSource().equals(pieces[3][6])){
					move = 3;
					choosePlayer(move, player);
				}

				if(e.getSource().equals(btn4)  || e.getSource().equals(pieces[4][0]) || e.getSource().equals(pieces[4][1]) || e.getSource().equals(pieces[4][2]) || e.getSource().equals(pieces[4][3])|| e.getSource().equals(pieces[4][4])|| e.getSource().equals(pieces[4][5])|| e.getSource().equals(pieces[4][6])){
					move = 4;
					choosePlayer(move, player);
				}


				if(e.getSource().equals(btn5)  || e.getSource().equals(pieces[5][0]) || e.getSource().equals(pieces[5][1]) || e.getSource().equals(pieces[5][2]) || e.getSource().equals(pieces[5][3])|| e.getSource().equals(pieces[5][4])|| e.getSource().equals(pieces[5][5])|| e.getSource().equals(pieces[5][6])){
					move = 5;
					choosePlayer(move, player);
				}

				if(e.getSource().equals(btn6)  || e.getSource().equals(pieces[6][0]) || e.getSource().equals(pieces[6][1]) || e.getSource().equals(pieces[6][2]) || e.getSource().equals(pieces[6][3])|| e.getSource().equals(pieces[6][4])|| e.getSource().equals(pieces[6][5])|| e.getSource().equals(pieces[6][6])){
					move = 6;
					choosePlayer(move, player);
				}

				if(e.getSource().equals(undobtn)){ //Calls undo function
					undoMove();

				}
				if(e.getSource().equals(redobtn)){ //Calls redo function.
					redoMove();
				}
			}
			if(e.getSource().equals(newgame)){
				newGame(pieces, panel); //A bit buggy. If long game played out, sometimes leaves 4 or 5 pieces on board.
				newGame(pieces, panel); //Called twice to make sure board cleared.
				revalidate(); //Another step to ensure board is fully cleared
			}

		}

		if(e.getSource().equals(newmatch)){ //Resets player names and Best of X settings
			setVisible(false);  //No real reason apart from making it feel as if the game restarted
			matchInitialization(); //Resets score/names
			newGame(pieces, panel); //Clears board
			newGame(pieces, panel); //Just in case
			setVisible(true);
		}

		if(e.getSource().equals(rematchbtn)){ //Calls rematch. Keeps same player names and matchnum but resets scores and board
			rematch();
		}

		if(e.getSource().equals(playbtn)){ //Plays music
			controlAudio(false);
		}

		if(e.getSource().equals(stopbtn)){ //Stops music
			controlAudio(true);

		}
	}






	public void setPosition(int x, int y, int player, String s) { //Method to place pieces based on moves and "boolean" value for Undo/Redo

		if (x > WIDTH || x < 0 || y > HEIGHT || y < 0){ return; } //Just in case. Invalid input should not be possible.

		if(s == "null"){ //Undo/Redo not called. Proceed normally.
			oldmoves.push(x); //Records moves played.
			if(!undone.isEmpty()){
				undone.clear(); //If a new move is played NORMALLY, not by redo, you should not be able to redo moves.
			}
			if (player == 1){
				pieces[x][y].setIcon(player1); //Changes blank to P1 icon
				turn++; 
				getPlayer(); //updates player
				setText(); //updates text
				if(checkVictory()){ //checks for victory
					scorep1++; 
					score.setText(player1name + " " + scorep1 + " - " + scorep2 + " " + player2name); //updates scoreboard
					if(scorep1 == matchnum){ //checks if MATCH won
						JOptionPane.showMessageDialog(null, player1name + " wins the match!");
						matchover = true;
					}
					else { //If not, game is still won.  Prevents double messages.
						JOptionPane.showMessageDialog(null, player1name + " wins the game!"); 
					}

				}
			}
			else if (player == 2){ //Same as above
				pieces[x][y].setIcon(player2); //Changes blank to P2 icon
				turn++;
				getPlayer();
				setText();
				if(checkVictory()){
					scorep2++;
					score.setText(player1name + " " + scorep1 + " - " + scorep2 + " " + player2name);
					if(scorep2 == matchnum){
						JOptionPane.showMessageDialog(null, player2name + " wins the match!");
						matchover = true;
					}
					else {
						JOptionPane.showMessageDialog(null, player2name + " wins the game!"); 
					}
				}

			}
		}

		if(s == "undo"){ //If undo is called, this string will be passed here.
			pieces[x][y].setIcon(blank); //finds move and resets to blank
			charboard[y][x] = '.'; //does this on both boards for game logic
			turn--;       //update turn, text, and counters.
			getPlayer();
			setText();
			revalidate(); //to ensure piece gets visually removed

		}

		if(s == "redo"){ //If redo is called, this string will be passed here.
			if (player == 1){
				pieces[x][y].setIcon(player1); //Changes blank to P1 icon
				turn++;
				getPlayer();
				setText();
			}
			else if (player == 2){
				pieces[x][y].setIcon(player2); //Changes blank to P2 icon
				turn++;
				getPlayer();
				setText();
			}

		}

	}


	public void playerOne(int move, String s) { //Places player one's pieces on the char-board. If called by normal input, s will be null.
		for (int j = numcol - 1; j > -1; j--) {
			if (charboard[j][move] == '.') { //Looks for first empty space from the bottom up.
				charboard[j][move] = 'O';
				setPosition(move, j, 1, s); //Calls for graphics.
				return; //exit
			}
		}

		return; //If invalid input, nothing will be done. Turn/Player not incremented in this method so nothing else needed.
	}

	public void playerTwo(int move, String s) { //Places player two's pieces on the char-board.
		for (int j = numcol - 1; j > -1; j--) {
			if (charboard[j][move] == '.') {
				charboard[j][move] = 'X';
				setPosition(move, j, 2, s); //Calls for graphics
				return; //exit
			}
		}
		return;
	}

	public boolean checkVictory() { //Checks for victory conditions!
		for (int i = 0; i <= numrow - win; i++) { //prevents if statements from going out of range for this type of win (ie. vertical) Adjusted for other victory types.
			for (int j = 0; j < numcol; j++) {
				if (charboard[i][j] != '.') { //prevents wins from being 4 "empty" spaces.
					if (charboard[i][j] == charboard[i + 1][j] && charboard[i][j] == charboard[i + 2][j] && charboard[i][j] == charboard[i + 3][j]) { //Checking for Vertical Wins.
						highlightWin(player, i, j, i+1, j, i+2, j, i+3, j); //New. Highlights winning pieces.
						gameover = true;
						return true;

					}
				}
			}
		}
		for (int i = 0; i < numrow; i++) {
			for (int j = 0; j <= numcol - win; j++) {
				if (charboard[i][j] != '.') {
					if (charboard[i][j] == charboard[i][j + 1] && charboard[i][j] == charboard[i][j + 2] && charboard[i][j] == charboard[i][j + 3]) { //Checking for Horizontal Wins.
						highlightWin(player, i, j, i, j+1, i, j+2, i, j+3); //Highlights winning pieces
						gameover = true;
						return true;
					}
				}
			}
		}
		for (int i = numrow - 4; i < numrow; i++) {
			for (int j = 0; j <= numcol - win; j++) {
				if (charboard[i][j] != '.') {
					if (charboard[i][j] == charboard[i - 1][j + 1] && charboard[i][j] == charboard[i - 2][j + 2] && charboard[i][j] == charboard[i - 3][j + 3]) { //Checking for Diagonally Up-Right wins.
						highlightWin(player, i, j, i-1, j+1, i-2, j+2, i-3, j+3); //Highlights winning pieces
						gameover = true;
						return true;
					}
				}
			}
		}
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				if (charboard[i][j] != '.') {
					if (charboard[i][j] == charboard[i + 1][j + 1] && charboard[i][j] == charboard[i + 2][j + 2] && charboard[i][j] == charboard[i + 3][j + 3]) { //Checking for Diagonally Down-Right wins.
						highlightWin(player, i, j, i+1, j+1, i+2, j+2, i+3, j+3); //Highlights winning pieces
						gameover = true;
						return true;
					}
				}
			}
		}

		if(turn == draw){ //checks for draw
			JOptionPane.showMessageDialog(null, "It's a draw!"); //tells players its a draw
			gameover = true;
			return false; //So score is not incremented
		}

		return false; //These four conditions should take into account all "8" winning directions.
	}

	public void choosePlayer(int move, int player){ //calls appropriate method based on turn. Written like so due to old text-based skeleton. Slightly redudant.
		if(player == 1){
			playerOne(move, "null"); //Passes on col# and null to signify a normal play

		}

		else {
			playerTwo(move, "null"); //Passed on col# and null to signify a normal play
		}
	}

	public void newGame(JButton[][] pieces, JPanel panel){  //New game function
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH; j++) {
				pieces[j][i].setIcon(blank); //Clear the visual board
				charboard[j][i] = '.';		// Clear game logic board
			}
		}
		turn = 0; //reset turn counter
		setText(); //updates text
		oldmoves.clear(); //clears stacks
		undone.clear(); 
		gameover = false; //resets boolean
	}

	public void highlightWin(int player, int y1, int x1, int y2, int x2, int y3, int x3, int y4, int x4){ //"animation" for victory
		getPlayer();
		if (player == 2){ //Since a move has been played, player 2 means player 1 won and vice-versa.
			pieces[x1][y1].setIcon(player1win);	
			pieces[x2][y2].setIcon(player1win);	
			pieces[x3][y3].setIcon(player1win);
			pieces[x4][y4].setIcon(player1win);

		}
		else if (player == 1){
			pieces[x1][y1].setIcon(player2win);
			pieces[x2][y2].setIcon(player2win); 
			pieces[x3][y3].setIcon(player2win); 
			pieces[x4][y4].setIcon(player2win);

		}
	}

	@SuppressWarnings("static-access") // Seems to appease eclipse. Text directions weren't updating correctly at the beginning so I bent a few rules.
	public void matchInitialization(){
		scorep1 = 0;
		scorep2 = 0; //resets scores
		matchover = false;
		player1name = input.showInputDialog("Please enter Player 1's name!"); //prompts player 1 for his or her name
		if(player1name == "" || player1name == null || player1name == " " || player1name.length() == 0){
			player1name = "Scorpion";  //Easter Egg. If no valid input, this is hard-wired in.
		}
		player2name = input.showInputDialog("Please enter Player 2's name!");
		if(player2name == "" || player2name == null || player2name == " " || player2name.length() == 0){
			player2name = "Sub-Zero"; //Player two is blue, so player two is Sub-Zero.
		}
		Object[] options = {"Freeplay", "Best of 7", "Best of 5", "Best of 3", "Single Match"}; // Types of Matches
		matchinput = JOptionPane.showOptionDialog(null, "Please Choose an Option Below", "Choose a Gamemode", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[4]); //Makes single match default option


		switch(matchinput){ //Based on which number of option player chose, assigns a number of games needed to win in order to win match

		case 0: matchnum = Integer.MAX_VALUE; //Unfortunately, freeplay isn't infinite, but close enough.
		break;

		case 1: matchnum = 4; //Best of 7
		break;

		case 2: matchnum = 3; //Best of 5
		break;

		case 3: matchnum = 2; //Best of 3
		break;

		case 4: matchnum = 1; //Single match
		break;						
		}

		//Note. Order of options flipped on windows and mac, but only visually.

		getPlayer(); //updates player
		setText(); //updates text
		score.setText(player1name + " " + scorep1 + " - " + scorep2 + " " + player2name); //updates score
	}

	public void undoMove(){ 
		getPlayer();
		int tempmove = 0; //Value to be passed on to set position. Initialized here to make eclipse happy.
		if(!oldmoves.isEmpty()){ //prevents empty stack errors
			int oldmove = oldmoves.pop(); //calls last move
			undone.push(oldmove); //Keeps track of what moves have been undone for redo function
			for (int j = 0; j < numcol; j++) {
				if (charboard[j][oldmove] != '.') {
					tempmove = j; //finds first non-empty row.
					break; //to prevent tempmove from being overwritten
				}
			}

			setPosition(oldmove, tempmove, player, "undo"); //makes set position replace Jbutton[x][y] with blank

		}
	}




	public void redoMove(){
		getPlayer();
		if(!undone.isEmpty()) { //can't redo if nothing undone
			int undoneMove = undone.pop(); //calls last undone move
			oldmoves.push(undoneMove); //pushes onto moves played since set Position no longer will
			if(player == 1){
				playerOne(undoneMove, "redo"); // playerX method already finds first empty space. Just passes on col number and redo
			}
			else{
				playerTwo(undoneMove, "redo");
			}
		}
	}

	public void rematch(){ //Reset scores, clear board, and update scores without resetting player names or Best of X
		scorep1 = 0;
		scorep2 = 0; 
		matchover = false;
		newGame(pieces, panel);
		score.setText(player1name + " " + scorep1 + " - " + scorep2 + " " + player2name);
	}

	public void getPlayer(){ //gets player
		if(turn%2 == 0){ 
			player =  1; //to make reading code easier
		}

		else{
			player = 2; //to make reading code easier
		}
	}

	public void setText(){ //Checks which player's turn it is, tells them to choose a column
		if(player == 1){
			directions.setText(player1name + ", please choose a column!");
		}
		else{
			directions.setText(player2name + ", please choose a column!");

		}
	}

	public void controlAudio(boolean stop){ //Audio control
		if(stop){
			AudioPlayer.player.stop(as);
		}
		else{

			AudioPlayer.player.start(as);
		}
	}

}


