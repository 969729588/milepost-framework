package com.milepost.api.vo.response;

public class ResponseHelper {
	public static <T> Response<T> createSuccessResponse() {
		return createResponse(ReturnCode.SUCCESS, (String) null);
	}
	
	public static <T> Response<T> createFailResponse() {
		return createResponse(ReturnCode.FAIL, (String) null);
	}

	public static <T> Response<T> createSuccessResponse(T payload) {
		Response<T> response = createResponse(ReturnCode.SUCCESS, (String) null);
		response.setPayload(payload);
		return response;
	}

	public static <T> Response<T> createResponse() {
		return createResponse(ReturnCode.NEVER_USED_CODE, (String) null);
	}

	public static <T> Response<T> createResponse(int code, String msg) {
		Response<T> response = new Response<T>();
		if (code != ReturnCode.NEVER_USED_CODE) {
			response.setCode(code);
		}

		if (msg != null) {
			response.setMsg(msg);
		}

		return response;
	}

	public static <T> Response<T> createBusinessErrorResponse(String description) {
		return createResponse(ReturnCode.BUSINESS_ERROR, description);
	}

	public static <T> Response<T> createExceptionResponse(Exception e) {
		String msg = e.getMessage();
		if(msg==null || msg.equals("")){
			msg = e.toString();
		}
		return createResponse(ReturnCode.EXCEPTION, msg);
	}

	public static <T> Response<T> createBindErrorResponse(String description) {
		return createResponse(ReturnCode.BIND_ERROR, description);
	}

	public static <T> Response<T> createNotFoundResponse() {
		return createResponse(ReturnCode.NOT_FOUND, "Data not found!");
	}

	public static <T> Response<T> createParameterErrorResponse() {
		return createResponse(ReturnCode.PARAM_ERROR, "Error parameter!");
	}

	public static <T> Response<T> createRemoteCallErrorResponse() {
		return createResponse(ReturnCode.REMOTE_CALL_ERROR, "Remote call error!");
	}
	
	public static <T> Response<T> createSessionKickedResponse() {
		return createResponse(ReturnCode.SESSION_KICKED, "Session kicked!");
	}
	
	public static <T> Response<T> createSessionExpiredResponse() {
		return createResponse(ReturnCode.SESSION_EXPIRED, "Session expired!");
	}
}