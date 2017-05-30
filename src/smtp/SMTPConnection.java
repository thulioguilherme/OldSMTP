/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smtp;

import java.net.*;
import java.io.*;

/**
 * Open an SMTP connection to a mailserver and send one mail.
 *
 */
public class SMTPConnection {
    /* The socket to the server */
    private final Socket connection;

    /* Streams for reading and writing the socket */
    private final BufferedReader fromServer;
    private final DataOutputStream toServer;

    private static final int SMTP_PORT = 2525;
    private static final String CRLF = "\r\n";
    
    /* Are we connected? Used in close() to determine what to do. */
    private boolean isConnected = false;

    /* Create an SMTPConnection object. Create the socket and the 
       associated streams. Initialize SMTP connection. */
    public SMTPConnection(Envelope envelope) throws IOException {
        //http://www.samlogic.net/articles/smtp-commands-reference.htm
        System.out.println("Opening connection with server.");
        
        connection = new Socket(envelope.DestAddr, SMTP_PORT);
        
        fromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        toServer =   new DataOutputStream(connection.getOutputStream());
        isConnected = true;
	
        
        System.out.println("\nLOG:\n");
        
        /* Fill in */
        /* Read a line from server and check that the reply code is 220.
	   	If not, throw an IOException. */
        String in = fromServer.readLine();
        if(parseReply(in) != 220){
            isConnected = false;
            throw new IOException("Reply code different from expect. Expected: 220 / Got: "+ in +".");
        }
        
        /* Fill in */
        /* SMTP handshake. We need the name of the local machine.
	   	Send the appropriate SMTP handshake command. */
        InetAddress ia = InetAddress.getLocalHost();
        String localhost = ia.getHostAddress();
        try{
        	sendCommand("HELO "+ localhost + CRLF, 250);
        }catch(IOException e){
            isConnected = false;
            System.out.println("Error on SMTP handshake. "+ e);
        }
    }
    
    /* Send the message. Write the correct SMTP-commands in the
       correct order. No checking for errors, just throw them to the
       caller. */
    public void send(Envelope envelope) throws IOException {
	/* Fill in */
	/* Send all the necessary commands to send a message. Call
	   sendCommand() to do the dirty work. Do _not_ catch the
	   exception thrown from sendCommand(). */
        if(!isConnected){
            throw new IOException("Tried to send message without being connected.");
        }
        
        sendCommand("MAIL FROM: <"+ envelope.Sender + "> "+ CRLF, 250);
        sendCommand("RCPT TO: <"+ envelope.Recipient + "> "+ CRLF, 250);
        sendCommand("DATA " + CRLF, 354);
        sendCommand(envelope.Message.toString()+ " " + CRLF+"."+CRLF, 250);       
    }

    /* Close the connection. First, terminate on SMTP level, then
       close the socket. */
    public void close() {
    	isConnected = false;
    	try {
    		sendCommand("QUIT "+ CRLF, 221);
    		connection.close();
    	} catch (IOException e) {
    		System.out.println("Unable to close connection: "+ e);
    		isConnected = true;
    	}
    }

    /* Send an SMTP command to the server. Check that the reply code is
       what is is supposed to be according to RFC 821. */
    private void sendCommand(String command, int rc) throws IOException {
	/* Fill in */
	/* Write command to server and read reply from server. */
        toServer.writeBytes(command);
        System.out.println("C: "+ command);
        
        String fs = fromServer.readLine();
        System.out.println("S: "+ fs);
        
        int rc_server = parseReply(fs);
       
	/* Fill in */

	/* Fill in */
	/* Check that the server's reply code is the same as the parameter
	   rc. If not, throw an IOException. */
        
        if(rc_server != rc){
            throw new IOException("Error on execute command: "+ command +"Server's reply code is different from passed as parameter. Expected: "+ rc +" / Got: "+ rc_server +".");
        }
        
	/* Fill in */
    }

    /* Parse the reply line from the server. Returns the reply code. */
    private int parseReply(String reply) {
        if(reply != null){
            String[] split = reply.split(" ");
            return Integer.parseInt(split[0]);
        }else{
            return 0;
        }
    }

    /* Destructor. Closes the connection if something bad happens. */
    @Override
    protected void finalize() throws Throwable {
	if(isConnected) {
	    close();
	}
	super.finalize();
    }
}
