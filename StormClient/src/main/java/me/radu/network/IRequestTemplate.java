package me.radu.network;

public interface IRequestTemplate {

    default void onNewRequest(Packet packet, Object[] params) {}

    default void onAnswer(Packet packet) {
        //packet.sendError(Packet.PACKET_CODES.ERROR);
        //logger.error("Unknown request.");
    }

    void onIncomingRequest(Packet packet);
}
