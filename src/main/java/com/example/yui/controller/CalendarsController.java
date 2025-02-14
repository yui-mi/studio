package com.example.yui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CalendarsController {

	@GetMapping("/calendars")
	public String index(Model model) {
		return "calendars/index";
	}
}