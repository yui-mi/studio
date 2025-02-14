package com.example.yui.validation.constraints;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ImageByteValidator implements ConstraintValidator<ImageByte, MultipartFile> {

	int max;

	@Override
	public void initialize(ImageByte annotation) {
		this.max = annotation.max();
	}

	@Override
	public boolean isValid(MultipartFile image, ConstraintValidatorContext context) {
		if (image.getSize() > this.max) {
			return false;
		}
		return true;
	}
}