package io.sim.bank.server;

import java.util.Date;
import java.sql.Timestamp;

import org.json.JSONObject;

public class BankTransaction {

    JSONObject json;

    public BankTransaction(String login, String action, boolean success, double amount, String recipient){
        Date date = new Date();

        json = new JSONObject();
        json.put("login", login);
        json.put("action", action);
        json.put("success", success);
        json.put("amount", amount);
        json.put("recipient", recipient);
        json.put("timestamp", new Timestamp(date.getTime()));
    }

    JSONObject getJson(){
        return json;
    }
}

