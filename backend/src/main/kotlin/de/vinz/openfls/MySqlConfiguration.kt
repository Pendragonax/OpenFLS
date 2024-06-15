package de.vinz.openfls

import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.orm.jpa.JpaVendorAdapter
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import java.io.File
import javax.sql.DataSource

@Configuration
class MySqlConfiguration {
    @Bean
    fun getDataSource(): DataSource {
        val dbHost = System.getenv("MYSQL_HOST")

        return DataSourceBuilder
            .create()
            .username(File(System.getenv("MYSQL_USER_FILE")).readText())
            .password(File(System.getenv("MYSQL_PASSWORD_FILE")).readText())
            .url("jdbc:mysql://$dbHost:3306/openfls")
            .driverClassName("com.mysql.cj.jdbc.Driver")
            .build()
    }
}