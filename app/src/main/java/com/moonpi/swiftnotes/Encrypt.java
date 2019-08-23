package com.moonpi.swiftnotes;

/**
 * Created by aman on 24/9/17.
 */

public class Encrypt
{
    public static StringBuffer encrypt(String text, int s) {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < text.length(); i++) {
            if (Character.isUpperCase(text.charAt(i)))  //if notes contains upper case letters
            {
                char ch = (char) (((int) text.charAt(i) +
                        s - 65) % 26 + 65);
                result.append(ch);
            } else if(Character.isLowerCase(text.charAt(i))) //if notes contains lower case letters
                {
                char ch = (char) (((int) text.charAt(i) +
                        s - 97) % 26 + 97);
                result.append(ch);
            }
            else if(Character.isDigit(text.charAt(i)))    //if notes contains digits
            {
                char ch=(char)((text.charAt(i)+s-48)%26+48);
                result.append(ch);
            }
            else if((int)text.charAt(i)==32) //if notes contain whitespace
            {
                char ch=(text.charAt(i));
                result.append(ch);
            }



        }


        return (result);
    }
}
