package com.samleighton.xquiset.sethomes;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Objects;

public class Home {

    //Data necessary for a home
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float yaw;
    private String world;
    private String homeName;
    private String desc = null;

    //Instantiate data
    public Home(Location l) {
        setWorld(Objects.requireNonNull(l.getWorld()).getName());
        setX(l.getX());
        setY(l.getY());
        setZ(l.getZ());
        setYaw(l.getYaw());
        setPitch(l.getPitch());
    }

    public Home(String homeName, String world, double x, double y, double z, float pitch, float yaw, String homeDesc) {
        setHomeName(homeName);
        setWorld(world);
        setX(x);
        setY(y);
        setZ(z);
        setPitch(pitch);
        setYaw(yaw);
        setDesc(homeDesc);
    }

    /**
     * @return the world
     */
    public String getWorld() {
        return world;
    }

    /**
     * @param w, set the World players home
     */
    public void setWorld(String w) {
        this.world = w;
    }

    /**
     * @return the pitch
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * @param pitch, set the pitch of the players home
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /**
     * @return the yaw
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * @param yaw, set the Yaw of the players home
     */
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    /**
     * @return the z
     */
    public double getZ() {
        return z;
    }

    /**
     * @param z, set Z value of players home
     */
    public void setZ(double z) {
        this.z = z;
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @param x, set X value of players home
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public double getY() {
        return y;
    }

    /**
     * @param y, set Y value of players home
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * @return the description
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @param desc, sets a players home description
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * @return the homeName
     */
    public String getHomeName() {
        return homeName;
    }

    /**
     * @param homeName, set the homes name given by the player
     */
    public void setHomeName(String homeName) {
        this.homeName = homeName;
    }

    /**
     * @return the home as a location object
     */
    public Location toLocation() {
        return new Location(Bukkit.getServer().getWorld(this.getWorld()), getX(), getY(), getZ(), getYaw(), getPitch());
    }

    /**
     * @return return the home object as a string
     */
    public String toString() {
        return "Home Name: " + getHomeName() + "\n" +
                "Home Desc: " + getDesc() + "\n" +
                "Location: " + toLocation().toString() + "\n";
    }
}
