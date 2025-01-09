package com.queryapplication.service;

import com.queryapplication.dto.PasswordResetDTO;

public interface PasswordService {

    boolean initiatePasswordReset(String email);

    boolean validatePasswordReset(String hashId);

    boolean resetPassword(String hashId, PasswordResetDTO passwordResetDTO);
}
