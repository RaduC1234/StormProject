package me.radu.network;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClientNetworkService {

    private static final Logger LOGGER = LogManager.getLogger(ClientNetworkService.class);

    private final Map<String, IRequestTemplate> requestTemplates = new HashMap<>();
    private final List<Packet> waitingOutboundPackets = new ArrayList<>();

    private Selector selector;
    private SocketChannel socketChannel;
    private Gson gson = new Gson();
    private boolean running = true;

    public ClientNetworkService() {
        // Initialize request templates
    }

    public ClientNetworkService addRequestTemplate(String name, IRequestTemplate requestTemplate) {
        this.requestTemplates.put(name, requestTemplate);
        return this;
    }

    public void connect(String host, int port) throws IOException {
        selector = Selector.open();
        socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        LOGGER.info("Connected to server at {}:{}", host, port);

        // Start event loop in a separate thread
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
                            handleIncomingBytes(key);
                        }

                        if (key.isWritable()) {
                            handleOutgoingBytes(key);
                        }

                        it.remove();
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Error in event loop", e);
                stop();
            }
        }
    }

    private void handleIncomingBytes(SelectionKey key) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead == -1) {
                LOGGER.info("Server closed connection");
                stop();
                return;
            }

            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String message = new String(bytes, StandardCharsets.UTF_8).trim();

            if (message.isEmpty()) return;

            LOGGER.info("Received: {}", message);

            Packet receivedPacket = gson.fromJson(message, Packet.class);
            processIncomingPacket(receivedPacket);

        } catch (IOException e) {
            LOGGER.error("Error reading from server", e);
            stop();
        }
    }

    private void handleOutgoingBytes(SelectionKey key) {
        synchronized (waitingOutboundPackets) {
            Iterator<Packet> iterator = waitingOutboundPackets.iterator();
            while (iterator.hasNext()) {
                Packet packet = iterator.next();
                try {
                    String json = gson.toJson(packet);
                    ByteBuffer buffer = ByteBuffer.wrap(json.getBytes(StandardCharsets.UTF_8));
                    socketChannel.write(buffer);
                    iterator.remove();
                    LOGGER.info("Sent: {}", json);
                } catch (IOException e) {
                    LOGGER.error("Failed to send packet", e);
                    stop();
                }
            }
        }
    }

    public void sendRequest(String name, Object[] params) {
        if (!requestTemplates.containsKey(name)) {
            throw new IllegalArgumentException("No request template found for: " + name);
        }

        Packet packet = new Packet(name);
        waitingOutboundPackets.add(packet);
        requestTemplates.get(name).onNewRequest(packet, params);
    }

    private void processIncomingPacket(Packet receivedPacket) {
        LOGGER.info("Processing packet: " + receivedPacket.getRequestName());

        if (receivedPacket.isRequestStatus()) {
            Iterator<Packet> iterator = waitingOutboundPackets.iterator();
            while (iterator.hasNext()) {
                Packet packet = iterator.next();
                if (packet.getRequestId() == receivedPacket.getRequestId()) {
                    IRequestTemplate requestTemplate = requestTemplates.get(packet.getRequestName());
                    if (requestTemplate != null) {
                        requestTemplate.onAnswer(receivedPacket);
                        iterator.remove();
                    } else {
                        LOGGER.error("No request template found for response: " + packet.getRequestName());
                    }
                    return;
                }
            }
            LOGGER.error("No matching request found for response ID: " + receivedPacket.getRequestId());
            return;
        }

        IRequestTemplate requestTemplate = requestTemplates.get(receivedPacket.getRequestName());
        if (requestTemplate != null) {
            requestTemplate.onIncomingRequest(receivedPacket);
        } else {
            LOGGER.error("Invalid request name: " + receivedPacket.getRequestName());
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
            LOGGER.error("Error closing client", e);
        }
    }
}
