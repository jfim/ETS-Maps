package im.jeanfrancois.etsmaps;

import com.google.inject.AbstractModule;
import im.jeanfrancois.etsmaps.ui.MapDisplayComponent;
import im.jeanfrancois.etsmaps.ui.svg.SvgMapComponent;

/**
 * Google Guice module to configure the ETS maps applet.
 */
public class EtsMapsModule extends AbstractModule {
	protected void configure() {
		bind(MapDisplayComponent.class).to(SvgMapComponent.class);
	}
}
