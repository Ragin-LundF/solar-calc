package io.github.raginlundf.solarcalc.domain.models

import liquibase.integration.spring.SpringLiquibase
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import jakarta.persistence.EntityManagerFactory
import javax.sql.DataSource
import java.util.Properties

@Configuration
@EnableJpaRepositories(basePackages = ["io.github.raginlundf.solarcalc.domain.models"])
@EnableTransactionManagement
class RepositoryTestConfig {

    @Bean
    fun dataSource(
        @Value("\${spring.datasource.url}") url: String,
        @Value("\${spring.datasource.username}") username: String,
        @Value("\${spring.datasource.password}") password: String,
    ): DataSource = DriverManagerDataSource(url, username, password).apply {
        setDriverClassName("org.mariadb.jdbc.Driver")
    }

    @Bean
    @DependsOn("liquibase")
    fun entityManagerFactory(dataSource: DataSource): LocalContainerEntityManagerFactoryBean =
        LocalContainerEntityManagerFactoryBean().apply {
            this.dataSource = dataSource
            setPackagesToScan("io.github.raginlundf.solarcalc.domain.models")
            jpaVendorAdapter = HibernateJpaVendorAdapter()
            setJpaProperties(Properties().apply {
                setProperty("hibernate.hbm2ddl.auto", "none")
            })
        }

    @Bean
    fun transactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager =
        JpaTransactionManager(entityManagerFactory)

    @Bean
    fun liquibase(dataSource: DataSource): SpringLiquibase = SpringLiquibase().apply {
        this.dataSource = dataSource
        changeLog = "classpath:db/db.changelog-master.xml"
    }
}
