/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import java.util.HashMap;

/**
 *
 * @author zurriyot
 */
public class ShapeDrawer {
    public static void draw(int x, int y, HashMap points) {
        for (int i = 0; i < y; i++) {
            int index = 0;
            for (int j = 0; j < x; j++) {
                if (points.containsKey(i+index)) {
                    if ((i+index)<=9) {
                        System.out.print("   "+(i+index)+" ");
                    }
                    else if ((i+index)/10<=9) {
                        System.out.print("  "+(i+index)+" ");
                    } else if ((i+index)/100<=9) {
                        System.out.print(" "+(i+index)+" ");
                    }else if ((i+index)/1000<=9) {
                        System.out.print((i+index)+" ");
                    }
                } else {
                    System.out.print("---- ");
                }
                index+=y;
            }
            System.out.println("");
        }
        System.out.println("");
    }
}
