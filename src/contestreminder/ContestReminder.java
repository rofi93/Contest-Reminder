/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contestreminder;

import java.io.*;

/**
 *
 * @author USER PC
 */
public class ContestReminder {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        SelectContests sc = new SelectContests();
        sc.setVisible(true);
    }
}
