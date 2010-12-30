/*
 * Copyright (c) 2010, WiseMatches (by Sergey Klimenko).
 */

package wisematches.server.web.mvc.services;

import wisematches.server.web.mvc.forms.ProblemsReportForm;

/**
 * @author klimese
 */
public class ProblemsReportService {
	public ServiceResponse reportProblem(ProblemsReportForm report) {
		System.out.println(report);
		if (report.getEmail().startsWith("test")) {
			return ServiceResponse.failure("adasdasdadad", "email", "asd.qwe.asdf.wer");
		}
		return ServiceResponse.SUCCESS;
	}
}