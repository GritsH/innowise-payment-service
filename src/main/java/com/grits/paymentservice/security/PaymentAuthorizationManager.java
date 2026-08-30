package com.grits.paymentservice.security;

import com.grits.paymentservice.client.ApiClient;
import com.grits.paymentservice.dao.PaymentDao;
import com.grits.paymentservice.model.response.UserResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final PaymentDao paymentDao;
    private final ApiClient apiClient;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
        Authentication authentication = authenticationSupplier.get();
        if (SecurityHelper.isNotAuthenticated(authentication)) {
            return new AuthorizationDecision(false);
        }
        if (SecurityHelper.isAdmin(authentication)) {
            return new AuthorizationDecision(true);
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            return new AuthorizationDecision(false);
        }

        try {
            UserResponse user = apiClient.getUserByEmail(email);
            String userId = context.getVariables().get("userId");
            if (userId != null) {
                return new AuthorizationDecision(user.getId().equals(UUID.fromString(userId)));
            }
            String orderId = context.getVariables().get("orderId");
            if (orderId != null) {
                UUID paymentUserId = paymentDao.getUserIdByOrderId(UUID.fromString(orderId));
                return new AuthorizationDecision(user.getId().equals(paymentUserId));
            }
            return new AuthorizationDecision(false);
        } catch (IllegalArgumentException | FeignException.NotFound e) {
            return new AuthorizationDecision(false);
        } catch (FeignException e) {
            log.error("Request failed during authorization", e);
            throw e;
        }
    }
}
