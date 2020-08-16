package dev.saljuama.demo.nerdalert

import dev.saljuama.demo.nerdalert.accounts.AccountRequestHandler
import dev.saljuama.demo.nerdalert.friendship.FriendshipRequestHandler
import dev.saljuama.demo.nerdalert.profiles.ProfilesRequestHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.servlet.function.*

@Configuration
class Routing {

  @Bean
  fun applicationRouter(
    accountRequestHandler: AccountRequestHandler,
    profilesRequestHandler: ProfilesRequestHandler,
    friendshipRequestHandler: FriendshipRequestHandler
  ): RouterFunction<ServerResponse> = router {
    path("/api").nest {
      path("/accounts").nest {
        GET("/{username}/verify/{token}")(accountRequestHandler::verifyStarterAccount)
        accept(APPLICATION_JSON).nest {
          POST("/")(accountRequestHandler::registerNewAccount)
          DELETE("/{username}")(accountRequestHandler::deleteAccount)
        }
      }
      path("/profiles").nest {
        GET("/search")(profilesRequestHandler::searchUserProfiles)
        GET("/{username}")(profilesRequestHandler::viewUserProfile)
        accept(APPLICATION_JSON).nest {
          PUT("/{username}")(profilesRequestHandler::updateUserProfile)
        }
      }
      path("/friendship").nest {
        accept(APPLICATION_JSON).nest {
          POST("/{username}")(friendshipRequestHandler::createNewFriendshipRequest)
        }
      }
    }
  }

}