package bgu.spl181.net.api.bidi;



import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class BidiProtocol implements BidiMessagingProtocol<String> {
    private static ConcurrentHashMap<String, AtomicBoolean> loglog = new ConcurrentHashMap<String, AtomicBoolean>();
    private static ConcurrentHashMap<String, Integer> loglogID = new ConcurrentHashMap<String, Integer>();
    private boolean shouldTerminate = false;
    private String name = "";
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

        if (message.length() >=8 &&message.substring(0, 8).equals("REGISTER")) {
            msg = "REGISTER";
            dataholder = message.substring(9);
        } else if (message.length() >=5 &&message.substring(0, 5).equals("LOGIN")) {
            msg = "LOGIN";
            dataholder = message.substring(6);
        } else if (message.length() >=7 &&message.substring(0, 7).equals("REQUEST")) {
            msg = "REQUEST";
            dataholder = message;
        } else if (message.length() >=7 &&message.substring(0, 7).equals("SIGNOUT")) {
            msg = "SIGNOUT";
            dataholder = message.substring(7);
        }
        switch (msg) {
            case "SIGNOUT":
                if(isLoggedIn) {
                 synchronized (loglog){
              //     shouldTerminate = true;//sould logout from the server
                   loglog.get(name).set(false);
                   loglog.remove(name);//todo delete?
                   connections.send(connectionId, "ACK signout succeeded");
                   connections.disconnect(connectionId);//get him out of connections
                  }
                }
                else connections.send(connectionId, "ERROR signout "+  "failed");


                break;
            case "REGISTER": {
                String[] data = dataholder.split(" ");
                boolean success;
                //todo syncronized open
                if (isLoggedIn || loglog.get(data[0]) == null)//only read from loglog so no problem in here
                        success = MessageHandler.register(data);
                else success = false;
                //todo syncronized close
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
                        else{
                            loglog.put(data[0], new AtomicBoolean(true));
                            loglogID.put(data[0], connectionId);
                        }

                        this.name = data[0];//todo
                        this.isLoggedIn = true;
                        connections.send(connectionId, "ACK login succeeded");
                    } else
                        connections.send(connectionId, "ERROR login failed");
                }
                /*
                data[0] - user name
                data[1] - password
                 */

            }
            break;
            case "REQUEST":
                String[] data = dataholder.split(" ");
                if(!isLoggedIn){
                    String error = new String ("ERROR request " + Request.getRequestType(data) + " failed");
                    connections.send(connectionId, error);
                }
               else {
                    Request r = MessageHandler.request(data, this.name, message);
                    String answer = r.answer;
                    String broadcast = r.broadcast;
                    connections.send(connectionId,answer);


                    if(broadcast != null){
                        for (String k :loglog.keySet()){
                            connections.send(loglogID.get(k), broadcast);
                        }
                        //connections.broadcast(broadcast);
                    }
                } //     System.out.println(message);
            //    System.out.println(data);
                /*
                data[0] - name
                data[1] - parameters
                 */
                //todo - implement. ack?

            break;

        }

    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }


}
