package dev.saljuama.demo.nerdalert

import dev.saljuama.demo.nerdalert.accounts.AccountRequestHandler
import dev.saljuama.demo.nerdalert.profiles.ProfilesRequestHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.servlet.function.*

@Configuration
class Routing {

  @Bean
  fun accountsRouter(accountRequestHandler: AccountRequestHandler): RouterFunction<ServerResponse> = router {
    path("/api/accounts").nest {
      GET("/{username}/verify/{token}")(accountRequestHandler::verifyStarterAccount)
      accept(APPLICATION_JSON).nest {
        POST("/")(accountRequestHandler::registerNewAccount)
        DELETE("/{username}")(accountRequestHandler::deleteAccount)
      }
    }
  }

  @Bean
  fun profilesRouter(profilesRequestHandler: ProfilesRequestHandler): RouterFunction<ServerResponse> = router {
    path("/api/profiles").nest {
      GET("/{username}")(profilesRequestHandler::viewUserProfile)
      accept(APPLICATION_JSON).nest {
        PUT("/{username}")(profilesRequestHandler::updateUserProfile)
      }
    }
  }

}