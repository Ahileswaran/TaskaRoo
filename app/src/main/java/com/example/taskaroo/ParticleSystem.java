package com.example.taskaroo;

import java.util.concurrent.TimeUnit;

public class ParticleSystem {
    private float speed = 0f;
    private float maxSpeed = 30f;
    private float damping = 0.9f;
    private int spread = 360;
    private int[] colors = {0xfce18a, 0xff726d, 0xf4306d, 0xb48def};
    private Emitter emitter;
    private Position position;
    private ViewKonfetti viewKonfetti;

    public ParticleSystem() {
        emitter = new Emitter(100, TimeUnit.MILLISECONDS);
        emitter.setMax(100);
        position = new Position.Relative(0.5f, 0.3f);
    }
    public void setViewKonfetti(ViewKonfetti viewKonfetti) {
        this.viewKonfetti = viewKonfetti;
    }
    public void start() {
        viewKonfetti.start(this);
    }

    // Getters and setters for the properties
    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public float getDamping() {
        return damping;
    }

    public void setDamping(float damping) {
        this.damping = damping;
    }

    public int getSpread() {
        return spread;
    }

    public void setSpread(int spread) {
        this.spread = spread;
    }

    public int[] getColors() {
        return colors;
    }

    public void setColors(int[] colors) {
        this.colors = colors;
    }

    public Emitter getEmitter() {
        return emitter;
    }

    public void setEmitter(Emitter emitter) {
        this.emitter = emitter;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}

class Emitter {
    private long duration;
    private TimeUnit timeUnit;
    private int max;

    public Emitter(long duration, TimeUnit timeUnit) {
        this.duration = duration;
        this.timeUnit = timeUnit;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public int getMax() {
        return max;
    }
}

class Position {
    private float x;
    private float y;

    private Position(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static class Relative extends Position {
        public Relative(float x, float y) {
            super(x, y);
        }
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}

class ViewKonfetti {
    public void start(ParticleSystem particleSystem) {
        // Start the particle system
    }
}



