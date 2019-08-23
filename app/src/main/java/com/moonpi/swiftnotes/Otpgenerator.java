package com.moonpi.swiftnotes;

import java.util.Random;

/**
 * Created by aman on 27/8/17.
 */

public class Otpgenerator
{
    static public char[] sendotp(int length)
    {
        String number="0123456789";
        Random r=new Random();
        char[] otp=new char[length];
        for(int i=0;i<length;i++)
        {
            otp[i]=number.charAt(r.nextInt(number.length()));
        }
        return otp;
    }



   /* public char[] main()
    {
        char[] otp1=sendotp(6);
        return  otp1;
    }*/
}
