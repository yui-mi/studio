package com.example.yui.form;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.yui.validation.constraints.ImageByte;
import com.example.yui.validation.constraints.ImageNotEmpty;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TopicForm {

	private Long id;

	private Long userId;

	@ImageNotEmpty
	@ImageByte(max = 2000000)
	private MultipartFile image;

	private String imageData;

	@NotEmpty
	@Size(max = 1000) //直す
	private String reservationDate;
	@NotEmpty
	@Size(max = 1000) //直す
	private String reservationTime;

	private String path;

	@NotEmpty
	@Size(max = 1000)
	private String description;

	private UserForm user;

	private List<FavoriteForm> favorites;

	private FavoriteForm favorite;

	private List<CommentForm> comments;

}