package com.example.yui.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.yui.entity.Favorite;
import com.example.yui.entity.Topic;
import com.example.yui.entity.UserInf;
import com.example.yui.form.TopicForm;
import com.example.yui.repository.FavoriteRepository;

import jakarta.transaction.Transactional;

@Controller
public class FavoritesController {

	@Autowired
	private MessageSource messageSource;
	@Autowired
	private FavoriteRepository repository;
	@Autowired
	private TopicsController topicsController;

	@GetMapping("/favorites")
	public String index(Principal principal, Model model) throws IOException {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		List<Favorite> topics = repository.findByUserIdOrderByUpdatedAtDesc(user.getUserId());
		List<TopicForm> list = new ArrayList<>();
		for (Favorite entity : topics) {
			Topic topicEntity = entity.getTopic();
			TopicForm form = topicsController.getTopic(user, topicEntity);
			list.add(form);
		}
		model.addAttribute("list", list);

		return "topics/index";
	}

	@PostMapping("/favorite")
	public String create(Principal principal, @RequestParam("topic_id") long topicId, RedirectAttributes redirAttrs,
			Locale locale) {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		Long userId = user.getUserId();
		List<Favorite> results = repository.findByUserIdAndTopicId(userId, topicId);
		if (results.size() == 0) {
			Favorite entity = new Favorite();
			entity.setUserId(userId);
			entity.setTopicId(topicId);
			repository.saveAndFlush(entity);

			redirAttrs.addFlashAttribute("hasMessage", true);
			redirAttrs.addFlashAttribute("class", "alert-info");
			redirAttrs.addFlashAttribute("message",
					messageSource.getMessage("favorites.create.flash", new String[] {}, locale));
		}

		return "redirect:/topics";
	}

	@DeleteMapping("/favorite")
	@Transactional
	public String destroy(Principal principal, @RequestParam("topic_id") long topicId, RedirectAttributes redirAttrs,
			Locale locale) {
		Authentication authentication = (Authentication) principal;
		UserInf user = (UserInf) authentication.getPrincipal();
		Long userId = user.getUserId();
		List<Favorite> results = repository.findByUserIdAndTopicId(userId, topicId);
		if (results.size() == 1) {
			repository.deleteByUserIdAndTopicId(user.getUserId(), topicId);

			redirAttrs.addFlashAttribute("hasMessage", true);
			redirAttrs.addFlashAttribute("class", "alert-info");
			redirAttrs.addFlashAttribute("message",
					messageSource.getMessage("favorites.destroy.flash", new String[] {}, locale));
		}
		return "redirect:/topics";
	}
}