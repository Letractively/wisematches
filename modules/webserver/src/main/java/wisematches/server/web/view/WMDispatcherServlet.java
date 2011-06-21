package wisematches.server.web.view;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;

import java.util.Map;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class WMDispatcherServlet extends DispatcherServlet {
	public WMDispatcherServlet() {
	}

	@Override
	protected void initStrategies(ApplicationContext context) {
		final Map<String, HandlerMapping> matchingBeans =
				BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
		for (HandlerMapping handlerMapping : matchingBeans.values()) {
			if (handlerMapping instanceof AbstractHandlerMapping) {
				AbstractHandlerMapping mapping = (AbstractHandlerMapping) handlerMapping;
				mapping.setAlwaysUseFullPath(true);
			}
		}

		Map<String, HandlerAdapter> matchingBeans2 =
				BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
		for (HandlerAdapter handlerAdapter : matchingBeans2.values()) {
			if (handlerAdapter instanceof AbstractHandlerMapping) {
				AbstractHandlerMapping adapter = (AbstractHandlerMapping) handlerAdapter;
				adapter.setAlwaysUseFullPath(true);
			}
		}

		super.initStrategies(context);

	}
}
