package com.kosmo.zipkok.exception;

import com.kosmo.zipkok.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

/*
    컨트롤러마다 try - catch 부문을 작성하지 않고
    리턴값을 ResponseEntity로 잡고 statusCode마다 전역예외처리를 진행한다.
*/
/*@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse> handleMaxUpload(MaxUploadSizeExceededException ex) {
        log.warn("파일 업로드 실패 - 용량 초과", ex);
        ApiResponse body = new ApiResponse(false, "파일 크기는 10MB를 초과할 수 없습니다.", null);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(body); // 413
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse> handleIOException(IOException ex) {
        log.error("파일 처리 중 IO 오류", ex);
        ApiResponse body = new ApiResponse(false, "파일 처리 중 오류가 발생했습니다. 관리자에게 문의하세요.", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception ex) {
        log.error("알수 없는 exception", ex);
        ApiResponse body = new ApiResponse(false, "서버 오류가 발생했습니다. 관리자에게 문의하세요.", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}*/

// TODO 일단 커스텀에러와 동일하게 에러 페이지 이동하는 걸로 바꿈 - 추후 try-catch를 없앨지 고민좀 해봐야할듯
@ControllerAdvice
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * API 요청인지 판별 (Accept 헤더 또는 URL 패턴으로)
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String uri = request.getRequestURI();

        // /api/** 경로이거나 Accept 헤더에 application/json이 있으면 API 요청
        return uri.startsWith("/api") ||
                (accept != null && accept.contains("application/json"));
    }

    // ========== 파일 업로드 용량 초과 ==========
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleMaxUpload(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("파일 업로드 실패 - 용량 초과", ex);

        if (isApiRequest(request)) {
            // API 요청: JSON 반환
            ApiResponse body = new ApiResponse(false, "파일 크기는 10MB를 초과할 수 없습니다.", null);
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(body);
        } else {
            // 일반 웹 요청: 에러 페이지로 이동
            ModelAndView mav = new ModelAndView("error/413");
            mav.addObject("message", "파일 크기는 10MB를 초과할 수 없습니다.");
            return mav;
        }
    }

    // ========== IO 예외 ==========
    @ExceptionHandler(IOException.class)
    public Object handleIOException(IOException ex, HttpServletRequest request) {
        log.error("파일 처리 중 IO 오류", ex);

        if (isApiRequest(request)) {
            ApiResponse body = new ApiResponse(false, "파일 처리 중 오류가 발생했습니다.", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        } else {
            ModelAndView mav = new ModelAndView("error/500");
            mav.addObject("message", "파일 처리 중 오류가 발생했습니다.");
            return mav;
        }
    }

    // ========== 일반 예외 (500) ==========
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex, HttpServletRequest request) {
        log.error("알 수 없는 예외 발생", ex);

        if (isApiRequest(request)) {
            ApiResponse body = new ApiResponse(false, "서버 오류가 발생했습니다.", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        } else {
            ModelAndView mav = new ModelAndView("error/500");
            mav.addObject("message", "서버 오류가 발생했습니다.");
            mav.addObject("error", ex.getMessage());
            return mav;
        }
    }

    // ========== 404 예외 (선택적) ==========
    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    public Object handleNotFound(Exception ex, HttpServletRequest request) {
        log.warn("404 에러: {}", request.getRequestURI());

        if (isApiRequest(request)) {
            ApiResponse body = new ApiResponse(false, "요청한 리소스를 찾을 수 없습니다.", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        } else {
            return new ModelAndView("error/404");
        }
    }

    // ========== 403 예외 (권한 없음) ==========
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public Object handleAccessDenied(Exception ex, HttpServletRequest request) {
        log.warn("403 에러: 접근 권한 없음");

        if (isApiRequest(request)) {
            ApiResponse body = new ApiResponse(false, "접근 권한이 없습니다.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        } else {
            return "redirect:/";  // 메인 페이지로 리다이렉트
        }
    }
}
