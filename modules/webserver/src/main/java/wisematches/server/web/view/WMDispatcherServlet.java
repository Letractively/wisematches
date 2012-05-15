package wisematches.server.web.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class WMDispatcherServlet extends DispatcherServlet {
    private static final Log log = LogFactory.getLog("wisematches.server.web.servlet");

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

    @Override
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            super.doDispatch(request, response);
        } catch (Exception ex) {
            log.error("Page can't be opened: " + request.getRequestURI() + "?" + request.getQueryString(), ex);
            throw ex;
        }
    }

    @Override
    protected ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.info("Handled exception received: " + ex.getMessage());
        return super.processHandlerException(request, response, handler, ex);
    }
}
