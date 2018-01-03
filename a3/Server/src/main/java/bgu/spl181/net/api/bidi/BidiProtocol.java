package bgu.spl181.net.api.bidi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class BidiProtocol implements BidiMessagingProtocol<String> {
    private static ConcurrentHashMap<String, AtomicBoolean> loglog = new ConcurrentHashMap<String, AtomicBoolean>();
    private boolean shouldTerminate = false;

    private boolean isLoggedIn;
    private int connectionId;
    private Connections connections;


    @Override
    public void start(int connectionId, Connections connections) {
        this.connectionId = connectionId;
        this.connections = connections;
        this.isLoggedIn = false;
    }

    @Override
    public void process(String message) {
        String msg = "";
        String dataholder = "";

        if (message.substring(0, 8).equals("REGISTER")) {
            msg = "REGISTER";
            dataholder = message.substring(9);
        } else if (message.substring(0, 5).equals("LOGIN")) {
            msg = "LOGIN";
            dataholder = message.substring(6);
        } else if (message.substring(0, 7).equals("REQUEST")) {
            msg = "REQUEST";
            dataholder = message.substring(8);
        }
        switch (msg) {
            case "SIGNOUT":
                shouldTerminate = true;
                connections.disconnect(connectionId);
                break;
            case "REGISTER": {
                String[] data = dataholder.split(" ");
                boolean success;
                if (isLoggedIn || loglog.get(data[0]) == null)
                        success = MessageHandler.register(data);
                else success = false;
                if(success){
                    connections.send(connectionId, "ACK registration succeeded");
                }
                else
                    connections.send(connectionId, "ERROR registration failed");
                /*
                data[0] - user name
                data[1] - password
                data[2] - Data Block - country="<country name">
                 */
                //todo - Add user to data base. ack?
            }
            break;
            case "LOGIN": {
                String[] data = dataholder.split(" ");
                boolean success;
                synchronized (loglog) {
                    if (isLoggedIn || (loglog.get(data[0]) != null && loglog.get(data[0]).get()))
                        success = false;
                    else success = MessageHandler.login(data);
                    if (success) {
                        if (loglog.get(data[0]) != null)
                            loglog.get(data[0]).set(true);
                        else
                            loglog.put(data[0], new AtomicBoolean(true));
                        this.isLoggedIn = true;
                        connections.send(connectionId, "ACK login succeeded");
                    } else
                        connections.send(connectionId, "ERROR login failed");
                }
                /*
                data[0] - user name
                data[1] - password
                 */
                //todo - Check if user is in DB and add him to connections. ack?
            }
            break;
            case "REQUEST": {
                String[] data = dataholder.split(" ");
                /*
                data[0] - name
                data[1] - parameters
                 */
                //todo - implement. ack?
            }
            break;

        }

    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }


}
