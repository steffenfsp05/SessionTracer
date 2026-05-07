package org.pytenix.config;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Configuration implements Serializable {


    String prefix;
    int maxAccountsPerIp;
    String staffPermission;
    String commandPermission;
    String kickAltMessage;
    String staffNotifyMessage;


    public static Configuration defaultConfiguration() {
        return new Configuration(
                "§7[§cSessionTracer§7]",
                2,
                "trace.alert",
                "trace.command",
                "§cMultiple Accounts detected",
                "§cAlt Account detected! User: §4%name% §7| §cIP: §4%ipaddress% §7| §cTotal Accounts: §4%knownaccounts% §7| §cKnown Alts: §4%duplicatenames%"
        );
    }
}
