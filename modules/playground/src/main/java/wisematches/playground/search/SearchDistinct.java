package wisematches.playground.search;

import java.lang.annotation.*;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SearchDistinct {
	String value();
}
