package com.atamanahmet.vinylexchange.session;

import com.atamanahmet.vinylexchange.domain.entity.User;
import com.atamanahmet.vinylexchange.domain.enums.RoleName;
import com.atamanahmet.vinylexchange.exception.UnauthorizedActionException;
import com.atamanahmet.vinylexchange.exception.UserNotFoundException;
import com.atamanahmet.vinylexchange.security.principal.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;
import java.util.UUID;

public class UserUtil {

    public static boolean isAuthenticated(){

        Authentication authentication = getAuthentication();

        if(authentication!=null && authentication.getPrincipal()!=null && authentication.getPrincipal() instanceof UserDetailsImpl){
            return authentication.isAuthenticated();
        }

        throw new UnauthorizedActionException("Authentication needed for this action");
    }

    public static Authentication getAuthentication(){

        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static UUID getCurrentUserId(){

        if (isAuthenticated()){

            User user = getCurrentUser();

            return user.getId();
        }

        throw new UserNotFoundException();

    }

    public static String getCurrentUserUsername(){

        if (isAuthenticated()){

            User user = getCurrentUser();

            return user.getUsername();
        }

        throw new UserNotFoundException();

    }

    public static Set<RoleName> getCurrentUserRoles(){

        if (isAuthenticated()){

            UserDetailsImpl currentUserDetails = getCurrentUserDetails();

            return currentUserDetails.getRoles();
        }

        throw new UserNotFoundException();

    }

    public static User getCurrentUser(){

        if (isAuthenticated()){

            Authentication authentication = getAuthentication();

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getUser();
        }

        throw new UserNotFoundException();
    }

    public static UserDetailsImpl getCurrentUserDetails(){

        if (isAuthenticated()){

            Authentication authentication = getAuthentication();

            return (UserDetailsImpl) authentication.getPrincipal();

        }

        throw new UserNotFoundException();
    }
}
