package dev.saljuama.demo.nerdalert

import dev.saljuama.demo.nerdalert.accounts.AccountsRequestHandler
import dev.saljuama.demo.nerdalert.accounts.AccountsService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.servlet.function.*

@Configuration
class RoutesConfiguration {

  @Bean
  fun accountsRequestHandler(accountsService: AccountsService): AccountsRequestHandler =
    AccountsRequestHandler(accountsService)

  @Bean
  fun accountsRoutes(accountsRequestHandler: AccountsRequestHandler): RouterFunction<ServerResponse> =
    router {
      accept(APPLICATION_JSON).nest {
        GET("/api/accounts")(accountsRequestHandler::listAccounts)
        POST("/api/accounts")(accountsRequestHandler::registerNewAccount)
        GET("/api/accounts/{username}")(accountsRequestHandler::viewAccountDetails)
        GET("/api/accounts/{username}/verify/{token}")(accountsRequestHandler::verifyStarterAccount)
        POST("/api/accounts/{username}/profile")(accountsRequestHandler::createProfile)
        //PUT("/api/accounts/{username}/profile")(accountsRequestHandler::updateProfile)
      }
    }

}