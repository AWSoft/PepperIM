/******************************************************
 * Copyright (C) 2011 Anton Pirogov, Felix Wiemuth    *
 * Licensed under the GNU GENERAL PUBLIC LICENSE      *
 * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
 ******************************************************/

package pepperim.backend.snserver;

import pepperim.Main;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import pepperim.backend.snserver.util.ChangeRequest;
import pepperim.backend.snserver.util.RawMessage;

/**
 * Class SNServer (Simple non-blocking Server).
 * Creating an object starts the server.
 * @author Felix Wiemuth
 */
public class SNServer extends Thread {
    private int port;
    private Selector selector;
    private ConcurrentMap<InetSocketAddress, SelectionKey> keys; // ID -> associated key
    private ConcurrentMap<SocketChannel,List<byte[]>> to_send; //channel -> data to be send from this channel
    private StringBuilder in_buffer; //buffer to incoming data until seperator is found and messages are stored in 'in_msg'
    private ConcurrentLinkedQueue<RawMessage> in_msg; //incoming messages to be fetched by messenger thread
    private ConcurrentLinkedQueue<ChangeRequest> pendingChanges; //changes to do before 'Selector.select()' is called

    /**
     * Create a server.
     * @param port port on which the server will run
     */
    public SNServer(int port) {
        this.port = port;
        to_send = new ConcurrentHashMap<SocketChannel,List<byte[]>>();
        in_buffer = new StringBuilder();
        in_msg = new ConcurrentLinkedQueue<RawMessage>();
        pendingChanges = new ConcurrentLinkedQueue<ChangeRequest>();
        keys = new ConcurrentHashMap<InetSocketAddress, SelectionKey>();
        start();
    }

    private void start_server() throws IOException {
        // create selector and channel
        selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // bind to port
        InetSocketAddress listenAddr = new InetSocketAddress((InetAddress)null, port);
        serverChannel.socket().bind(listenAddr);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        Main.log("Echo server ready. Ctrl-C to stop.");
        main();
    }

    /**
     * Servers main loop
     * @throws IOException
     */
    private void main() throws IOException {
        while (true) {
            process_changes();
            do_selection();
        }
    }

    private void process_changes() throws IOException {
        while (true) {
                ChangeRequest change = pendingChanges.poll();
                if (change == null)
                    break;
                //process current change
                switch (change.type) {
                    case CHANGEOPS:
                        SelectionKey key = change.channel.keyFor(this.selector);
                        key.interestOps(change.ops);
                        break;
                    case REGISTER:
                        change.channel.register(this.selector, change.ops);
                        break;
                }
        }
    }

    private void do_selection() throws IOException {
        // wait for events
        // timeout 1000ms -> check for pendingChanges at least every second
        if (selector.select(1000) == 0)
            return;

        // wakeup to work on selected keys
        Iterator keys = selector.selectedKeys().iterator();
        while (keys.hasNext()) {
            SelectionKey key = (SelectionKey) keys.next();
            keys.remove();

            if (! key.isValid()) {
                continue;
            }

            if (key.isAcceptable()) {
                accept(key);
            }
            else if (key.isReadable()) {
                read(key);
            }
            else if (key.isWritable()) {
                write(key);
            }
            else if(key.isConnectable()) {
                connect(key);
            }
        }
    }

    /**
     * Register the channel/key of a new connection with Selector and Maps for
     * further IO
     * @param key Key
     */
    private void register_connection(SocketChannel channel) throws IOException {
        SelectionKey key = channel.register(selector, SelectionKey.OP_READ); //TODO necessary? (OP_READ)
        to_send.put(channel, new ArrayList<byte[]>());
        //store key in 'keys' to be accessable by ID from messenger thread //TODO first get ID
        keys.put((InetSocketAddress) channel.socket().getRemoteSocketAddress(), key);
        //send_message(key, "MSG after register_connection"); //DEBUG
    }

