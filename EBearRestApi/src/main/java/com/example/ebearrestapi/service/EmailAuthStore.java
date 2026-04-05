package com.example.ebearrestapi.service;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmailAuthStore {

    private final Map<String, String> codeStore = new ConcurrentHashMap<>();
    private final Map<String, Boolean> verifiedStore = new ConcurrentHashMap<>();

    public void saveCode(String email, String code){
        codeStore.put(email, code);
    }

    public boolean verifyCode(String email, String code){
        String saved = codeStore.get(email);
        if(saved == null) return false;

        if(saved.equals(code)){
            verifiedStore.put(email, true);
            codeStore.remove(email);
            return true;
        }
        return false;
    }

    public boolean isVerified(String email){
        return verifiedStore.getOrDefault(email, false);
    }

    public void removeVerified(String email){
        verifiedStore.remove(email);
    }
}