package be.kicksync_backend.common.security;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.common.util.JwtUtil;
import be.kicksync_backend.feature.product.service.DropEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final DropEventService dropEventService;
    private static final Pattern DROP_TOPIC_PATTERN = Pattern.compile("/topic/drop/(\\d+)");


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        switch (accessor.getCommand()) {
            case CONNECT:
                authenticate(accessor);
                break;
            case SUBSCRIBE:
                authorizeSubscription(accessor);
                break;
            case null:
                break;
            default:
                break;
        }
        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String token = findToken(accessor);
        if (token == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    accessor.setUser(authentication);
                } else {
                    throw new CustomException(ErrorCode.INVALID_TOKEN);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication", e);
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        Authentication user = (Authentication) accessor.getUser();
        String destination = accessor.getDestination();

        if (user == null || destination == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_SUBSCRIBE);
        }

        Matcher matcher = DROP_TOPIC_PATTERN.matcher(destination);
        if (matcher.matches()) {
            Long eventId = Long.parseLong(matcher.group(1));
            dropEventService.findById(eventId);
            log.info("User '{}' subscribed to drop event topic '{}'", user.getName(), eventId);
        }
    }

    private String findToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return accessor.getFirstNativeHeader("access-token");
    }
}
