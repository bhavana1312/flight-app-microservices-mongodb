package com.flightapp.authservice.payload.request;

import lombok.Data;

@Data
public class ChangePasswordRequest {
	private String oldPassword;
	private String newPassword;
}
