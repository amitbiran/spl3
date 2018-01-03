package bgu.spl181.net.api.bidi;

import com.google.gson.internal.LinkedTreeMap;

public class MessageHandler {

    public static boolean register (String[] data){
        if (data.length == 2)
            return JsonHandler.addUser(data);
        if (data.length == 3 && data[2].substring(0, 8).compareTo("country=") == 0){
            data[2] = data[2].substring(8);
            return JsonHandler.addUser(data);
        }
        return false;
    }

    public static boolean login(String[] data) {
        if (data.length > 2)
            return false;
        else {
            boolean ans = false;
            LinkedTreeMap user = JsonHandler.getUser(data[0]);
            if (user != null && ((String)user.get("password")).compareTo(data[1]) == 0)
                ans = true;
            return ans;
        }
    }


}