    /**
     * Procedure OP_ACCEPT of selector
     * @param key Key of this server
     * @throws IOException
     */
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);

        //send_message(key, "Welcome."); //DEBUG
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        Main.log("Connected to: " + remoteAddr); //DEBUG

        register_connection(channel);
    }

    /**
     * Procedure OP_CONNECT of selector
     * @param key Connectable key
     */
    private void connect(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            channel.finishConnect(); //try to finish connection - if 'false' is returned keep 'OP_CONNECT' registered
            register_connection(channel);
//            send_message(key, "This is a test message."); //DEBUG
        }
        catch (IOException e0) {
            try {
                //TODO handle ok?
                if (e0 == null)
                    channel.close();
            }
            catch (IOException e1) {
                //TODO handle
            }
        }

    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(65536);
        int numRead = -1;
        try {
            numRead = channel.read(buffer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (numRead == -1) {
            to_send.remove(channel);
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            Main.log("Connection closed by client: " + remoteAddr); //TODO handle
            channel.close();
            return;
        }

        //add new data to 'in_buffer'
        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        in_buffer.append(new String(data, "utf-8"));

        //check if there is a complete message in 'in_buffer'
        int pos = in_buffer.indexOf("\n");
        if (pos != -1) {
            in_msg.add(new RawMessage(RawMessage.Type.CLIENT_MSG, in_buffer.substring(0, pos)));
            in_buffer.delete(0, pos+1);
        }
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        List<byte[]> pendingData = to_send.get(channel);
        Iterator<byte[]> items = pendingData.iterator();
        while (items.hasNext()) {
            byte[] item = items.next();
            items.remove();
            //TODO is this correct? -> re-doing write until buffer empty
            ByteBuffer buffer = ByteBuffer.wrap(item);
            int bytes_to_write = buffer.capacity();
            while (bytes_to_write > 0) {
                bytes_to_write -= channel.write(buffer);
            }
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private void queue_data(SelectionKey key, byte[] data) {
        SocketChannel channel = (SocketChannel) key.channel();
        List<byte[]> pendingData = to_send.get(channel);
        key.interestOps(SelectionKey.OP_WRITE);

        pendingData.add(data);
    }

    private void send_message(SelectionKey key, String msg) {
        String msg_marked = msg + '\n'; //add seperator to end of message
        try {
            queue_data(key, msg_marked.getBytes("utf-8"));
        }
        catch (UnsupportedEncodingException ex) {
            //is not thrown: utf-8 is always defined
        }
    }

    @Override
    public void run() {
        try {
            start_server();
        }
        catch (IOException e) {
            Main.log("SNServer> IOException: " + e);
            //TODO handle exception
        }
    }

    /**
     * Initiate a connection to a client. Success reports are delivered
     * over the message queue (getMessage()).
     * @param addr The complete internet address to connect to
     */
    public void init_connect(InetSocketAddress addr) {
        try {
            // Create a non-blocking channel
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);

            // Send a connection request to the server; this method is non-blocking
            channel.connect(addr);
            pendingChanges.add(new ChangeRequest(channel, ChangeRequest.Type.REGISTER, SelectionKey.OP_CONNECT));

            in_msg.add(new RawMessage(RawMessage.Type.CLIENT_CONNECTED, addr.getAddress().getHostAddress()+":"+String.valueOf(addr.getPort()) ));
        }
        catch (IOException e) {
            //TODO handle
        }
    }

    /**
     * Send a message to a connected client.
     * @param addr The complete internet address of the receving client
     * @param msg The message to be sent
     */
    public void send_message(InetSocketAddress addr, String msg) {
        SelectionKey key = keys.get(addr);
        if (key != null)
            send_message(key, msg);
        //else
            //TODO handle
    }

    /**
     * Poll the queue of incoming messages, including different types
     * defined in {@link util.RawMessage.Type}.
     * @return The next message in queue or {@link null} if queue is empty.
     */
    public RawMessage getMessage() {
        return in_msg.poll();
    }

}
