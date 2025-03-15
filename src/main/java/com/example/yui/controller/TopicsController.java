package com.example.yui.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;
import org.springframework.context.MessageSource;

import com.example.yui.entity.Topic;
import com.example.yui.entity.UserInf;
import com.example.yui.form.CommentForm;
import com.example.yui.form.FavoriteForm;
import com.example.yui.form.TopicForm;
import com.example.yui.form.UserForm;
import com.example.yui.repository.TopicRepository;
import com.example.yui.service.SendMailService;
import com.example.yui.entity.Comment;
import com.example.yui.entity.Favorite;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class TopicsController {

	@Autowired
	private MessageSource messageSource;
	@Autowired
	private ModelMapper modelMapper;
	@Autowired
	private TopicRepository repository;
	@Autowired
	private HttpServletRequest request;

	@Value("${image.local:false}")
	private String imageLocal;

	@Autowired
	private SendMailService sendMailService;

	@GetMapping("/topics")
	public String index(Principal principal, Model model) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();

		//List<Topic> topics = repository.findAllByOrderByUpdatedAtDesc();
		List<Topic> topics = repository.findAll();
		List<TopicForm> list = new ArrayList<>();
		for (Topic entity : topics) {
			TopicForm form = getTopic(user, entity);
			list.add(form);
		}
		model.addAttribute("list", list);

		return "calendars/index";
	}

	public TopicForm getTopic(UserInf user, Topic entity) throws FileNotFoundException, IOException {
		modelMapper.getConfiguration().setAmbiguityIgnored(true);
		modelMapper.typeMap(Topic.class, TopicForm.class).addMappings(mapper -> mapper.skip(TopicForm::setUser));
		modelMapper.typeMap(Favorite.class, FavoriteForm.class)
				.addMappings(mapper -> mapper.skip(FavoriteForm::setTopic));

		boolean isImageLocal = false;
		if (imageLocal != null) {
			isImageLocal = new Boolean(imageLocal);
		}
		TopicForm form = modelMapper.map(entity, TopicForm.class);

		if (isImageLocal) {
			try (InputStream is = new FileInputStream(new File(entity.getPath()));
					ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				byte[] indata = new byte[10240 * 16];
				int size;
				while ((size = is.read(indata, 0, indata.length)) > 0) {
					os.write(indata, 0, size);
				}
				StringBuilder data = new StringBuilder();
				data.append("data:");
				data.append(getMimeType(entity.getPath()));
				data.append(";base64,");

				data.append(new String(Base64.getEncoder().encode(os.toByteArray()), "ASCII"));
				form.setImageData(data.toString());
			}
		}

		UserForm userForm = modelMapper.map(entity.getUser(), UserForm.class);
		form.setUser(userForm);

		List<FavoriteForm> favorites = new ArrayList<FavoriteForm>();
		for (Favorite favoriteEntity : entity.getFavorites()) {
			FavoriteForm favorite = modelMapper.map(favoriteEntity, FavoriteForm.class);
			favorites.add(favorite);
			if (user.getUserId().equals(favoriteEntity.getUserId())) {
				form.setFavorite(favorite);
			}
		}
		form.setFavorites(favorites);

		List<CommentForm> comments = new ArrayList<CommentForm>();

		for (Comment commentEntity : entity.getComments()) {
			CommentForm comment = modelMapper.map(commentEntity, CommentForm.class);
			comments.add(comment);
		}
		form.setComments(comments);
		return form;
	}

	private String getMimeType(String path) {
		String extension = FilenameUtils.getExtension(path);
		String mimeType = "image/";
		switch (extension) {
		case "jpg":
		case "jpeg":
			mimeType += "jpeg";
			break;
		case "png":
			mimeType += "png";
			break;
		case "gif":
			mimeType += "gif";
			break;
		}
		return mimeType;
	}

	@GetMapping("/topic/show")
	public String show(@RequestParam int id, Model model) {
		System.out.println("⭐︎︎︎⭐︎︎︎︎⭐︎︎︎︎⭐︎︎︎︎⭐︎︎︎︎⭐︎︎︎︎⭐︎︎︎︎⭐︎︎︎︎⭐︎︎︎︎︎" + id);
		Topic topic = repository.getReferenceById((long) id);
		model.addAttribute("topic", topic);
		//IDと一致するトピックのデータを取得
		//取得したデータをmodel.addAttributeで設定する
		return "topics/show";
	}

	@GetMapping("/topics/new")
	public String newTopic(Model model) {
		model.addAttribute("form", new TopicForm());
		return "topics/new";
	}

	@PostMapping("/topic")
	public String create(Principal principal, @Validated @ModelAttribute("form") TopicForm form, BindingResult result,
			Model model, @RequestParam MultipartFile image, RedirectAttributes redirAttrs, Locale locale)
			throws IOException {
		if (result.hasErrors()) {
			model.addAttribute("hasMessage", true);
			model.addAttribute("class", "alert-danger");
			model.addAttribute("message", messageSource.getMessage("topics.create.flash.1", new String[] {}, locale));
			return "topics/new";
		}
		boolean isImageLocal = false;
		if (imageLocal != null) {
			isImageLocal = new Boolean(imageLocal);
		}

		Topic entity = new Topic();
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		System.out.println("⭐︎︎︎⭐︎︎︎︎⭐︎︎︎︎⭐︎︎︎︎⭐︎︎︎︎⭐︎︎︎︎⭐︎︎︎︎⭐︎︎︎︎⭐︎︎︎︎︎" + user);
		System.out.println(user.getUserId());
		entity.setUserId(user.getUserId());
		entity.setReservationDate(form.getReservationDate());
		entity.setReservationTime(form.getReservationTime());
		File destFile = null;
		if (isImageLocal) {
			destFile = saveImageLocal(image, entity);
			entity.setPath(destFile.getAbsolutePath());
		} else {
			entity.setPath("");
		}
		entity.setDescription(form.getDescription());
		repository.saveAndFlush(entity);
		System.out.println(entity);

		redirAttrs.addFlashAttribute("hasMessage", true);
		redirAttrs.addFlashAttribute("class", "alert-info");
		redirAttrs.addFlashAttribute("message",
				messageSource.getMessage("topics.create.flash.2", new String[] {}, locale));

		Context context = new Context();
		//sendMailService.sendMail(context);

		return "redirect:/topics";
	}

	private File saveImageLocal(MultipartFile image, Topic entity) throws IOException {
		File uploadDir = new File("/uploads");
		uploadDir.mkdir();

		String uploadsDir = "/uploads/";
		String realPathToUploads = request.getServletContext().getRealPath(uploadsDir);
		if (!new File(realPathToUploads).exists()) {
			new File(realPathToUploads).mkdir();
		}
		String fileName = image.getOriginalFilename();
		File destFile = new File(realPathToUploads, fileName);
		image.transferTo(destFile);

		return destFile;
	}

	@GetMapping("/topic/edit")
	public String edit(@RequestParam int id, Model model) {
		System.out.println("testtest" + id);
		Topic entity = repository.getReferenceById((long) id);
		TopicForm topicForm = modelMapper.map(entity, TopicForm.class);
		model.addAttribute("form", topicForm);
		System.out.println("testtest" + topicForm);
		return "topics/edit";
	}

	@PostMapping("/topic/update")
	public String update(@ModelAttribute("form") TopicForm form, Model model) {
		Topic entity = repository.getReferenceById(form.getId());
		System.out.println("aioueaiueo︎" + form.getId());
		entity.setReservationDate(form.getReservationDate());
		entity.setReservationTime(form.getReservationTime());
		entity.setDescription(form.getDescription());
		System.out.println("⭐︎⭐︎⭐︎⭐︎⭐︎⭐︎⭐︎⭐︎⭐︎" + entity);
		repository.saveAndFlush(entity);
		return "redirect:/topic/show?id=" + form.getId();
	}

	@GetMapping("/topic/delete")
	public String delete(@RequestParam int id, Model model) {
		Topic entity = repository.getReferenceById((long) id);
		TopicForm topicForm = modelMapper.map(entity, TopicForm.class);
		model.addAttribute("form", topicForm);
		repository.deleteById((long) id);
		return "redirect:/topics";
	}
}