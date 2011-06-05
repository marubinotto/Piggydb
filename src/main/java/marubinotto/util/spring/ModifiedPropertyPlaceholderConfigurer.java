package marubinotto.util.spring;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import marubinotto.util.PropertyValueConverter;

import org.apache.commons.lang.UnhandledException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class ModifiedPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
	
	private static Log logger = LogFactory.getLog(ModifiedPropertyPlaceholderConfigurer.class);
	
	private Map<String, PropertyValueConverter> converters = 
		new HashMap<String, PropertyValueConverter>();

	public ModifiedPropertyPlaceholderConfigurer() {
	}

	public Map<String, PropertyValueConverter> getConverters() {
		return converters;
	}

	public void setConverters(Map<String, PropertyValueConverter> converters) {
		this.converters = converters;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void convertProperties(Properties properties) {
		super.convertProperties(properties);
		
		Enumeration<String> names = (Enumeration<String>)properties.propertyNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			String value = properties.getProperty(name);
			
			PropertyValueConverter converter = this.converters.get(name);
			if (converter != null) {
				logger.info("Converting: " + name + " ...");
				try {
					properties.setProperty(name, converter.convertPropertyValue(value));
				}
				catch (Exception e) {
					throw new UnhandledException(e);
				}
			}
		}
	}
}
