/*
 * Created by IntelliJ IDEA.
 * User: jfim
 * Date: Sep 28, 2010
 * Time: 7:06:42 PM
 */
package im.jeanfrancois.etsmaps;

import com.google.inject.AbstractModule;
import im.jeanfrancois.etsmaps.ui.MapDisplayComponent;
import im.jeanfrancois.etsmaps.ui.svg.SvgMapComponent;

public class EtsMapsModule extends AbstractModule {
	protected void configure() {
		bind(MapDisplayComponent.class).to(SvgMapComponent.class);
	}
}
