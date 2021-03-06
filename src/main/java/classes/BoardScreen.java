package classes;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import enums.*;

import java.util.*;

public class BoardScreen implements Screen {

    //Creates the map, and the layers with different map pieces
    public Map map;

    //Creates a robot that reacts to input
    private Robot robot;

    private final StartGame game;

    //The camera and the viewpoint
    private OrthogonalTiledMapRenderer TMRenderer;
    private OrthographicCamera camera;

    private final int WIDTH = 12;
    private final int HEIGHT = 19;

    private SpriteBatch batch;
    private BitmapFont font;
    private SpriteBatch batch2;
    private BitmapFont font2;
    private Button doTurnButton;
    private ArrayList<Button> cards;
    private TextField[] selectedNumbers;
    private TurnHandler turnHandler;
    private List<Robot> AIList;

    private Stage stage;

    private int inputCooldown;

    /**
     * Gets the main game and the number of players and AI.
     * @param game
     * @param numbPlayers
     * @param numbAI
     */

    public BoardScreen(StartGame game, int numbPlayers, int numbAI,String boardname){
        this.game = game;
        stage = new Stage();
        //Loads the board.
        String boardName = boardname + ".tmx";
        map = new Map(boardName);
        map.getBoard(this);

        //Creates the turnahandler
        turnHandler = new TurnHandler();
        turnHandler.setMap(map);
        TurnHandler.setPlayers(numbPlayers + numbAI);
        //places the players an AI on the boarsd.
        map.placePlayers(numbPlayers);
        map.placeAI(numbAI);

        this.AIList = map.getAIList();

        setPlayer();

        camera = new OrthographicCamera();
        camera.setToOrtho(false,WIDTH,HEIGHT);

        initCards();
        //creates the button for swapping players and starting the rounds.
        doTurnButton = new Button(new TextureRegionDrawable(new TextureRegion(new Texture("buttons/startbtn.png"))));
        doTurnButton.setSize(100,50);
        doTurnButton.setPosition(Options.screenWidth/2f,Options.screenHeight/6f,0);
        stage.addActor(doTurnButton);

        camera.position.set((WIDTH-1.8f)/2,(HEIGHT-9.5f)/2,0);
        camera.update();

        TMRenderer = new OrthogonalTiledMapRenderer(map.getMap(), 1f/350f);
        TMRenderer.setView(camera);

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.BLACK);

        selectedNumbers = new TextField[5];

        batch2 = new SpriteBatch();
        font2 = new BitmapFont();
        font2.setColor(Color.BLACK);

        //Creates the input.
        InputProcessor inputProcessorOne = stage;
        //creates testing controller
        //InputProcessor inputProcessorTwo = createController();

        InputMultiplexer inputMultiplexer = new InputMultiplexer();

        inputMultiplexer.addProcessor(inputProcessorOne);
        //inputMultiplexer.addProcessor(inputProcessorTwo);

