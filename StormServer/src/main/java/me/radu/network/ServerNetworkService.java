package me.radu.network;

import com.google.gson.Gson;
import me.radu.core.ServerInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServerNetworkService {

    private static final Logger LOGGER = LogManager.getLogger(ServerNetworkService.class);

    private List<Client> clients;
    private Map<String, IRequestTemplate> requestTemplates;

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private int port;
    private ServerInstance serverInstance;

    private List<Packet> waitingOutboundPackets = new ArrayList<>();

    public ServerNetworkService(ServerInstance serverInstance) {
        this.serverInstance = serverInstance;
        this.requestTemplates = new HashMap<>();
    }

    public ServerNetworkService addRequestTemplate(String name, IRequestTemplate requestTemplate) {
        this.requestTemplates.put(name, requestTemplate);
        return this;
    }

    public void start(int port) throws IOException {

        this.port = port;
        this.clients = new ArrayList<>();

        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(false);
        this.selector = Selector.open();

        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        Iterator<SelectionKey> it;
        while (serverSocketChannel.isOpen()) {

            if (selector.select() != 0) {
                it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();


                    if (key.isAcceptable()) { // on new connection

                        Client channelClient = new Client(serverSocketChannel.accept());

                        channelClient.getChannel().configureBlocking(false);
                        channelClient.getChannel().register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                        LOGGER.info("New Connection from " + channelClient.getGuestName());
                        clients.add(channelClient);

                        this.sendRequest("SERVER_AUTHENTICATE", channelClient);

                    } else if (key.isReadable()) { // on new message
                        this.handleIncomingBytes(key);
                    } else if (key.isWritable()) {
                        //this.handleOutcomingBytes(key); // when we send an message
                    }

                    it.remove();
                }
            }
        }
    }

    private void handleIncomingBytes(SelectionKey key) {
        Optional<Client> client = clients.stream()
                .filter(c -> c.getChannel().equals(key.channel()))
                .findFirst();

        if (client.isEmpty()) {
            return;
        }

        String message = "";
        try {
            SocketChannel socketChannel = client.get().getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            int bytesRead = socketChannel.read(buffer);
            if (bytesRead == -1) {
                socketChannel.close();
                return;
            }

            buffer.flip(); // flip on TCP
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            
            message = new String(bytes, StandardCharsets.UTF_8);
            Packet receivedPacket = new Gson().fromJson(message, Packet.class);
            receivedPacket.setClient(client.get());

            processIncomingPacket(receivedPacket); // Process the packet

        } catch (IOException e) {
            LOGGER.error("Error reading data from client: " + client.get().getGuestName(), e);
            try {
                key.cancel();
                key.channel().close();
                clients.remove(client);
            } catch (IOException ex) {
                LOGGER.error("Error closing socket channel: ", ex);
            }
        } catch (Exception e) {
            LOGGER.info("Message:{}", message);
        }
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



    private void sendRequest(String requestTemplate, Client client) {

    }
}
