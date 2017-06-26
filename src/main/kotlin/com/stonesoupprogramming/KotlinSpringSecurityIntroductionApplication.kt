package com.stonesoupprogramming

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcOperations
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import javax.sql.DataSource

@SpringBootApplication
class KotlinSpringSecurityIntroductionApplication

fun main(args: Array<String>) {
    SpringApplication.run(KotlinSpringSecurityIntroductionApplication::class.java, *args)
}

@Configuration
class DataConfig {

    @Bean(name = arrayOf("dataSource"))
    fun dataSource() : DataSource {
        //This will create a new embedded database and run the schema.sql script
        return EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("schema.sql")
                .build()
    }

    @Bean
    fun jdbcTemplate(@Qualifier("dataSource") dataSource: DataSource) : JdbcOperations {
        return JdbcTemplate(dataSource)
    }
}

@Configuration //Make this as a configuration class
@EnableWebSecurity //Turn on Web Security
class SecurityWebInitializer(
        //Inject our datasource into this class for the AuthenticationManagerBuilder
        @Autowired @Qualifier("dataSource") val dataSource: DataSource)
    : WebSecurityConfigurerAdapter(){

    override fun configure(http: HttpSecurity) {
        http
                    .formLogin()
                .and()
                    .logout()
                        .logoutSuccessUrl("/")
                .and()
                    .rememberMe()
                        .tokenRepository(JdbcTokenRepositoryImpl())
                            .tokenValiditySeconds(2419200)
                                .key("BurgerBob")
                .and()
                    .httpBasic()
                .and()
                    .authorizeRequests()
                        .antMatchers("/").authenticated()
                        .anyRequest().permitAll()
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        //As long as our database schema conforms to the default queries
        //we can use jdbcAuthentication and pass in our data source
        //Spring will do the rest of the work for us
        auth.jdbcAuthentication().dataSource(dataSource)
    }
}

@Controller
@RequestMapping("/")
class IndexController {

    @RequestMapping(method = arrayOf(RequestMethod.GET))
    fun doGet(model : Model) : String {
        //We can access the current user like this
        val authorization = SecurityContextHolder.getContext().authentication

        //Send the user name back to the view
        model.addAttribute("username", authorization.name)
        return "index"
    }
}