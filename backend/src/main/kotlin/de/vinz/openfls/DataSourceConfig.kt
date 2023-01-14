package de.vinz.openfls

import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File
import javax.sql.DataSource

@Configuration
class DataSourceConfig {
    @Bean
    fun getDataSource(): DataSource {
        return DataSourceBuilder
            .create()
            .username(File(System.getenv("MYSQL_USER_FILE")).readText())
            .password(File(System.getenv("MYSQL_PASSWORD_FILE")).readText())
            .url("jdbc:mysql://open_fls_db:3306/openfls")
            .driverClassName("com.mysql.cj.jdbc.Driver")
            .build()
    }
}