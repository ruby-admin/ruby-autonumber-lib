package com.ruby.autonumber.lib.mysql;

import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

class BinaryUUIDArgument implements Argument {

    private final UUID uuid;

    public BinaryUUIDArgument(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
        if (uuid != null) {
            statement.setBytes(position, UuidAdaptor.getBytesFromUUID(uuid));
        }
        else {
            statement.setNull(position, Types.BINARY);
        }
    }
}