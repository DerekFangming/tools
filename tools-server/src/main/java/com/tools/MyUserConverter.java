package com.tools;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class MyUserConverter extends DefaultUserAuthenticationConverter {
    public Authentication extractAuthentication(Map<String, ?> map) {
//        if (map.contenttainsKey(USERNAME)) {
//            // Object principal = map.get(USERNAME);
//            Collection<? extends GrantedAuthority> authorities = getAuthorities(map);
////            UserDto utente = new UserDto();
////            utente.setUsername(map.get(USERNAME).toString());
////            utente.setUfficio(map.get("ufficio").toString());
////            utente.setExtraInfo(map.get("Informazione1").toString());
////            utente.setNome(map.get("nome").toString());
////            utente.setCognome(map.get("cognome").toString());
////            utente.setRuolo(map.get("ruolo").toString());
//
//            return new UsernamePasswordAuthenticationToken(utente, "N/A", authorities);
//        }
        Collection<? extends GrantedAuthority> authorities = Collections.emptyList();
        return new UsernamePasswordAuthenticationToken(map, "N/A", authorities);
    }
}