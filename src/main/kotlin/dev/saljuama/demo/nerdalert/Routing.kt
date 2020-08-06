package dev.saljuama.demo.nerdalert

import dev.saljuama.demo.nerdalert.accounts.AccountsRequestHandler
import dev.saljuama.demo.nerdalert.accounts.registration.AccountRegistrationRequestHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.servlet.function.*

@Configuration
class Routing {

  @Bean
  fun accountsRouter(
    accountRegistrationRequestHandler: AccountRegistrationRequestHandler,
    accountsRequestHandler: AccountsRequestHandler
  ): RouterFunction<ServerResponse> = router {
    path("/api/accounts").nest {
      GET("/")(accountsRequestHandler::listAccounts)
      GET("/{username}")(accountsRequestHandler::viewAccountDetails)
      GET("/{username}/verify/{token}")(accountRegistrationRequestHandler::verifyStarterAccount)
      accept(APPLICATION_JSON).nest {
        POST("/")(accountRegistrationRequestHandler::registerNewAccount)
        PUT("/{username}/profile")(accountsRequestHandler::updateProfile)
      }
    }
  }

}