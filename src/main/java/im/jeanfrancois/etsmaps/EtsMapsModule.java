package im.jeanfrancois.etsmaps;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import im.jeanfrancois.etsmaps.model.NavigableMap;
import im.jeanfrancois.etsmaps.model.svg.SvgNavigableMap;
import im.jeanfrancois.etsmaps.ui.MapDisplayComponent;
import im.jeanfrancois.etsmaps.ui.NavigationPanel;
import im.jeanfrancois.etsmaps.ui.svg.SvgMapComponent;


/**
 * Google Guice module to configure the ETS maps applet.
 */
public class EtsMapsModule extends AbstractModule {
	protected void configure() {
		bind(NavigableMap.class).to(SvgNavigableMap.class).in(Scopes.SINGLETON);
		bind(MapDisplayComponent.class).to(SvgMapComponent.class).in(Scopes.SINGLETON);
		bind(NavigationPanel.class).in(Scopes.SINGLETON);
	}
}
