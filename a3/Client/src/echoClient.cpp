#include <stdlib.h>
#include "../include/connectionHandler.h"
#include <boost/thread.hpp>

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/



class SocketTask{
    private:
        ConnectionHandler &_connectionHandler;

    public:
    SocketTask (ConnectionHandler &connectionHandler ) : _connectionHandler(connectionHandler) {}

    void operator()(){
        while(1){
            std::string answer;//todo port

                if (!_connectionHandler.getLine(answer)) {//todo port
                    std::cout << "Disconnected. Exiting...\n" << std::endl;
                    break;
                }

            int len = answer.length();
            answer.resize(len-1);
            std::cout << "Reply: " << answer << " " << len << " bytes " << std::endl << std::endl;
            if (answer == "bye") {
                std::cout << "Exiting...\n" << std::endl;
                break;
            }

        }


    }

};

class KeyboardTask{
private:
    ConnectionHandler &_connectionHandler;

public:
    KeyboardTask (ConnectionHandler &connectionHandler ) : _connectionHandler(connectionHandler) {}

    void operator()(){
    while (1) {

        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);//todo get line into buffer keyboard
        std::string line(buf);//todo create line out of buffer keyboard
        int len = line.length();//todo keyboard


        if (!_connectionHandler.sendLine(line)) {//todo send line port
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        std::cout << "Sent " << len + 1 << " bytes to server" << std::endl;//todo port

    }
    }

};




int main (int argc, char *argv[]) {


    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }//todo check arguments main
    std::string host = argv[1];
    short port = atoi(argv[2]);
    
    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {//todo connect to port
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }


    KeyboardTask keyboardTask(connectionHandler );
    SocketTask socketTask(connectionHandler );
    boost::thread thkeyboard(keyboardTask);
    boost::thread thsocket(socketTask);
    thkeyboard.join();
    thsocket.join();



    return 0;
}
