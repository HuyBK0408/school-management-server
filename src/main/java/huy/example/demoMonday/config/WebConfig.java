// huy/example/demoMonday/config/WebConfig.java
package huy.example.demoMonday.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.base-dir:uploads}")
    private String baseDir;

    @Value("${app.upload.public-prefix:/files}")
    private String publicPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(baseDir).toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) location += "/";
        registry.addResourceHandler(publicPrefix + "/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }


}
