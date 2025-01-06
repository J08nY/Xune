package sk.neuromancer.Xune.entity;

public interface Moveable {

    void setPosition(float x, float y);

    void move(float toX, float toY);

    float getSpeed();
}
