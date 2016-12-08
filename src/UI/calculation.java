/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package UI;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.swing.JTextField;

/**
 *
 * @author see-l
 */
public class calculation {

    int pwLength = 0;
    String charset = "";
    int chainTable = 0;
    int chainLength = 0;

    String startChain = "";
    String endChain = "";

    String hash = "";
    HashMap<String, String> hashTable = new HashMap<String, String>();

    public calculation(String charset, int pwLength, int chainTable, int chainLength) {
        this.charset = charset;
        this.pwLength = pwLength;
        this.chainTable = chainTable;
        this.chainLength = chainLength;
    }

    public int calcPossiblePassword() {//calculate the possible outcomes based on the sample size and password length
        Double dPwSpace = Math.pow(charset.length(), pwLength);
        return dPwSpace.intValue();
    }

    public String calculateChain(String currentEndChain) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String hashed;
        for (int j = 0; j < chainLength; j++) {//loop whole process based on the chain Length
            hashed = Sha1(currentEndChain);//hash the input
            int reduced = reduce(hashed);//reduce the hashed
            currentEndChain = intToPwd(reduced);//convert the reduce to string
            System.out.println(currentEndChain);//print all the chain string for debugging purpose
        }
        return currentEndChain;//new end chain
    }

    public void createTable() throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        for (int i = 0; i < chainTable; i++) {//loop whole process based on numbers of chain per table
            startChain = generateRandomChar();//generate start chain ; random string
            endChain = calculateChain(startChain);//the end chain produced by calculate chain
            hashTable.put(endChain, startChain);//put into hash table
        }//end of chain per table  
        try {//serialize the table
            FileOutputStream fileOut = new FileOutputStream(calcPossiblePassword() + "-" + chainLength + "-" + pwLength + ".ser");//generate the serialize file
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(hashTable);//write the content in hashtable into the file
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    public String generateRandomChar() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < pwLength; i++) {//generate string based on the password length
            sb.append(charset.charAt(random.nextInt(charset.length())));//generate random string based on charset
        }
        return sb.toString();
    }

    public int reduce(String h) {
        BigInteger b = new BigInteger(h, 16);//convert the hash to big integer
        BigInteger p = new BigInteger(String.valueOf(calcPossiblePassword()));//convert password size to big integer
        b = b.mod(p);//mod the password size
        int i = Math.abs(b.intValue());//make it positive if it is negative
        return i;
    }

    public String intToPwd(int start) {//take the int generated by sha then generate new digit
        int remainder;
        int base = charset.length();
        String s = "";
        int count = 0;
        char[] c = charset.toCharArray();

        while (start >= 0) {//start reduce num
            remainder = start % base;//get the position of the number
            String newC = String.valueOf(c[remainder % base]);
            s = newC + s; //add new character
            start = start / base;//get the remaining and divide the base to get a new reduce num
            count++;
            if (count == pwLength) {
                break;
            }
        }
        return s;//return new character
    }

    //SHA1 Generator from : http://stackoverflow.com/questions/4895523/java-string-to-sha1
    public String Sha1(String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        byte[] result = md.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public void readFromFile(String file,String h, JTextField pwd) throws IOException, NoSuchAlgorithmException {
        try {
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            hashTable = (HashMap) in.readObject();
            Set set = hashTable.entrySet();
            Iterator iterator = set.iterator();

            while (iterator.hasNext()) {
                Map.Entry mentry = (Map.Entry) iterator.next();
                endChain = (String) mentry.getKey();
                startChain = (String) mentry.getValue();
                hashTable.put(endChain,startChain);
                
                readTable( h,  pwd);
                //System.out.println("s: "+startChain+"e: "+endChain);
            }
            in.close();
            fileIn.close();
        } catch (ClassNotFoundException c) {
            System.out.println("File not found");
        }
    }

    public void readTable(String h, JTextField pwd) throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        String endPoint = "";
        String hashed = "";
        //System.out.println("s: "+startChain+"e: "+endChain);
        int reduces = reduce(h);//reduce the hash
        endPoint = intToPwd(reduces);//string generated based on the given hash

        for (int i = 0; i < chainLength; i++) {
            hashed = Sha1(endPoint);//hash the String
            int reduced = reduce(hashed);//reduce the String
            endPoint = intToPwd(reduced);//get the new String produced by the hash
            String startpt = hashTable.get(endPoint);//acess the value with the key
            if (startpt != null) {//if not equal
                //System.out.println("Start Plaintext: " + startpt);//get Start Point by accessimg the key
                String newEnd = startpt;
                while (true) {//then loop until get the hash equal the given hash                    
                    hashed = Sha1(newEnd);//hash the newEnd
                    if (h.equals(hashed)) {//if the hash equals to the hash generated by the String
                        //System.out.println("Password: " + newEnd);
                        pwd.setText(newEnd);//
                        return;
                    }
                    int reduce = reduce(hashed);//reduce the startpt
                    newEnd = intToPwd(reduce);//get new String 
                }
            }
        }
    }
}
