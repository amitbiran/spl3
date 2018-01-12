#include <stdlib.h>
#include "../include/connectionHandler.h"
#include <boost/thread.hpp>

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/



class SocketTask{
    private:
        ConnectionHandler &_connectionHandler;
        std::atomic<bool> &cont_;
    public:
    SocketTask (ConnectionHandler &connectionHandler,std::atomic<bool> &cont ) : _connectionHandler(connectionHandler) , cont_(cont){}

    void operator()(){
        while(_connectionHandler.run_){
            cont_ = true;
            std::string answer;//todo port

                if (!_connectionHandler.getLine(answer)) {//todo port
                    std::cout << "Disconnected. Exiting...\n" << std::endl;
                    break;
                }

            int len = answer.length();
            answer.resize(len-1);
            std::cout <<  answer  << std::endl;
            if (answer == "ACK signout succeeded") {
                _connectionHandler.close();
                cont_ = false;
                std::cout << "Exiting...\n" << std::endl;
                break;
            }

        }


    }

};

class KeyboardTask{
private:
    ConnectionHandler &_connectionHandler;
    std::atomic<bool> & cont_;
public:
    KeyboardTask (ConnectionHandler &connectionHandler , std::atomic<bool> &cont ) : _connectionHandler(connectionHandler) , cont_(cont) {}

    void operator()(){
    while (_connectionHandler.run_) {

        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);//todo get line into buffer keyboard
        std::string line(buf);//todo create line out of buffer keyboard
      //  int len = line.length();//todo keyboard


        if (!_connectionHandler.sendLine(line)) {//todo send line port
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        if(line.compare("SIGNOUT") == 0){
            while(cont_);
        }
       // std::cout << "Sent " << len + 1 << " bytes to server" << std::endl;//todo port

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
    std::atomic<bool> run(true);
    std::atomic<bool>* run1 = &run;
    ConnectionHandler connectionHandler(host, port , *run1);
    if (!connectionHandler.connect()) {//todo connect to port
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    std::atomic<bool> cont(true);
    std::atomic<bool> *contp = &cont;
    KeyboardTask keyboardTask(connectionHandler ,*contp);
    SocketTask socketTask(connectionHandler, *contp );
    boost::thread thkeyboard(keyboardTask);
    boost::thread thsocket(socketTask);
    thsocket.join();
    thkeyboard.join();




    return 0;
}
