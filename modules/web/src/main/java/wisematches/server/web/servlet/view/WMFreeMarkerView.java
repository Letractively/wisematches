package wisematches.server.web.servlet.view;

import freemarker.ext.servlet.FreemarkerServlet;
import freemarker.ext.servlet.HttpRequestParametersHashModel;
import freemarker.template.*;
import org.springframework.beans.BeansException;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;
import wisematches.core.Language;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class WMFreeMarkerView extends FreeMarkerView {
	private FreeMarkerConfig configuration;
	private Collection<Class<? extends Enum>> exposeEnums;

	public WMFreeMarkerView() {
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void processTemplate(Template template, SimpleHash model, HttpServletResponse response) throws IOException, TemplateException {
		model.put("locale", template.getLocale());
		model.put("language", Language.byLocale(template.getLocale()));

		if (exposeEnums != null) {
			for (Class<? extends Enum> exposeEnum : exposeEnums) {
				FreeMarkerEnumMap view = FreeMarkerEnumMap.valueOf(exposeEnum);
				model.put(exposeEnum.getSimpleName(), view);
			}
		}

		final HttpRequestParametersHashModel sm2 = (HttpRequestParametersHashModel) model.get(FreemarkerServlet.KEY_REQUEST_PARAMETERS);
		if ("true".equalsIgnoreCase(String.valueOf(sm2.get("plain"))) ||
				(model.get("plain") != null && ((TemplateBooleanModel) model.get("plain")).getAsBoolean())) {
			super.processTemplate(template, model, response);
		} else {
			model.put("templateName", getUrl());
			super.processTemplate(getTemplate("/content/wisematches.ftl", template.getLocale()), model, response);
		}
	}

	@Override
	protected FreeMarkerConfig autodetectConfiguration() throws BeansException {
		return configuration;
	}

/*
	@Override
	protected FreeMarkerConfig autodetectConfiguration() throws BeansException {
		try {
			return getApplicationContext().getBean("freemarkerConfig", FreeMarkerConfig.class);
		} catch (NoSuchBeanDefinitionException ex) {
			throw new ApplicationContextException(
					"Must define a single FreeMarkerConfig bean in this web application context " +
							"(may be inherited): FreeMarkerConfigurer is the usual implementation. " +
							"This bean may be given any name.", ex);
		}
	}
*/

	public void setConfiguration(FreeMarkerConfig configuration) {
		this.configuration = configuration;
	}

	public void setExposeEnums(Collection<Class<? extends Enum>> exposeEnums) throws TemplateModelException {
		this.exposeEnums = exposeEnums;
	}
}
