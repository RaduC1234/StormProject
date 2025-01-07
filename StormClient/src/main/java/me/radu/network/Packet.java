package me.radu.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class Packet {

    public static final boolean SEND = false;
    public static final boolean RECEIVE = true;
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Expose
    @Getter
    private String requestName; // Name of the request type

    @Expose
    @Getter
    @Setter
    private boolean requestStatus; // false if outgoing, true if response

    @Expose
    @Getter
    private long requestId;

    @Setter
    @Getter
    @Expose
    private JsonElement payload; // Stores data or errors

    public Packet(String requestName) {
        this.requestName = requestName;
        this.requestId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }

    /**
     * Checks if this packet represents an error.
     *
     * @return true if the packet is an error, false otherwise.
     */
    public boolean isError() {
        if (payload == null || !payload.isJsonObject()) return false;
        JsonObject json = payload.getAsJsonObject();
        return json.has("error");
    }

    /**
     * Gets the error code from the packet if it is an error.
     *
     * @return The ErrorCode if present, null otherwise.
     */
    public ErrorCode getError() {
        if (!isError()) return null;
        JsonObject json = payload.getAsJsonObject();
        return ErrorCode.valueOf(json.get("error").getAsString());
    }

    /**
     * Sets this packet as an error with the given error code.
     *
     * @param errorCode The error code to set in the packet.
     * @return The current packet instance with the error set.
     */
    public Packet sendError(ErrorCode errorCode) {
        this.requestStatus = true; // Mark as a response

        JsonObject errorPayload = new JsonObject();
        errorPayload.addProperty("error", errorCode.name());
        this.payload = errorPayload;

        return this;
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

    /**
     * Enum for defining standard error codes.
     */
    public enum ErrorCode {
        USER_IN_USE,
        BAD_CREDENTIALS,
        NOT_AUTHENTICATED,
        UNKNOWN
    }
}
