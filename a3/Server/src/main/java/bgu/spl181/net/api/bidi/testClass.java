package bgu.spl181.net.api.bidi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class testClass {

    public static void main(String[] args) {
        String a = JsonHandler.getMovie("South Park: Bigger, Longer & Uncut", "id");
        System.out.println(a);
    }
}
