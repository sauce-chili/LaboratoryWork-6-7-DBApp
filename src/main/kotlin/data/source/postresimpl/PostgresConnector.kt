package data.source.postresimpl

import java.sql.Connection
import java.sql.DriverManager

class PostgresConnector(private val connectionParam: ConnectionParam) : Connector {

    init {
        Class.forName("org.postgresql.Driver")
    }

    override fun getConnection(): Connection? = DriverManager.getConnection(
        connectionParam.url,
        connectionParam.user,
        connectionParam.password
    )
}