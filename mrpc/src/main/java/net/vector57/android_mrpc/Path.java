package net.vector57.android_mrpc;

/**
 * Created by Vector on 11/12/2016.
 */

public class Path {
    public String name;
    public String procedure;

    public static Path FromString(String string) {
        Path path = null;
        String[] splitted = string.split(".");
        if(splitted.length == 2) {
            path = new Path();
            path.name = splitted[0];
            path.procedure = splitted[1];
        }
        else
        {
            //Check UUID case
        }
        return path;
    }

    public boolean isWildcard(){
        return name.equals("*");
    }
}
