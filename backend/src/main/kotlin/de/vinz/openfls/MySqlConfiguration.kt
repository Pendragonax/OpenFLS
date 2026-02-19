package de.vinz.openfls

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File
import javax.sql.DataSource

@Configuration
class MySqlConfiguration(
    @param:Value($$"${openfls.mysql.host}") private val dbHost: String,
    @param:Value($$"${openfls.mysql.database}") private val dbName: String,
    @param:Value($$"${openfls.mysql.user-file}") private val userFile: String,
    @param:Value($$"${openfls.mysql.password-file}") private val passwordFile: String
) {
    @Bean
    fun getDataSource(): DataSource {
        return DataSourceBuilder
            .create()
            .username(File(userFile).readText())
            .password(File(passwordFile).readText())
            .url("jdbc:mysql://$dbHost:3306/$dbName")
            .driverClassName("com.mysql.cj.jdbc.Driver")
            .build()
    }
}
