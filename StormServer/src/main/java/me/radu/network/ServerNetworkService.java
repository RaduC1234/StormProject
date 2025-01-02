package me.radu.network;

import me.radu.core.ServerInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServerNetworkService {

    private static final Logger LOGGER = LogManager.getLogger(ServerNetworkService.class);

    private List<Client> clients;
    private List<IRequestHandler> requestTemplates;

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private int port;
    private ServerInstance serverInstance;

    public ServerNetworkService(ServerInstance serverInstance) {
        this.serverInstance = serverInstance;
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
                    it.remove();
                }
            }
        }
    }

    private void handle(SelectionKey key) {

    }
}
