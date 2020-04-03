package zhh_fu.miaosha.exception;

import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import zhh_fu.miaosha.result.CodeMsg;
import zhh_fu.miaosha.result.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
    //任何异常都需要拦截
    @ExceptionHandler(value = Exception.class)
    public Result<String> exceptionHandler(HttpServletRequest request, Exception ex){
        ex.printStackTrace();
        if (ex instanceof GlobalException){
            //返回全局异常
            GlobalException globalEx = (GlobalException) ex;
            return Result.error(globalEx.getCm());
        } else if (ex instanceof BindException){
            BindException bindEx = (BindException) ex;
            List<ObjectError> errors = bindEx.getAllErrors();
            ObjectError error = errors.get(0);
            String msg = error.getDefaultMessage();
            //返回绑定异常
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(msg));
        } else{
            //返回通用的服务异常
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
