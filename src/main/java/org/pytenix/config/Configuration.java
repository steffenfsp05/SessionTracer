package org.pytenix.config;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Configuration implements Serializable {


    int maxAccountsPerIp;
    String staffPermission;
    String bypassPermission;
    String kickAltMessage;
    String staffNotifyMessage;



    public static Configuration defaultConfiguration()
    {
        return new Configuration(2,
                "trace.alert",
                "trace.bypass",
                "Multiple Accounts detected",
                "An User with an Alt Account detected! [Username: %s, IP-Address: %s]"
        );
    }
}
