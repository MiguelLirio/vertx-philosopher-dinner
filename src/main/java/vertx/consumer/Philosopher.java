package vertx.consumer;

import vertx.components.Status;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Philosopher {

    private Date lastMeal;
    private Status status;
    private final String name;
    private int forks;

    public Philosopher(String nameValue) {
        this.name = nameValue;
        this.status = Status.THINKING;
        this.lastMeal = new Date();
        this.forks = 0;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status statusValue) {
        this.status = statusValue;
    }

    public String getName() {
        return name;
    }

    public void takeFork() {
        if (this.forks >= 2) {
            throw new IllegalStateException();
        }
        this.forks++;
    }

    public void releaseForks() {
        this.forks = 0;
    }

    public boolean isAlive() {
        return this.getStatus() != Status.DEAD;
    }

    private long getSecondSinceLastMeal() {
        long duration  = new Date().getTime() - this.lastMeal.getTime();
        return TimeUnit.MILLISECONDS.toSeconds(duration);
    }

    @Override
    public String toString() {
        return name + ", " + status;
    }
}
