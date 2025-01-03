package me.radu.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

public class Packet {

    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Expose
    @Getter
    private String requestName; // name of the request template

    @Expose
    @Getter
    private boolean requestStatus; // false if is outgoing, true if is a response

    @Getter
    private long requestId;
    private JsonElement requestContent;

    @Setter
    private volatile transient Client client;

    public Packet() {

    }
}
