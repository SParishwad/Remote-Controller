package com.erz.joystick;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class TCPClient extends AsyncTask< Float, String, Void> {
    public static final String SERVER_IP = "192.168.4.1";                                           // Server IP address. NodeMCU default IP address
    public static final int SERVER_PORT = 80;
    private Socket socket;                                                                          // Combination of Server IP address and Port
    private String serverMessage;                                                                   // message to be sent to the server
    private onMessageReceived messageListener = null;                                               // Sends message received notifications
    private PrintWriter bufferOut;                                                                  // used to send messages to the server
    private BufferedReader bufferIn;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(/*onMessageReceived listener*/) {
        //messageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (bufferOut != null) {
                    Log.d("TCP Client: ", "Sending: " + message);
                    bufferOut.println(message);
                    bufferOut.flush();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Override
    protected Void doInBackground(Float... floats) {
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            Log.d("TCP Client", "C: Connecting...");

            socket = new Socket(serverAddr, SERVER_PORT);
            socket.setKeepAlive(true);

            bufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            if((serverMessage = bufferIn.readLine()) != null){
                messageListener.messageReceived(serverMessage);
                Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopClient();
        return null;
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {
        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (bufferOut != null) {
            bufferOut.flush();
            bufferOut.close();
        }

        messageListener = null;
        bufferIn = null;
        bufferOut = null;
        serverMessage = null;
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
    //class at on AsyncTask doInBackground
    public interface onMessageReceived {
        void messageReceived(String message);
    }
}
