package guessinggame;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import static java.lang.String.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class ServerHandler implements Runnable {

    private static final int DELAY_SECS = 0;
    private Socket clientSocket;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private String answer = "";
    private final File sourceFile = new File("src/guessinggame/words.txt");
    private final int min = 11;
    private final int max = 25143;
    private int failedAttemptCounter;
    private int scoreCounter = 0;//全局变量，整个过程中不再初始化
    private char[] template;

    ServerHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        int secsToMillis = 1000;
        String msg = null;

        //create connection to client
        connect();
        /////////////////////////////////////////////////////////////
        //server keeps listening to client
        while (true) {
            try {
                
                msg = receiveMsgFromClient();
                String[] msgList = msg.split(" ");
                System.out.println(msg);
                Thread.sleep(DELAY_SECS * secsToMillis);
                //start game
                if (msg.equals("*start")) {
                    //initialize
                    initialization();
                    ////////////////////////////
                    System.out.println(answer);
                } //close game
                else if (msg.equals("*close")) {
                    in.close();
                    out.close();
                    break;
                } //msg is guess for the answer
                else if (msgList[0].equals("*hint")) {
                    String replayHint = Hint();
                    sendReply(replayHint);
                } else if (msgList[0].equals("*Add")) {
                    String ReplyADD = AddWord(msgList[1]);
                    sendReply(ReplyADD);
                } else if (msgList[0].equals("*getAnswer")) {
                    sendReply(answer);
                    
                }
                else {
                    String reply = evaluateGuess(msg);
                    sendReply(reply);
                }
                
            } catch (IOException | InterruptedException e) {
                System.out.println(e.toString());
                break;
            }
        }
        //////////////////////////////////////////////////////////////
    }

    //initialize in and out
    void connect() {
        try {
            in = new BufferedInputStream(clientSocket.getInputStream());
            out = new BufferedOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    //initialize answer, template and failed attempt counter, and send template and failedAttemptCounter to client
    void initialization() throws IOException {
        String result = null;

        //initialize answer
        createAnswer();
        //initialize template(the current view)
        template = new char[answer.length()];
        for (int i = 0; i < answer.length(); i++) {
            template[i] = '-';
        }
        failedAttemptCounter = 10;

        result = "uncompleted" + "," + String.valueOf(template) + "," + String.valueOf(failedAttemptCounter);
        sendReply(result);
    }

    void createAnswer() throws IOException {
        Random random = new Random();
        int lineNumber = random.nextInt(max) % (max - min + 1) + min;
        FileReader fr = new FileReader(sourceFile);
        LineNumberReader lnr = new LineNumberReader(fr);
        answer = lnr.readLine();
        while (answer != null) {
            if (lineNumber == lnr.getLineNumber()) {
                break;
            }
            answer = lnr.readLine();
        }
        lnr.close();
        answer = answer.toLowerCase();
    }

    String receiveMsgFromClient() throws IOException {
        String str = null;

        byte[] bt = new byte[256];
        int bytesRead = 0;
        int n;
        while ((n = in.read(bt, bytesRead, 64)) != -1) {
            bytesRead += n;
            if (bytesRead == 256) {
                break;
            }
            if (in.available() == 0) {
                break;
            }
        }
        str = new String(bt);
        //bytesRead is the real length of the bt[], the elements since bt[bytesRead] afterwards are empty.
        str = str.substring(0, bytesRead);
        return str;
    }

    String evaluateGuess(String guess) throws IOException {
        String result = null;

        //guess the whole word
        if (guess.length() > 1) {
            if (guess.equals(answer)) {
                scoreCounter += 1;
                result = "win" + "," + answer + "," + String.valueOf(scoreCounter);
            } else {
                failedAttemptCounter -= 1;
                if (failedAttemptCounter == 0) {
                    scoreCounter -= 1;
                    result = "loose" + "," + String.valueOf(scoreCounter);
                } else {
                    result = "uncompleted" + "," + String.valueOf(template) + "," + String.valueOf(failedAttemptCounter);
                }
            }
        } //guess a letter
        else {
            //guess letter is included in answer
            if (answer.indexOf(guess) != -1) {
                char guessLetter = guess.charAt(0);
                char[] answerLetters = answer.toCharArray();
                for (int i = 0; i < answer.length(); i++) {
                    if (guessLetter == answerLetters[i]) {
                        template[i] = guessLetter;
                    }
                }
                result = String.valueOf(template);

                if (result.equals(answer)) {
                    scoreCounter += 1;
                    result = "win" + "," + answer + "," + String.valueOf(scoreCounter);
                } else {
                    result = "uncompleted" + "," + String.valueOf(template) + "," + String.valueOf(failedAttemptCounter);
                }
            } //guess letter is not included in answer
            else {
                failedAttemptCounter -= 1;
                if (failedAttemptCounter == 0) {
                    scoreCounter -= 1;
                    result = "loose" + "," + String.valueOf(scoreCounter);
                } else {
                    result = "uncompleted" + "," + String.valueOf(template) + "," + String.valueOf(failedAttemptCounter);
                }
            }
        }
        return result;
    }

    void sendReply(String result) {
        try {
            byte[] toClient = result.getBytes();
            out.write(toClient, 0, toClient.length);
            out.flush();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
    
    String Hint() {
        String result = null;
        int loopbreaker = 1;// counter to break out the while loop 
        int randomNum = 0;
        char hintchar = ' ';
        //generete randome number with a range of the word length
        while (loopbreaker == 1) {
            randomNum = (int) (Math.random() * answer.length());
            hintchar = answer.charAt(randomNum);
            System.out.println(hintchar);

            // Check if the letter already guessed or not
            for (int i = 0; i < template.length; i++) {
                if (hintchar == template[i]) {
                    loopbreaker = 1;
                    break;
                } else {
                    loopbreaker--;
                }
            }// for loop
        }// while loop 
        
        // add the letter to the templete and send it to the client
        template[randomNum] = hintchar;
        result = String.valueOf(template);

        return result = String.valueOf(template);
    }

    String AddWord(String word) {
        String Replay = "";
        Scanner in = new Scanner(System.in);
        ArrayList<String> arrayOfStrings = new ArrayList<String>();

        try {

            // put all the words from the dictionary to an array list so it will be easy to search
            BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
            String line = "";
            while ((line = reader.readLine()) != null) {
                arrayOfStrings.add(line);

            }
            reader.close();
            PrintWriter v = new PrintWriter(sourceFile);
            int counter = 0;

            // check if the word already exict in the dictionary
            for (String readline : arrayOfStrings) {
                v.println(readline);
            }
            for (String readline : arrayOfStrings) {
                if (readline.equalsIgnoreCase(word)) {
                    System.out.println("exist");
                    counter = 1;
                    Replay = "not added";
                    break;
                } else {
                    counter = -1;
                    Replay = "Added";
                }
            }
            if (counter == -1) {
                System.out.println("not exist");
                v.println(word);
            }
            v.close();
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

        return Replay;
    }

}
