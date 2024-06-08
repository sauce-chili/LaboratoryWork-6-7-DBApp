package data.source.postresimpl

import java.sql.Connection

interface Connector {
    fun getConnection(): Connection?
}