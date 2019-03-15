/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package queryBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author zurriyot
 */
public class Helper {
    
    public static String getQuery(String fileName) {
        File file = new File("src/queries/"+fileName);
        String query = "";
        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNext()) {
                query+=sc.nextLine();
            }
            sc.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return query;
    }
    
    public static String httpGet(String urlStr) {
        URL url;
        String format = "application/gml+xml";
        HttpURLConnection conn;
        StringBuilder sb = new StringBuilder();
        try {
            url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.addRequestProperty("Accept", format);
            if (conn.getResponseCode() != 200) {
                return null;
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line + '\n');
            }
            rd.close();
            conn.disconnect();
        } catch (IOException ex) {
            Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }
}
