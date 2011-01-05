/*
 * Copyright (c) 2010, WiseMatches (by Sergey Klimenko).
 */

package wisematches.server.web.mvc.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import java.util.Locale;

/**
 * @author klimese
 */
@Controller
public class TestController {
	@RequestMapping("/test")
	public String infoPages(WebRequest webRequest, Model model, Locale locale) {
		return "/content/test";
	}
}
