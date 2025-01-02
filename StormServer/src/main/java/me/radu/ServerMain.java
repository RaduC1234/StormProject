package me.radu;

import me.radu.core.ServerInstance;

public class ServerMain {
    public static void main(String[] args) {
        ServerInstance instance = new ServerInstance();
        instance.start();
    }
}