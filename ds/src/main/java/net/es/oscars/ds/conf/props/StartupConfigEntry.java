package net.es.oscars.ds.conf.props;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import net.es.oscars.ds.conf.StartupCfgSerializer;

import java.io.Serializable;

@Data
@JsonSerialize(using = StartupCfgSerializer.class)
public class StartupConfigEntry implements Serializable {
    public StartupConfigEntry() {

    }
    String name;

    Integer server_port;
    Boolean ssl_enabled;
    String ssl_key_store;
    String ssl_key_store_type;
    String ssl_key_store_password;
    String ssl_key_alias;
    String ssl_key_password;
    String ssl_ciphers;

    Boolean sec_basic_enabled;
    String sec_user_name;
    String sec_user_password;

}
