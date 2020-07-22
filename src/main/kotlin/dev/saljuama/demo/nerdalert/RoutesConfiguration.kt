package dev.saljuama.demo.nerdalert

import dev.saljuama.demo.nerdalert.accounts.AccountsRepository
import dev.saljuama.demo.nerdalert.accounts.AccountsRequestHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.servlet.function.*

@Configuration
class RoutesConfiguration {

  @Bean
  fun accountsRequestHandler(accountsRepository: AccountsRepository) = AccountsRequestHandler(accountsRepository)

  @Bean
  fun accountsRoutes(accountsRequestHandler: AccountsRequestHandler): RouterFunction<ServerResponse> = router {
    accept(APPLICATION_JSON).nest {
      POST("/api/accounts")(accountsRequestHandler::registerNewAccount)
      GET("/api/accounts/{username}/verify/{token}")(accountsRequestHandler::verifyNewAccount)
      POST("/api/accounts/{username}/profile")(accountsRequestHandler::createProfile)
    }
  }
}