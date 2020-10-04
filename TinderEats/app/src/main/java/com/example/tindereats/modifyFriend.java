package com.example.tindereats;

import android.os.AsyncTask;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// modifies friend list on database
// Accepts the user's uid, the modified friend's username, and a string "True" or "False"
// if "True", accept the friend request, if "False", reject the friend request/remove the friend
public class modifyFriend extends AsyncTask<String, String, Response> {

    @Override
    protected Response doInBackground(String... strings) {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        String uid = strings[0];
        String friendUsername = strings[1];
        String accept = strings[2];
        String url;
        if(accept.equals("True")) {
            url = App.getContext().getResources().getString(R.string.ServerAddress) + "acceptFriend";
        }
        else {
            url = App.getContext().getResources().getString(R.string.ServerAddress) + "rejectFriend";
        }
        MediaType mediaType = MediaType.get("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "senderUid=" + uid + "&friendUsername=" + friendUsername);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(Response result) {
        if(result != null) {
            String resp = null;
            try {
                resp = result.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {  // no response sent back

        }
    }
}