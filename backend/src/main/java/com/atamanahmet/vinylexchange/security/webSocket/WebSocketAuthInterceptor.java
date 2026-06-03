// package com.vinyl.VinylExchange.security.webSocket;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.messaging.Message;
// import org.springframework.messaging.MessageChannel;
// import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
// import org.springframework.messaging.support.ChannelInterceptor;
// import org.springframework.messaging.support.MessageHeaderAccessor;
// import org.springframework.stereotype.Component;

// import com.vinyl.VinylExchange.security.service.UserDetailsServiceImpl;
// import com.vinyl.VinylExchange.security.util.JwtCookieUtil;

// @Component
// public class WebSocketAuthInterceptor implements ChannelInterceptor {

// private final Logger logger =
// LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

// private final JwtCookieUtil jwtCookieUtil;
// private final UserDetailsServiceImpl userDetailsServiceImpl;

// public WebSocketAuthInterceptor(
// JwtCookieUtil jwtCookieUtil,
// UserDetailsServiceImpl userDetailsServiceImpl) {

// this.jwtCookieUtil = jwtCookieUtil;
// this.userDetailsServiceImpl = userDetailsServiceImpl;
// }

// @Override
// public Message<?> preSend(Message<?> message, MessageChannel channel) {

// StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
// StompHeaderAccessor.class);

// return ChannelInterceptor.super.preSend(message, channel);
// }

// @Override
// public boolean preReceive(MessageChannel channel) {
// // TODO Auto-generated method stub
// return ChannelInterceptor.super.preReceive(channel);
// }

// }