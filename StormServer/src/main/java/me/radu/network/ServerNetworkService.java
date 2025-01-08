package me.radu.network;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import me.radu.core.ServerInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Log4j2
public class ServerNetworkService {

    @Getter
    private final List<Client> clients = new ArrayList<>();
    private final Map<String, IRequestTemplate> requestTemplates = new HashMap<>();
    private final Gson gson = new Gson();

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private final ServerInstance serverInstance;

    public ServerNetworkService(ServerInstance serverInstance) {
        this.serverInstance = serverInstance;
    }

    /**
     * Registers a new request handler template.
     */
    public ServerNetworkService addRequestTemplate(String name, IRequestTemplate requestTemplate) {
        this.requestTemplates.put(name, requestTemplate);
        return this;
    }

    /**
     * Starts the network service on the specified port.
     */
    public void start(int port) throws IOException {
        log.info("Starting Network Service on port {}...", port);

        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(false);

        this.selector = Selector.open();
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        log.info("Network Service is running on port {}.", port);

        while (serverSocketChannel.isOpen()) {
            if (selector.select() > 0) {
                var selectedKeys = selector.selectedKeys();
                var it = selectedKeys.iterator();

                while (it.hasNext()) {
                    var key = it.next();
                    it.remove();

                    try {
                        if (key.isAcceptable()) {
                            acceptNewClient();
                        } else if (key.isReadable()) {
                            handleIncomingBytes(key);
                        }
                    } catch (Exception e) {
                        log.error("Error processing key: {}", key, e);
                    }
                }
            }
        }
    }

    private void acceptNewClient() throws IOException {
        SocketChannel clientChannel = serverSocketChannel.accept();
        if (clientChannel != null) {
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            Client client = new Client(clientChannel);
            clients.add(client);

            log.info("New client connected: {}", client.getGuestName());
        }
    }

    private void handleIncomingBytes(SelectionKey key) {
        var clientOpt = clients.stream()
                .filter(client -> client.getChannel().equals(key.channel()))
                .findFirst();

        if (clientOpt.isEmpty()) {
            log.warn("Received data from an unknown client.");
            return;
        }

        Client client = clientOpt.get();
        try {
            ByteBuffer buffer = ByteBuffer.allocate(65536);
            SocketChannel clientChannel = client.getChannel();

            int bytesRead = clientChannel.read(buffer);
            if (bytesRead == -1) {
                log.info("Client {} disconnected.", client.getGuestName());
                cleanupClient(client);
                return;
            }

            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String message = new String(data, StandardCharsets.UTF_8).trim();

            if (!message.isEmpty()) {
                log.debug("Received message from {}: {}", client.getGuestName(), message);
                processIncomingPacket(client, message);
            }

        } catch (SocketException e) {
            log.info("Client {} disconnected unexpectedly.", client.getGuestName());
            cleanupClient(client);
        } catch (IOException e) {
            log.error("Error reading from client {}: {}", client.getGuestName(), e);
            cleanupClient(client);
        }
    }

    private void processIncomingPacket(Client client, String message) {
        try {
            Packet packet = new Gson().fromJson(message, Packet.class);
            packet.setClient(client);

            String requestName = packet.getRequestName();
            IRequestTemplate handler = requestTemplates.get(requestName);

            if (handler != null) {
                handler.onIncomingRequest(packet);
                packet.setRequestStatus(Packet.RECEIVE);
                sendPacket(packet, client.getChannel());
            } else {
                log.warn("No handler found for request: {}", requestName);
            }
        } catch (Exception e) {
            log.error("Error processing packet from client {}: {}", client.getGuestName(), e);
        }
    }

    private void sendPacket(Packet packet, SocketChannel socketChannel) {
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

    private void cleanupClient(Client client) {
        try {
            clients.remove(client);
            client.getChannel().close();
            log.info("Client {} has been cleaned up.", client.getGuestName());
        } catch (IOException e) {
            log.error("Error closing client connection: {}", client.getGuestName(), e);
        }
    }
}
