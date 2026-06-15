package com.atamanahmet.vinylexchange.exception;

import java.util.HashMap;
import java.util.Map;

import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * DTO field validation errors, returns map of field -> message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Registration business rule violations (duplicate email, username etc)
     */
    @ExceptionHandler(RegistrationValidationException.class)
    public ResponseEntity<?> handleRegistrationValidationException(RegistrationValidationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

    @ExceptionHandler({ MaxUploadSizeExceededException.class, FileSizeLimitExceededException.class })
    public ResponseEntity<?> handleMaxUploadSizeExceededException(
            Exception exception,
            HttpServletRequest request,
            HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("Max file size exceeded");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleWrongCredentialsException(BadCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
    }

    @ExceptionHandler(PageNotFoundException.class)
    public ResponseEntity<?> handlePageNotFoundException(PageNotFoundException exception) {
        logger.warn("Page not found: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    /**
     * Generic resource not found, logs warn only, no body needed
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException exception) {
        logger.warn("Resource not found: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(TokenExpireException.class)
    public ResponseEntity<?> handleTokenExpiredException(TokenExpireException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired, please log in again");
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<?> handleUnauthorizedActionException(UnauthorizedActionException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
    }

    /**
     * No authenticated user found in security context
     */
    @ExceptionHandler(NoCurrentUserException.class)
    public ResponseEntity<?> handleNoCurrentUserException(NoCurrentUserException exception) {
        logger.warn("Unauthenticated access attempt");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
    }

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<?> handleImageUploadException(ImageUploadException exception) {
        logger.error("Image upload failed: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Image upload failed");
    }

    @ExceptionHandler(ListingCreationException.class)
    public ResponseEntity<?> handleListingCreationException(ListingCreationException exception) {
        logger.error("Listing creation failed: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Listing creation failed");
    }

    @ExceptionHandler(ListingUpdateException.class)
    public ResponseEntity<?> handleListingUpdateException(ListingUpdateException exception) {
        logger.error("Listing update failed: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Listing update failed");
    }

    @ExceptionHandler(ConversationNotFoundException.class)
    public ResponseEntity<?> handleConversationNotFoundException(ConversationNotFoundException exception) {
        logger.warn("Conversation not found: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException exception) {
        logger.warn("User not found: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(InvalidOrderOperationException.class)
    public ResponseEntity<?> handleInvalidOrderOperationException(InvalidOrderOperationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

    @ExceptionHandler(ListingNotFoundException.class)
    public ResponseEntity<?> handleListingNotFoundException(ListingNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<?> handleCartItemNotFoundException(CartItemNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<?> handleInsufficientStockException(InsufficientStockException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

    @ExceptionHandler(GenreNotFoundException.class)
    public ResponseEntity<?> handleGenreNotFoundException(GenreNotFoundException exception) {
        logger.warn("Genre not found: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<?> handleRoleNotFoundException(RoleNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    /**
     * Invalid state machine transition, e.g. DELIVERED -> PENDING
     */
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<?> handleInvalidStatusTransitionException(InvalidStatusTransitionException exception) {
        logger.warn("Invalid status transition: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<?> handleOrderNotFoundException(OrderNotFoundException exception) {
        logger.warn("Order not found: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<?> handleEmptyCartException(EmptyCartException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(CheckOutValidationException.class)
    public ResponseEntity<?> handleCheckOutValidationException(CheckOutValidationException exception) {
        logger.warn("Checkout validation failed: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(CheckOutProcessingException.class)
    public ResponseEntity<?> handleCheckOutProcessingException(CheckOutProcessingException exception) {
        logger.error("Checkout processing failed: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Checkout processing failed");
    }
}