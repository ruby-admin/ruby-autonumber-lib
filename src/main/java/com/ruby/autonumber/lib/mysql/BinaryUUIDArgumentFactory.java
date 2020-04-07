package com.ruby.autonumber.lib.mysql;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;
import java.util.UUID;

public class BinaryUUIDArgumentFactory extends AbstractArgumentFactory<UUID> {

    public BinaryUUIDArgumentFactory() {
        super(Types.BINARY);
    }

    @Override
    protected Argument build(UUID value, ConfigRegistry config) {
        return new BinaryUUIDArgument(value);
    }
}