        Gdx.input.setInputProcessor(inputMultiplexer);
    }
     //initiates the card buttons.
    private void initCards(){
        cards = new ArrayList<>();
        for (int i = 0; i < robot.getHand().size(); i++) {
            Button button = new Button(new TextureRegionDrawable(new TextureRegion(new Texture("card/card.png"))));
            button.setPosition((float)(Options.screenWidth * i / robot.getHand().size()) + getCardPadding(),0);
            button.setName(""+i);

            button.setColor(Color.CYAN);
            cards.add(button);
            stage.addActor(button);
        }
    }

    public int getCardPadding(){
        return ((Options.screenWidth/robot.getHand().size())-72)/2;
    }

    @Override
    public void show() {
    }

    public void resetInputCooldown(){
        inputCooldown = 10;
    }

    public boolean inputCooldownDone(){
        return inputCooldown <= 0;
    }

    @Override
    public void render(float v) {
        Gdx.gl.glClearColor(0.33f, 0.33f, 0.33f, 1);
        Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

        TMRenderer.render();

        InfoText();
        updateCardButtons();
        stage.act();
        stage.draw();

        cardPress();
        startPressed();

        //prints the type of card.
        batch.begin();
        for (int i = 0; i < robot.getHand().size(); i++) {
            robot.getHand().get(i).render(batch,font,(Options.screenWidth * i / robot.getHand().size()) + getCardPadding(),0,(int)cards.get(0).getWidth(),(int)cards.get(0).getHeight());
        }
        batch.end();
        inputCooldown = inputCooldownDone() ? 0 : inputCooldown-1;
    }

    //updates card button positions
    public void updateCardButtons(){
        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).setPosition((float)(Options.screenWidth * i / robot.getHand().size()) + getCardPadding(),0);
        }
    }

    //prints the info about the player.
    private void InfoText() {
        String playerInfoText = robot.getName() + "  " + "Position: " + robot.getPos() + "\n" +
                "Lives: " + robot.getHp() + "  " +
                "Damage Tokens: " + robot.getDamageToken() + "\n" +
                "Flags taken: " + robot.numbFlags() + "  " +
                "Direction: " + robot.getDirection() + "\n";
        batch2.begin();
        font2.draw(batch2, playerInfoText, 30, 75 + cards.get(0).getHeight());
        batch2.end();
    }

    //checks if the player is ready and the round. can begin or just switch player.
    private void startPressed(){
        if (inputCooldownDone()) {
            if (doTurnButton.isPressed() && !doTurnButton.isDisabled()) {
                resetInputCooldown();
                robot.setReady();
                System.out.println("START");
                if(playersReady()) {
                    System.out.println("yes");
                    AIdoTurn();
                    turnHandler.setReady();
                    setPlayer();
                    newTurnCleanup();
                }
                else{
                    if(robot.cardsChosen.size() == robot.getNumbRegister()) {
                        setPlayer();
                        newPlayerCleanup();
                    }
                }
            }
        }
    }

    //checks if all the players are ready-
    public boolean playersReady(){
        for(Robot r : map.playerList){
            if(!r.isReady()) return false;
        }
        return true;
    }

    //cheack if the card is pressed, and then add that to the selected cards of the player.
    private void cardPress(){
        if (inputCooldownDone()) {
            for (int i = 0; i < robot.getHand().size(); i++) {
                Button card = cards.get(i);
                if (card.isPressed()) {
                    resetInputCooldown();
                    if (card.isDisabled()) {
                        removeSelectedText(i);
                    } else {
                        int xPos = (Options.screenWidth*i/robot.getHand().size())+(int)(card.getWidth()/2);
                        addSelectedText(i,xPos);
                    }

                }
            }
        }
    }
    // see cardpress
    private void addSelectedText(int cardNr, int xPos){
        boolean failed = true;
        for (int i = 0; i < 5; i++) {
            if (selectedNumbers[i]==null){
                TextField textField = new TextField(""+i,new TextField.TextFieldStyle(new BitmapFont(),Color.BLACK,null,null,null));
                textField.setName(""+cardNr);
//                textField.setBounds(xPos,125,50,50);
                textField.setPosition(xPos,100);
                selectedNumbers[i]=textField;
                stage.addActor(textField);
                failed=false;
                break;
            }
        }
        if (failed){
            removeLastSelectedText();
            addSelectedText(cardNr,xPos);
        }
        else {
            cards.get(cardNr).setDisabled(true);
            cards.get(cardNr).setColor(Color.MAGENTA);
            robot.selectCard(cardNr);
            System.out.println("selected card " + cardNr);
        }
    }

    //removes the card if it is selected.
    private void removeLastSelectedText(){
        int lastIdx = Character.getNumericValue(selectedNumbers[4].getName().charAt(0));
        removeSelectedText(lastIdx);
    }

    //sets the board up for the next player.
    private void newPlayerCleanup(){
        for (Button cardButton: cards) {
            cardButton.setColor(Color.CYAN);
            cardButton.setDisabled(false);
        }
        for (TextField textField: selectedNumbers) {
            textField.remove();
        }
        selectedNumbers = new TextField[robot.getNumbRegister()];
    }

    //cleans the board up for a new round.
    private void newTurnCleanup(){
        for (Button cardButton: cards) {
            cardButton.setColor(Color.CYAN);
            cardButton.setDisabled(false);
        }
        for (int i = 0; i < selectedNumbers.length; i++) {
            selectedNumbers[i].remove();
            selectedNumbers[i]=null;
        }
        for(Robot r : map.playerList){
            r.notReady();
        }
    }

    private void removeSelectedText(int cardNr){
        System.out.println("unselected card " + cardNr);
        cards.get(cardNr).setColor(Color.CYAN);
        robot.unselectCard(cardNr);
        cards.get(cardNr).setDisabled(false);
        for (int i = 0; i < 5; i++) {
            if (selectedNumbers[i]!=null) {
                if (selectedNumbers[i].getName().equals(cards.get(cardNr).getName())) {
                    selectedNumbers[i].remove();
                    selectedNumbers[i] = null;
                    break;
                }
            }
        }
    }

    @Override
    public void resize(int i, int i1) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
       TMRenderer.dispose();
       batch.dispose();
       stage.dispose();
    }

    /**
     * Add keyboard and mouse interactions
     */
    private InputProcessor createController() {
        /*Input controller*/
       InputProcessor input = new InputAdapter() {
            @Override
            public boolean keyUp(int keycode) {
                /*input controller*/
                switch (keycode) {
                    case Input.Keys.LEFT:
                        map.moveRobot(robot, Direction.LEFT);
                        break;
                    case Input.Keys.RIGHT:
                        map.moveRobot(robot, Direction.RIGHT);
                        break;
                    case Input.Keys.UP:
                        map.moveRobot(robot, Direction.UP);
                        break;
                    case Input.Keys.DOWN:
                        map.moveRobot(robot, Direction.DOWN);
                        break;
                        case Input.Keys.S:
                        setPlayer();
                        break;
                    case Input.Keys.F:
                        GameLogic.fireLaser(new Vector2(robot.getPosX(), robot.getPosY()), robot.getDirection());
                        break;
                    case Input.Keys.C:
                        GameLogic.clearLasers();
                        break;
                    case Input.Keys.L:
                        GameLogic.fireAllLasers();
                        break;
                    case Input.Keys.B:
                        GameLogic.doConveyor(robot);
                        break;
                    case Input.Keys.R:
                        GameLogic.rotorPad(robot);
                        break;
                    case Input.Keys.T:
                        GameLogic.repeair(robot);
                        break;
                    case Input.Keys.P:
                        GameLogic.pushPad(robot);
                        break;
                    case Input.Keys.ESCAPE:
                        Gdx.app.exit();
                        break;
                }
                return false;
            }
        };
       return input;
    }

    /**
     * Set player to interact with
     */
    public void setPlayer(){
        if(robot == null) {
            robot = map.getPlayerList().get(map.switchPlayer(null));
        }
        else {
            robot = map.getPlayerList().get(map.switchPlayer(robot));
        }
    }


    public void AIdoTurn(){
        for(Robot AI : AIList){
            AI.doTurn();
        }
    }

    public Map getMap() {
        return map;
    }

}
