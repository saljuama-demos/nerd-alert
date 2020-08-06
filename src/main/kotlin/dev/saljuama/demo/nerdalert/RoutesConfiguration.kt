package dev.saljuama.demo.nerdalert

import dev.saljuama.demo.nerdalert.accounts.AccountsRequestHandler
import dev.saljuama.demo.nerdalert.accounts.registration.AccountRegistrationRequestHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.servlet.function.*

@Configuration
class RoutesConfiguration {

  @Bean
  fun accountsRoutes(
    accountRegistrationRequestHandler: AccountRegistrationRequestHandler,
    accountsRequestHandler: AccountsRequestHandler
  ): RouterFunction<ServerResponse> =
    router {
      accept(APPLICATION_JSON).nest {
        GET("/api/accounts")(accountsRequestHandler::listAccounts)
        POST("/api/accounts")(accountRegistrationRequestHandler::registerNewAccount)
        GET("/api/accounts/{username}")(accountsRequestHandler::viewAccountDetails)
        GET("/api/accounts/{username}/verify/{token}")(accountRegistrationRequestHandler::verifyStarterAccount)
        PUT("/api/accounts/{username}/profile")(accountsRequestHandler::updateProfile)
      }
    }

}