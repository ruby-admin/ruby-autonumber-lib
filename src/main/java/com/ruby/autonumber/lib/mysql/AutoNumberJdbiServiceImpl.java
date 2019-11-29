package com.ruby.autonumber.lib.mysql;

import com.ruby.autonumber.lib.config.MySqlDataSourceProperties;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

public class AutoNumberJdbiServiceImpl implements AutoNumberJdbiService {

    private Jdbi jdbi;

    public AutoNumberJdbiServiceImpl() {
        String username = MySqlDataSourceProperties.getInstance().getJdbcUsername();
        String password = MySqlDataSourceProperties.getInstance().getJdbcPassword();
        String url = MySqlDataSourceProperties.getInstance().getJdbcUrl();
        jdbi = Jdbi.create(url, username, password);
    }

    public String createSequence(String sequenceName, long startNumber) {
        return jdbi.withHandle(handle -> {
            Optional<String> sequence = findIfSequenceExists(handle, sequenceName);
            if(!sequence.isPresent()) {
                handle.execute("CREATE SEQUENCE " + sequenceName + " START WITH " + startNumber + " INCREMENT BY 1 CACHE 1000 MINVALUE 1 MAXVALUE 9999999999");
                return findIfSequenceExists(handle, sequenceName).get();
            }
            return sequence.get();
        });
    }


    private Optional<String> findIfSequenceExists(Handle handle, String sequenceName) {
        return handle.createQuery("SELECT \'" + sequenceName + "\' FROM INFORMATION_SCHEMA.TABLES "
                + "WHERE TABLE_NAME = \'" + sequenceName + "\' AND TABLE_TYPE = \'sequence\'").mapTo(String.class).findFirst();
    }

    public long getNextSequenceValue(String sequenceName) {
        return jdbi.withHandle(handle -> handle.createQuery("SELECT NEXTVAL(" + sequenceName + ")").mapTo(long.class).first());
    }

}
