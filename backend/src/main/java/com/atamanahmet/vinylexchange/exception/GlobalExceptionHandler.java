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

    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // dto validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {

        Map<String, String> errors = new HashMap<>();

        exception.getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);

    }

    // service level validation
    @ExceptionHandler(RegistrationValidationException.class)
    public ResponseEntity<?> handleRegistrationValidationException(RegistrationValidationException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }

    @ExceptionHandler({ MaxUploadSizeExceededException.class, FileSizeLimitExceededException.class })
    public ResponseEntity<?> handleMaxUploadSizeExceededException(Exception exception, HttpServletRequest request,
            HttpServletResponse response) {

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body("Max file size exceeded");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleWrongCredentialsException(BadCredentialsException exception) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(exception.getMessage());
    }

    @ExceptionHandler(PageNotFoundException.class)
    public ResponseEntity<?> handlePageNotFoundException(PageNotFoundException exception) {
        logger.warn(exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleResourceNotFoundException(ResourceNotFoundException exception) {
        logger.warn(exception.getMessage());
    }

    @ExceptionHandler(TokenExpireException.class)
    public void handleTokenExpiredException(TokenExpireException exception) {

    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<?> handleUnauthorizedActionException(UnauthorizedActionException exception) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(exception.getMessage());
    }

    @ExceptionHandler(NoCurrentUserException.class)
    public void handleNoCurrentUserException(NoCurrentUserException exception) {
        System.out.println("No user");
    }


    @ExceptionHandler(ImageUploadException.class)
    public void handleImageUploadException(ImageUploadException exception) {
        logger.error(exception.getMessage());
    }

    @ExceptionHandler(ListingCreationException.class)
    public void handleListingCreationException(ListingCreationException exception, String username) {
        logger.error(exception.getMessage(), username);
    }

    @ExceptionHandler(ConversationNotFoundException.class)
    public void handleConversationNotFoundException(ConversationNotFoundException exception) {
        System.out.println(exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<HttpStatus> handleUserNotFoundException(UserNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(InvalidOrderOperationException.class)
    public ResponseEntity<?> handleInvalidOrderOperationException(InvalidOrderOperationException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }

    @ExceptionHandler(ListingNotFoundException.class)
    public ResponseEntity<?> handleListingNotFoundException(ListingNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<?> handleCartItemNotFoundException(CartItemNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<?> handleInsufficientStockException(InsufficientStockException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }

    @ExceptionHandler(GenreNotFoundException.class)
    public void handleGenreNotFoundException(GenreNotFoundException exception) {

        logger.warn(exception.getMessage());
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<?> handleRoleNotFoundException(RoleNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public void handleInvalidStatusTransitionException(InvalidStatusTransitionException exception) {
        System.out.println(exception.getMessage());
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public void handleOrderNotFoundException(OrderNotFoundException exception) {
        System.out.println(exception.getMessage());
    }

    @ExceptionHandler(EmptyCartException.class)
    public void handleEmptyCartException(EmptyCartException exception) {
        System.out.println(exception.getMessage());
    }

    @ExceptionHandler(CheckOutValidationException.class)
    public ResponseEntity<?> handleCheckOutValidationException(CheckOutValidationException exception) {
        System.out.println(exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler(CheckOutProcessingException.class)
    public void handleCheckOutProcessingException(CheckOutProcessingException exception) {

        System.out.println(exception.getMessage());

    }

}
