package net.vector57.android.mrpc;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.vector57.mrpc.MRPC;
import net.vector57.mrpc.Result;

import java.io.UnsupportedEncodingException;

/**
 * Created by Alex Sherman on 7/15/2017.
 */

public class VolleyMRPC {
    private RequestQueue requestQueue;
    public VolleyMRPC(Context ctx) {
        requestQueue = Volley.newRequestQueue(ctx);
    }
    public void rpc(String url, final String path, final Object value, final Result.Callback callback) {
        StringRequest jsObjRequest = new StringRequest
                (Request.Method.POST, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        if(callback != null)
                            callback.onSuccess(MRPC.gson().fromJson(response.toString(), JsonElement.class));
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
            @Override
            public byte[] getBody() throws AuthFailureError {
                JsonObject args = new JsonObject();
                args.add("path", MRPC.gson().toJsonTree(path));
                args.add("value", MRPC.gson().toJsonTree(value));
                try {
                    return MRPC.gson().toJson(args).getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        requestQueue.add(jsObjRequest);
    }
}
