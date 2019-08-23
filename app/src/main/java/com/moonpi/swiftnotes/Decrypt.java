package com.moonpi.swiftnotes;

/**
 * Created by aman on 24/9/17.
 */
//decryption is done according to n
//encryption will be done according to 26-n
public class Decrypt
{
    public static StringBuffer decrypt(String text, int s) {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < text.length(); i++) {
            if (Character.isUpperCase(text.charAt(i))) {
                char ch = (char) (((int) text.charAt(i) +
                        s - 65) % 26 + 65);
                result.append(ch);
            } else {
                char ch = (char) (((int) text.charAt(i) +
                        s - 97) % 26 + 97);
                result.append(ch);
            }
        }

        StringBuffer result2=Encrypt.encrypt(text,(26-s)); //decrypted text=encrypted(n)+encrypted(26-n)
        return (result2);
    }

}
