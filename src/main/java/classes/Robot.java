package classes;

import com.badlogic.gdx.math.*;
import enums.*;
import interfaces.IRobot;
import com.badlogic.gdx.maps.tiled.*;
import java.util.ArrayList;
import java.util.List;

public class Robot implements IRobot {

    private TiledMapTileLayer.Cell state;

    private int x, y;
    private ArrayList<Card> hand;
    private int bp_x, bp_y;
    private ArrayList<Integer> flags;
    private int hp;
    private Direction direction;
    private boolean died;

    public Robot(TiledMapTileLayer.Cell start){
        hp = 3;
        x = 1;
        y = 1;
        bp_x = x;
        bp_y = y;
        state = start;
        flags = new ArrayList<>();
        direction = Direction.UP;
    }

    @Override
    public void takeDamage() {
        hp--;
    }

    @Override
    public void die() {
    }

    @Override
    public void shootLaser() {
    }

    @Override
    public void input() {
    }

    @Override
    public boolean powerDown(boolean input) {return input;}

    @Override
    public void createHand() {
        hand = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            hand.add(new Card());
            hand.get(i).setRobot(this);
        }
    }

    @Override
    public boolean isReady() {
        //TODO: implement this!
        return false;
    }

    @Override
    public Direction getDirection() {return direction;}

    @Override
    public void setDirection(Direction direction) {
        switch (direction){
            case LEFT:
                state.setRotation(1);
                break;
            case RIGHT:
                state.setRotation(3);
                break;
            case UP:
                state.setRotation(0);
                break;
            case DOWN:
                state.setRotation(2);
                break;
        }
        this.direction = direction;
    }

    @Override
    public TiledMapTileLayer.Cell getState(){
        return state;
    }

    @Override
    public void setState(TiledMapTileLayer.Cell state){
        this.state = state;
    }

    @Override
    public void setPos(int x, int y){
        this.x = x;
        this.y = y;
    }

    @Override
    public int getPosX(){ return x;}

    @Override
    public int getPosY(){return y;}

    @Override
    public void setBackup(int x, int y){
        bp_y = y;
        bp_x = x;
    }

    @Override
    public boolean addFlag(int id, TiledMapTileLayer flagLayer){
        if(flagLayer.getCell(x,y).getTile().getId() == tileID.FLAG1.getId() && !hasFlag(tileID.FLAG1.getId()) && !hasFlag(tileID.FLAG2.getId()) && !hasFlag(tileID.FLAG3.getId()) && !hasFlag(tileID.FLAG4.getId())){
            flags.add(id);
            setBackup(x,y);
            return true;
        }
        else if(flagLayer.getCell(x,y).getTile().getId() == tileID.FLAG2.getId() && hasFlag(tileID.FLAG1.getId()) && !hasFlag(tileID.FLAG2.getId()) && !hasFlag(tileID.FLAG3.getId()) && !hasFlag(tileID.FLAG4.getId())){
            flags.add(id);
            setBackup(x,y);
            return true;
        }
        else if(flagLayer.getCell(x,y).getTile().getId() == tileID.FLAG3.getId() && hasFlag(tileID.FLAG1.getId()) && hasFlag(tileID.FLAG2.getId()) && !hasFlag(tileID.FLAG3.getId()) && !hasFlag(tileID.FLAG4.getId())){
            flags.add(id);
            setBackup(x,y);
            return true;
        }
        else if(flagLayer.getCell(x,y).getTile().getId() == tileID.FLAG4.getId() && hasFlag(tileID.FLAG1.getId()) && hasFlag(tileID.FLAG2.getId()) && hasFlag(tileID.FLAG3.getId()) && !hasFlag(tileID.FLAG4.getId())) {
            flags.add(id);
            setBackup(x,y);
            return true;
        }
        else return false;
    }

    @Override
    public boolean hasFlag(int id){
        return flags.contains(id);
    }

    @Override
    public int numbFlags(){
        return flags.size();
    }

    @Override
    public int getBp_x() {
        return bp_x;
    }

    @Override
    public int getBp_y() {
        return bp_y;
    }

    @Override
    public int getHp(){
        return hp;
    }
    @Override
    public List<Card> getHand() {
        if (hand == null)
            createHand();
        else if (hand.isEmpty())
            createHand();
        return hand;
    }
    @Override
    public void setDied(boolean b){
        died = b;
    }
    @Override
    public boolean getDied(){
        return died;
    }
}

