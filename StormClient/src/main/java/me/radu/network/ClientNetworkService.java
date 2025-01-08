package me.radu.network;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class ClientNetworkService {

    private final Map<Long, CompletableFuture<Packet>> pendingRequests = new ConcurrentHashMap<>();
    private final List<Packet> waitingOutboundPackets = new ArrayList<>();
    private final Gson gson = new Gson();

    private Selector selector;
    private SocketChannel socketChannel;
    private boolean running = true;

    public void connect(String host, int port) throws IOException {
        selector = Selector.open();
        socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        log.info("Connected to server at {}:{}", host, port);

        new Thread(this::eventLoop).start();
    }

    private void eventLoop() {
        while (running) {
            try {
                if (selector.select() > 0) {
                    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        if (key.isReadable()) {
                            handleIncomingBytes();
                        }
                        it.remove();
                    }
                }
            } catch (IOException e) {
                log.error("Error in event loop", e);
                stop();
            }
        }
    }

    private void handleIncomingBytes() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(65536);
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead == -1) {
                log.info("Server closed connection");
                stop();
                return;
            }

            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String message = new String(bytes, StandardCharsets.UTF_8).trim();

            if (message.isEmpty()) return;

            log.info("Received: {}", message);
            Packet receivedPacket = gson.fromJson(message, Packet.class);
            processIncomingPacket(receivedPacket);

        } catch (IOException e) {
            log.error("Error reading from server", e);
            stop();
        }
    }

    public CompletableFuture<Packet> sendRequest(String requestType, JsonElement payload) {
        Packet packet = new Packet(requestType);
        packet.setRequestStatus(Packet.SEND);
        packet.setPayload(payload);

        CompletableFuture<Packet> future = new CompletableFuture<>();
        pendingRequests.put(packet.getRequestId(), future);

        sendPacket(packet);
        return future;
    }

    private void sendPacket(Packet packet) {
        String jsonPacket = gson.toJson(packet);
        ByteBuffer buffer = ByteBuffer.wrap(jsonPacket.getBytes(StandardCharsets.UTF_8));

        try {
            socketChannel.write(buffer);
            log.info("Sent request '{}' to server.", packet.getRequestName());
        } catch (IOException e) {
            log.error("Error sending request", e);
            throw new RuntimeException(e);
        }
    }

    private void processIncomingPacket(Packet receivedPacket) {
        log.info("Processing packet: {}", receivedPacket.getRequestName());

        long requestId = receivedPacket.getRequestId();
        if (pendingRequests.containsKey(requestId)) {
            CompletableFuture<Packet> future = pendingRequests.remove(requestId);
            if (future != null) {
                future.complete(receivedPacket);
            }
        } else {
            log.warn("Received unknown request ID: {}", requestId);
        }
    }

    public void stop() {
        running = false;
        try {
            if (socketChannel != null) {
                socketChannel.close();
            }
            if (selector != null) {
                selector.close();
            }
        } catch (IOException e) {
            log.error("Error closing client", e);
        }
    }

    public List<Packet> getWaitingOutboundPackets() {
        return waitingOutboundPackets;
    }
}
